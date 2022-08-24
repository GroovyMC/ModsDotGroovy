/*
 * MIT License
 *
 * Copyright (c) 2022 GroovyMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.groovymc.modsdotgroovy

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.CopySpec
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.language.jvm.tasks.ProcessResources

import java.nio.file.Files

abstract class AbstractConvertTask extends DefaultTask {
    @InputFile
    abstract RegularFileProperty getInput()
    @Optional
    @OutputFile
    abstract RegularFileProperty getOutput()
    @Optional
    @InputFile
    abstract RegularFileProperty getDslLocation()
    @Optional
    @Input
    abstract Property<Configuration> getDslConfiguration()

    @Input
    @Optional
    abstract MapProperty<String, Object> getArguments()

    @Internal
    protected abstract String getOutputName()
    @Internal
    protected abstract void setupPlatformSpecificArguments()
    @Internal
    protected abstract String writeData(Map data)
    @Internal
    protected abstract String getOutputDir()
    @Internal
    protected abstract String getPlatform()

    @Internal
    protected String getScriptHeader() {
        return """
if (ModsDotGroovy.metaClass.respondsTo(null,'setPlatform')) {
    ModsDotGroovy.setPlatform('${getPlatform()}')
}
"""
    }

    AbstractConvertTask() {
        output.convention(project.layout.buildDirectory.dir(name).map {it.file(getOutputName())})
        arguments.convention(project.objects.mapProperty(String, Object))
        project.afterEvaluate {
            arguments.put('buildProperties', project.extensions.extraProperties.properties)
            arg('version', project.version)
            arg('platform', getPlatform())
            arg('group', project.group)
            setupPlatformSpecificArguments()
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
        Files.writeString(outPath, writeData(data))
    }

    @CompileDynamic
    Map from(File script) {
        final bindings = new Binding(arguments.get())
        final actualDsl = dslLocation.getOrNull()?.asFile ?: (dslConfiguration.getOrNull()?:project.configurations.getByName(ModsDotGroovy.CONFIGURATION_NAME)).resolve().find()
        final shell = new GroovyShell(getClass().classLoader, bindings, new DelegateConfig(CompilerConfiguration.DEFAULT) {
            final List<String> classpath = List.of(actualDsl.toString())
            @Override
            List<String> getClasspath() {
                return classpath
            }
        })
        shell.evaluate(getScriptHeader())
        return shell.evaluate(script).data as Map
    }

    @SuppressWarnings('unused')
    void configureForSourceSet(SourceSet sourceSet, String fileName = 'mods.groovy') {
        final modsToml = ModsDotGroovy.browse(sourceSet)
                { new File(it, fileName) }
        if (modsToml === null) throw new IllegalArgumentException("Cannot find '$fileName' file in source set $sourceSet")
        input.set(modsToml)
        project.configurations.getByName(sourceSet.compileOnlyConfigurationName)
                .extendsFrom(project.configurations.getByName(ModsDotGroovy.CONFIGURATION_NAME))

        project.tasks.named(sourceSet.processResourcesTaskName, ProcessResources).configure {
            it.exclude(fileName)
            it.dependsOn(this)
            it.from(output.get().asFile) { CopySpec spec ->
                spec.into getOutputDir()
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
