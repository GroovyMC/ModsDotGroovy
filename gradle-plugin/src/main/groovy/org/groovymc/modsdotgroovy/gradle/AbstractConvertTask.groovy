/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

package org.groovymc.modsdotgroovy.gradle

import groovy.transform.CompileStatic
import org.groovymc.modsdotgroovy.core.Platform
import org.groovymc.modsdotgroovy.frontend.ModsDotGroovyFrontend
import org.groovymc.modsdotgroovy.transform.MDGBindingAdder
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.file.CopySpec
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.language.jvm.tasks.ProcessResources

import javax.inject.Inject
import java.nio.file.Files

@CacheableTask
abstract class AbstractConvertTask extends DefaultTask {
    private static final CompilerConfiguration MDG_COMPILER_CONFIG = new CompilerConfiguration().tap {
        targetBytecode = JDK17
        optimizationOptions['indy'] = true
    }

    @Classpath
    @InputFile
    abstract RegularFileProperty getInput()

    @Optional
    @OutputFile
    abstract RegularFileProperty getOutput()

    @Input
    @Optional
    abstract Property<Configuration> getDslConfiguration()

    @Input
    @Optional
    abstract MapProperty<String, Object> getArguments()

    @Input
    @Optional
    abstract ListProperty<String> getCatalogs()

    @Classpath
    @InputFiles
    abstract ListProperty<File> getMdgRuntimeFiles()

    @Input
    @Optional
    abstract ListProperty<String> getEnvironmentBlacklist()

    @Internal
    protected abstract String getOutputName()

    protected abstract void setupPlatformSpecificArguments()
    protected abstract String writeData(Map data)

    @Internal
    protected abstract String getOutputDir()

    @Internal
    protected abstract Platform getPlatform()

    @Inject
    protected abstract ProjectLayout getProjectLayout()

    @Inject
    protected abstract ObjectFactory getObjects()

    AbstractConvertTask() {
        notCompatibleWithConfigurationCache('This version of the ModsDotGroovy Gradle plugin does not support the configuration cache.')
        environmentBlacklist.convention(['pass', 'password', 'token', 'key', 'secret'])
        output.convention(projectLayout.buildDirectory.dir(name).map {it.file(getOutputName())})
        arguments.convention(objects.mapProperty(String, Object))
        catalogs.convention(['libs'])
        project.afterEvaluate {
            arguments.put('buildProperties', filterBuildProperties(project.extensions.extraProperties.properties, environmentBlacklist.get()))
            catalogs.get().each {
                arg(it, versionCatalogToMap(getLibsExtension(project, it)))
            }
            arg('version', project.version)
            arg('platform', getPlatform() ?: Platform.UNKNOWN)
            arg('group', project.group)
            setupPlatformSpecificArguments()
        }
    }

    @CompileStatic
    protected static Map<String, Object> filterBuildProperties(final Map<String, Object> buildProperties, final List<String> blacklist) {
        return buildProperties.findAll { entry ->
            for (String blacklistEntry in blacklist) {
                if (entry.key.containsIgnoreCase(blacklistEntry))
                    return false
            }
            return true
        } as Map<String, Object>
    }

    protected static VersionCatalog getLibsExtension(Project project, String name) {
        java.util.Optional<? extends VersionCatalog> catalogView = project.extensions.findByType(VersionCatalogsExtension)?.find(name)
        if (!catalogView?.isEmpty() || project.parent === null) {
            return catalogView?.orElse(null)
        }
        return getLibsExtension(project.parent, name)
    }

    protected static Map versionCatalogToMap(VersionCatalog catalog) {
        if (catalog === null)
            return [:]
        Map out = [:]
        Map versions = [:]
        Map plugins = [:]
        Map bundles = [:]
        out.versions = versions
        out.plugins = plugins
        out.bundles = bundles
        catalog.versionAliases.each {
            var val = catalog.findVersion(it)
            if (val.isPresent())
                writeByPartwise(versions, it, "${val.get()}" as String)
        }
        catalog.pluginAliases.each {
            var val = catalog.findPlugin(it)
            if (val.isPresent())
                writeByPartwise(plugins, it, "${val.get().get()}" as String)
        }
        catalog.bundleAliases.each {
            var val = catalog.findBundle(it)
            if (val.isPresent()) {
                var modules = val.get().get()
                writeByPartwise(bundles, it, modules.collect {"${it}" as String})
            }
        }
        catalog.libraryAliases.each {
            var val = catalog.findLibrary(it)
            if (val.isPresent())
                writeByPartwise(out, it, "${val.get().get()}" as String)
        }
        return out
    }

    protected static void writeByPartwise(Map root, String path, Object value) {
        List<String> parts = path.split(/\./).collect {it.trim()}.findAll {!it.isEmpty()}
        if (parts.size() === 1)
            root[parts[0]] = value
        else if (parts.size() >= 1) {
            Object inner = root.computeIfAbsent(parts[0], {[:]})
            if (inner !instanceof Map) {
                root[parts[0]] = [:]
                inner = root[parts[0]]
            }
            writeByPartwise(inner as Map, parts.subList(1, parts.size()).join('.'), value)
            root[parts[0]] = inner
        }
    }

    void arg(String name, Object arg) {
        arguments[name] = arg
    }

    @TaskAction
    @CompileStatic
    void run() {
        final input = getInput().asFile.get()
        if (!input.exists()) {
            logger.warn("Input file {} for task '{}' could not be found!", input, getName())
            return
        }
        final Map data = from(input)
        final outPath = getOutput().get().asFile.toPath()
        if (outPath.parent !== null && !Files.exists(outPath.parent)) Files.createDirectories(outPath.parent)
        Files.deleteIfExists(outPath)
        Files.writeString(outPath, writeData(data))
    }

    @CompileStatic
    Map from(File script) {
        final compilerConfig = new CompilerConfiguration(MDG_COMPILER_CONFIG)
        compilerConfig.classpathList = mdgRuntimeFiles.get().collect { it.toString() }

        final bindingAdderTransform = new ASTTransformationCustomizer(MDGBindingAdder)
        final Platform platform = arguments.get()['platform'] as Platform
        if (platform !== Platform.FORGE)
            bindingAdderTransform.annotationParameters = [className: "${platform.toString()}ModsDotGroovy"] as Map<String, Object>

        compilerConfig.addCompilationCustomizers(bindingAdderTransform)

        final bindings = new Binding(arguments.get())
        final shell = new GroovyShell(getClass().classLoader, bindings, compilerConfig)
        return ((ModsDotGroovyFrontend) shell.evaluate(script)).core.build()
    }

    @SuppressWarnings('unused')
    void configureForSourceSet(SourceSet sourceSet, String fileName = 'mods.groovy') {
        final modsToml = ModsDotGroovyGradlePlugin.browse(sourceSet) { new File(it, fileName) }
        if (modsToml === null) throw new IllegalArgumentException("Cannot find '$fileName' file in source set $sourceSet")
        input.set(modsToml)
        project.configurations.named(sourceSet.compileOnlyConfigurationName) {
            extendsFrom project.configurations.getByName(ModsDotGroovyGradlePlugin.CONFIGURATION_NAME_ROOT)
        }

        project.tasks.named(sourceSet.processResourcesTaskName, ProcessResources).configure {
            exclude fileName
            dependsOn this
            from(output.get().asFile) { CopySpec spec ->
                spec.into getOutputDir()
            }
        }
    }
}
