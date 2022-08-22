package io.github.groovymc.modsdotgroovy

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.moandjiezana.toml.TomlWriter
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.file.CopySpec
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import org.gradle.language.jvm.tasks.ProcessResources

import java.nio.file.Files

abstract class ConvertToQuiltJsonTask extends DefaultTask {
    @InputFile
    abstract RegularFileProperty getInput()
    @Optional
    @OutputFile
    abstract RegularFileProperty getOutput()
    @Optional
    @InputFile
    abstract RegularFileProperty getDslLocation()

    @Input
    @Optional
    abstract MapProperty<String, Object> getArguments()

    ConvertToQuiltJsonTask() {
        output.convention(project.layout.buildDirectory.dir(name).map {it.file('quilt.mod.json')})
        arguments.convention(project.objects.mapProperty(String, Object))
        project.afterEvaluate {
            arguments.put('buildProperties', project.extensions.extraProperties.properties)
            arg('version', project.version)

            final mcDependency = project.configurations.findByName('minecraft')
                    ?.getDependencies()?.find()
            if (mcDependency !== null) {
                final version = mcDependency.version.split('-')
                arg('minecraftVersion', version[0])
                // No forge version
                //arg('forgeVersion', version[1].split('_mapped_')[0])

                final mcSplit = version[0].split('\\.')
                if (mcSplit.length > 1) {
                    try {
                        final currentVersion = Integer.parseInt(mcSplit[1])
                        arg('minecraftVersionRange', "[${version[0]},1.${currentVersion + 1})")
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    void arg(String name, Object arg) {
        arguments[name] = arg
    }

    @TaskAction
    void run() {
        final input = getInput().asFile.get()
        if (!input.exists()) {
            getProject().logger.warn("Input file {} for task '{}' could not be found!", input, getName())
            return
        }
        final data = from(input)
        final outPath = getOutput().get().asFile.toPath()
        if (outPath.parent !== null && !Files.exists(outPath.parent)) Files.createDirectories(outPath.parent)
        Files.deleteIfExists(outPath)
        final gsonWriter = new GsonBuilder()
                .setPrettyPrinting()
                .create()
        Files.writeString(outPath, gsonWriter.toJson(data))
    }

    @CompileDynamic
    Map from(File script) {
        final bindings = new Binding(arguments.get())
        final actualDsl = dslLocation.getOrNull()?.asFile ?: project.configurations.getByName(ModsDotGroovy.CONFIGURATION_NAME).resolve().find()
        final shell = new GroovyShell(getClass().classLoader, bindings, new DelegateConfig(CompilerConfiguration.DEFAULT) {
            final List<String> classpath = List.of(actualDsl.toString())
            @Override
            List<String> getClasspath() {
                return classpath
            }
        })
        return shell.evaluate(script).quiltData
    }

    @SuppressWarnings('unused')
    void configureForSourceSet(SourceSet sourceSet, String fileName = 'mods.groovy') {
        final modsJson = ModsDotGroovy.browse(sourceSet)
                { new File(it, fileName) }
        if (modsJson === null) throw new IllegalArgumentException("Cannot find '$fileName' file in source set $sourceSet")
        input.set(modsJson)
        project.configurations.getByName(sourceSet.compileOnlyConfigurationName)
                .extendsFrom(project.configurations.getByName(ModsDotGroovy.CONFIGURATION_NAME))

        project.tasks.named(sourceSet.processResourcesTaskName, ProcessResources).configure {
            it.exclude(fileName)
            it.dependsOn(this)
            it.from(output.get().asFile) { CopySpec spec ->
                spec.into ''
            }
        }
    }

    @CompileStatic
    static class DelegateConfig extends CompilerConfiguration {
        DelegateConfig(CompilerConfiguration configuration) {
            this.configuration = configuration
        }
        @Delegate
        final CompilerConfiguration configuration
    }
}
