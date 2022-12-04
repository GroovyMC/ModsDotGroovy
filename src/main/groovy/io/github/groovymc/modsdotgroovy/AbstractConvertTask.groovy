/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

package io.github.groovymc.modsdotgroovy

import com.google.gson.GsonBuilder
import com.moandjiezana.toml.TomlWriter
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import org.codehaus.groovy.control.CompilerConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.file.CopySpec
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources

import java.nio.file.Files
import java.util.function.Function

@CompileStatic
abstract class AbstractConvertTask extends DefaultTask {
    protected final Map<String, Strategy> strategies = [:]

    @InputFile
    abstract RegularFileProperty getInput()
    @Optional
    @OutputFiles
    abstract MapProperty<String, Object> getOutput()
    @Deprecated(forRemoval = true)
    @Optional
    @InputFile
    abstract RegularFileProperty getDslLocation()
    @Optional
    @Input
    abstract Property<Configuration> getDslConfiguration()

    @Input
    @Optional
    abstract MapProperty<String, Object> getArguments()
    @Input
    @Optional
    abstract ListProperty<String> getCatalogs()
    @Input
    @Optional
    abstract MapProperty<String, String> getMixinConfigs()

    protected abstract void setupPlatformSpecificArguments()

    void register(String id, Strategy strategy) {
        strategies[id] = strategy
    }

    protected String getOutputName(String mapId) {
        return strategies.get(mapId).outputName
    }

    protected String getOutputDir(String mapId) {
        return strategies.get(mapId).outputDir
    }

    protected String writeData(String mapId, Map data) {
        return strategies.get(mapId).writer.apply(data)
    }

    @Internal
    protected Collection<String> getKnownMapIds() {
        return strategies.keySet()
    }

    @Internal
    protected abstract String getPlatform()

    @Internal
    protected String getScriptHeader() {
        return """
if (ModsDotGroovy.metaClass.respondsTo(null,'setPlatform')) {
    ModsDotGroovy.setPlatform('${getPlatform()}')
}
if (ModsDotGroovy.metaClass.respondsTo(null,'setMixinRefMap')) {
    ${mixinConfigs.get().entrySet().stream().map { Map.Entry it ->
            "ModsDotGroovy.setMixinRefMap('$it.key', '$it.value')"
    }.iterator().join('\n')}
}
"""
    }

    AbstractConvertTask() {
        mixinConfigs.convention([:])
        output.convention(project.objects.mapProperty(String, Object))
        arguments.convention(project.objects.mapProperty(String, Object))
        catalogs.convention(['libs'])
        project.afterEvaluate {
            registerStrategies()

            arguments.put('buildProperties', project.extensions.extraProperties.properties)
            catalogs.get().forEach { String id ->
                arg(id, versionCatalogToMap(getLibsExtension(project, id)))
            }
            arg('version', project.version)
            arg('platform', getPlatform())
            arg('group', project.group)
            setupPlatformSpecificArguments()

            knownMapIds.each { mapId ->
                output.put(mapId, project.layout.buildDirectory.dir(name).map { it.file(getOutputName(mapId))} )
            }
        }
    }

    protected static VersionCatalog getLibsExtension(Project project, String name) {
        java.util.Optional<? extends VersionCatalog> catalogView = project.extensions.findByType(VersionCatalogsExtension)?.find(name)
        if (!catalogView?.isEmpty() || project.parent === null) {
            return catalogView?.orElse(null)
        }
        return getLibsExtension(project.parent, name)
    }

    protected static Map versionCatalogToMap(VersionCatalog catalog) {
        Map out = [:]
        Map versions = [:]
        Map plugins = [:]
        Map bundles = [:]
        out.versions = versions
        out.plugins = plugins
        out.bundles = bundles
        if (catalog === null)
            return [:]
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
        if (parts.size() == 1)
            root[parts[0]] = value
        else if (parts.size() >= 1) {
            Object inner = root.computeIfAbsent(parts[0], {[:]})
            if (!(inner instanceof Map)) {
                root[parts[0]] = [:]
                inner = root[parts[0]]
            }
            writeByPartwise(inner as Map, parts.subList(1, parts.size()).join('.'), value)
            root[parts[0]] = inner
        }
    }

    protected void registerStrategies() {}

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
        final maps = split(from(input))
        maps.each { mapId, data ->
            output.getting(mapId).map { project.file(it) }.getOrNull()?.toPath()?.tap { outPath ->
                if (outPath.parent !== null && !Files.exists(outPath.parent)) Files.createDirectories(outPath.parent)
                Files.deleteIfExists(outPath)
                Files.writeString(outPath, writeData(mapId, data))
            }
        }
    }

    @SuppressWarnings('GrDeprecatedAPIUsage')
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
            setupOnProcessResources(it, fileName)
        }
    }

    @CompileDynamic
    @PackageScope void setupOnProcessResources(ProcessResources processResources, Object exclusion) {
        //noinspection GroovyAssignabilityCheck
        processResources.exclude(exclusion)
        processResources.dependsOn(this)
        output.get().each { String mapId, Object file ->
            processResources.from(file) { CopySpec spec ->
                spec.into(getOutputDir(mapId))
                if (mapId == 'mixinConfig') {
                    spec.rename((String from) -> {
                        if (from == 'mixins.json') {
                            return mixinConfigName.getOrElse(project.tasks.named('jar')
                                    .map { (Jar) it }.map { it.archiveBaseName.get() + '.' }.getOrElse('') + 'mixins.json')
                        }
                        return from
                    })
                }
            }
        }
    }

    static Map<String, Map> split(Map map) {
        final Map<String, Map> maps = [:]
        final Map root = new HashMap(map)
        maps['root'] = root

        ((Map)root.getOrDefault('extraMaps', [:])).each { id, val ->
            maps[id as String] = val as Map
        }
        root.remove('extraMaps')

        return maps
    }

    @CompileStatic
    static class DelegateConfig extends CompilerConfiguration {
        DelegateConfig(CompilerConfiguration configuration) {
            this.configuration = configuration
        }
        @Delegate
        final CompilerConfiguration configuration
    }

    @TupleConstructor
    static final class Strategy {
        final String outputName
        final String outputDir
        final Function<Map, String> writer
    }

    protected static final Function<Map, String> TOML_WRITER = { Map data ->
        final tomlWriter = new TomlWriter.Builder()
                .indentValuesBy(2)
                .indentTablesBy(4)
                .build()
        return tomlWriter.write(data)
    }

    protected static final Function<Map, String> JSON_WRITER = { Map data ->
        final gson = new GsonBuilder()
                .setPrettyPrinting()
                .create()
        return gson.toJson(data)
    }
}
