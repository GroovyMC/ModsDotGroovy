package org.groovymc.modsdotgroovy.gradle

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.gradle.api.file.FileTreeElement
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.language.jvm.tasks.ProcessResources
import org.groovymc.modsdotgroovy.core.Platform
import org.groovymc.modsdotgroovy.gradle.tasks.AbstractMDGConvertTask
import org.groovymc.modsdotgroovy.gradle.tasks.ModsDotGroovyToJson
import org.groovymc.modsdotgroovy.gradle.tasks.ModsDotGroovyToYml
import org.groovymc.modsdotgroovy.gradle.tasks.ModsDotGroovyToToml
import org.jetbrains.annotations.Nullable

@CompileStatic
class ModsDotGroovyGradlePluginOld implements Plugin<Project> {
    @PackageScope static final String CONFIGURATION_NAME_ROOT = 'mdgRuntime'
    @PackageScope static final String CONFIGURATION_NAME_PLUGIN = 'mdgPlugin'
    @PackageScope static final String CONFIGURATION_NAME_FRONTEND = 'mdgFrontend'

    private MDGExtension mdgExtension

    @Override
    @CompileDynamic
    void apply(Project project) {
        mdgExtension = project.extensions.create(MDGExtension.NAME, MDGExtension)

        final rootConfiguration = project.configurations.register(CONFIGURATION_NAME_ROOT) {
            canBeConsumed = false
            attributes.attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category, Category.LIBRARY))
            attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling, Bundling.EXTERNAL))
            attributes.attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, project.objects.named(TargetJvmEnvironment, TargetJvmEnvironment.STANDARD_JVM))
            attributes.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage, Usage.JAVA_RUNTIME))
            attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements, LibraryElements.JAR))
        }

        final pluginConfiguration = project.configurations.register(CONFIGURATION_NAME_PLUGIN) {
            extendsFrom rootConfiguration.get()
        }

        final frontendConfiguration = project.configurations.register(CONFIGURATION_NAME_FRONTEND) {
            extendsFrom rootConfiguration.get()
        }

        project.plugins.apply('java')
        project.plugins.apply('groovy')

        project.afterEvaluate {
            // make sure Groovy is loaded into mdgRuntime for IDE support
            rootConfiguration.get().dependencies.add(project.dependencies.create('org.apache.groovy:groovy:4.0.15'))

            final List<Platform> platforms = mdgExtension.platforms.get().unique(false)

            if (mdgExtension.setupDsl.get() || mdgExtension.setupPlugins.get()) {
                project.repositories.mavenCentral()
                project.repositories.maven { MavenArtifactRepository repo ->
                    repo.name = 'Modding Inquisition Releases'
                    repo.url = 'https://maven.moddinginquisition.org/releases'
                }
                project.repositories.maven { MavenArtifactRepository repo ->
                    repo.name = 'Modding Inquisition Snapshots'
                    repo.url = 'https://maven.moddinginquisition.org/snapshots'
                }

                if (mdgExtension.setupDsl.get()) {
                    if (platforms.containsAll([Platform.FORGE, Platform.FABRIC])) {
                        rootConfiguration.get().dependencies.add(project.dependencies.create('org.groovymc.modsdotgroovy.frontend-dsl:multiplatform'))
                    } else if (Platform.FORGE in platforms) {
                        rootConfiguration.get().dependencies.add(project.dependencies.create('org.groovymc.modsdotgroovy.frontend-dsl:forge'))
                    } else if (Platform.NEOFORGE in platforms) {
                        rootConfiguration.get().dependencies.add(project.dependencies.create('org.groovymc.modsdotgroovy.frontend-dsl:forge')) // for IDE support - IntelliJ 2023.2 doesn't recognise transitive deps in the base package
                        rootConfiguration.get().dependencies.add(project.dependencies.create('org.groovymc.modsdotgroovy.frontend-dsl:neoforge'))
                    } else if (Platform.FABRIC in platforms) {
                        rootConfiguration.get().dependencies.add(project.dependencies.create('org.groovymc.modsdotgroovy.frontend-dsl:fabric'))
                    } else if (Platform.QUILT in platforms) {
                        rootConfiguration.get().dependencies.add(project.dependencies.create('org.groovymc.modsdotgroovy.frontend-dsl:quilt'))
                    } else if (Platform.SPIGOT in platforms) {
                        rootConfiguration.get().dependencies.add(project.dependencies.create('org.groovymc.modsdotgroovy.frontend-dsl:spigot'))
                    } else {
                        throw new UnsupportedOperationException("""
                            There is no stock frontend DSL available for ${platforms} on this version of ModsDotGroovy.
                            Possible solutions:
                            - Check for updates to ModsDotGroovy
                            - Use a custom frontend by setting the 'setupDsl' property to false and adding an mdgFrontend dependency
                        """.stripIndent().strip())
                    }
                }

                if (mdgExtension.setupPlugins.get())
                    setupPlugins(project, platforms, rootConfiguration.get().dependencies)
            }

            if (mdgExtension.automaticConfiguration.get()) {
                for (Platform platform in platforms) {
//                    if (platform !== Platform.MULTILOADER) {
                        final SourceSetContainer srcSets = project.extensions.getByType(JavaPluginExtension).sourceSets
                        final srcSet = mdgExtension.source.isPresent() ? mdgExtension.source.get() : browse(srcSets) { new File(it, 'mods.groovy')}
                                .map((FileWithSourceSet fileWithSourceSet) -> fileWithSourceSet.sourceSet)
                                .orElseGet(() -> srcSets.named('main').get())

                        final modsGroovy = new FileWithSourceSet(srcSet, new File(srcSet.resources.srcDirs.find(), 'mods.groovy'))

                        project.configurations.named(modsGroovy.sourceSet.compileOnlyConfigurationName) {
                            extendsFrom rootConfiguration.get()
                        }

                        switch (platform) {
                            case Platform.FORGE:
                                makeAndAppendConvertTask(modsGroovy, project, 'Toml', ModsDotGroovyToToml, 'META-INF')
                                break
                            case Platform.NEOFORGE:
                                makeAndAppendConvertTask(modsGroovy, project, 'Toml', ConvertToNeoForgeTomlTask, 'META-INF')
                                break
                            case Platform.FABRIC:
                                makeAndAppendConvertTask(modsGroovy, project, 'FabricJson', ModsDotGroovyToJson)
                                break
                            case Platform.QUILT:
                                makeAndAppendConvertTask(modsGroovy, project, 'QuiltJson', ConvertToQuiltJsonTask)
                                break
                            case Platform.SPIGOT:
                                makeAndAppendConvertTask(modsGroovy, project, 'PluginYml', ModsDotGroovyToYml)
                                break
                        }
//                    } else {
//                        final common = mdgExtension.multiloader.getOrNull()?.common ?: project.subprojects.find { name.equalsIgnoreCase('common') }
//
//                        if (common === null)
//                            throw new IllegalArgumentException("Specified platform 'multiloader' but missing common subproject")
//
//                        final forge = mdgExtension.multiloader.isPresent() ? mdgExtension.multiloader.get().forge : [project.subprojects.find { name.equalsIgnoreCase('forge') }]
//                        final fabric = mdgExtension.multiloader.isPresent() ? mdgExtension.multiloader.get().fabric : [project.subprojects.find { name.equalsIgnoreCase('fabric') }]
//                        final quilt = mdgExtension.multiloader.isPresent() ? mdgExtension.multiloader.get().quilt : [project.subprojects.find { name.equalsIgnoreCase('quilt') }]
//
//                        final SourceSetContainer commonSrcSets = common.extensions.getByType(JavaPluginExtension).sourceSets
//                        final commonSrcSet = mdgExtension.source.isPresent() ? mdgExtension.source.get() : browse(commonSrcSets) { new File(it, 'mods.groovy') }
//                                .map((FileWithSourceSet fileWithSourceSet) -> fileWithSourceSet.sourceSet)
//                                .orElseGet(() -> commonSrcSets.named('main').get())
//
//                        final modsGroovy = new FileWithSourceSet(commonSrcSet, new File(commonSrcSet.resources.srcDirs.find(), 'mods.groovy'))
//
//                        final commonConfiguration = common.configurations.named(CONFIGURATION_NAME_ROOT) ?: common.configurations.register(CONFIGURATION_NAME_ROOT)
////                        commonConfiguration.configure {
////                            mdgRuntimeDependencies.add(common.mdgRuntimeDependencies.create(mdgExtension.frontendDsl()))
////                        }
//                        common.configurations.named(modsGroovy.sourceSet.compileOnlyConfigurationName) {
//                            extendsFrom rootConfiguration.get(), commonConfiguration.get()
//                        }
//
//                        forge.each {
//                            makeAndAppendForgeTask(modsGroovy, it).with {
//                                dslConfiguration.set(commonConfiguration)
//                                arguments.set(mdgExtension.arguments.get())
//                                catalogs.set(mdgExtension.catalogs.get())
//                            }
//                        }
//                        fabric.each {
//                            makeAndAppendFabricTask(modsGroovy, it).with {
//                                dslConfiguration.set(commonConfiguration)
//                                arguments.set(mdgExtension.arguments.get())
//                                catalogs.set(mdgExtension.catalogs.get())
//                            }
//                        }
//                        quilt.each {
//                            makeAndAppendQuiltTask(modsGroovy, it).with {
//                                dslConfiguration.set(commonConfiguration)
//                                arguments.set(mdgExtension.arguments.get())
//                                catalogs.set(mdgExtension.catalogs.get())
//                            }
//                        }
//                    }
                }
            }
            //println "deps: ${rootConfiguration.get().dependencies.collect({ it.group + ':' + it.name + ':' + it.version })}"
        }
    }

    private static void setupPlugins(final Project project, final List<Platform> platforms, final DependencySet mdgRuntimeDependencies) {
        if (platforms.containsAll([Platform.FORGE, Platform.FABRIC]))
            mdgRuntimeDependencies.add(project.dependencies.create('org.groovymc.modsdotgroovy.stock-plugins:multiplatform'))

        if (Platform.FORGE in platforms || Platform.NEOFORGE in platforms) {
            mdgRuntimeDependencies.add(project.dependencies.create('org.groovymc.modsdotgroovy.stock-plugins:forge'))

            if (Platform.NEOFORGE in platforms)
                mdgRuntimeDependencies.add(project.dependencies.create('org.groovymc.modsdotgroovy.stock-plugins:neoforge'))
        }

        if (Platform.FABRIC in platforms)
            mdgRuntimeDependencies.add(project.dependencies.create('org.groovymc.modsdotgroovy.stock-plugins:fabric'))

        if (Platform.QUILT in platforms)
            mdgRuntimeDependencies.add(project.dependencies.create('org.groovymc.modsdotgroovy.stock-plugins:quilt'))

        if (Platform.SPIGOT in platforms)
            mdgRuntimeDependencies.add(project.dependencies.create('org.groovymc.modsdotgroovy.stock-plugins:spigot'))
    }

    private static List<File> collectFilesFromAllMdgConfigurations(ConfigurationContainer configurations) {
        return collectFilesFromConfigurations(
                configurations.getByName(CONFIGURATION_NAME_ROOT),
                configurations.getByName(CONFIGURATION_NAME_FRONTEND),
                configurations.getByName(CONFIGURATION_NAME_PLUGIN),
        )
    }

    private static List<File> collectFilesFromConfigurations(final Configuration[] configurations) {
        final List<File> files = []
        for (configuration in configurations) {
            configuration.resolvedConfiguration.resolvedArtifacts.each { files.add(it.file) }
        }
        return files.unique({ File a, File b -> (a.absolutePath == b.absolutePath) ? 0 : 1 })
                    .toSorted(Comparator.comparing(File::getAbsolutePath))
    }

    @CompileDynamic
    private <T extends AbstractMDGConvertTask> AbstractMDGConvertTask makeAndAppendConvertTask(FileWithSourceSet modsGroovy, Project project,
                                                                                               String taskNameSuffix, Class<T> taskType, String destDir = '') {
        final T convertTask = project.tasks.create("modsDotGroovyTo${taskNameSuffix}", taskType) {
            notCompatibleWithConfigurationCache('This version of the ModsDotGroovy Gradle plugin does not support the configuration cache.')
            dependsOn project.configurations.named(CONFIGURATION_NAME_ROOT)

            input.set(modsGroovy.file)
            mdgRuntimeFiles.set(collectFilesFromAllMdgConfigurations(project.configurations))
            arguments.set(mdgExtension.arguments.get())
            catalogs.set(mdgExtension.catalogs.get())
            environmentBlacklist.set(mdgExtension.environmentBlacklist.get())
        }
        project.tasks.named(modsGroovy.sourceSet.processResourcesTaskName, ProcessResources).configure {
            exclude((FileTreeElement el) -> el.file == convertTask.input.get().asFile)
            dependsOn convertTask
            from(convertTask.output.get().asFile) {
                into destDir
            }
        }
        return convertTask
    }

    static Optional<FileWithSourceSet> browse(final Collection<SourceSet> sourceSet, @ClosureParams(value = SimpleType, options = 'java.io.File') Closure<File> finder) {
        sourceSet.stream()
                .sorted((SourceSet it1, SourceSet it2) -> {
                    if (it1.name == 'main') return 1
                    if (it2.name == 'main') return -1
                    return Comparator.<String>naturalOrder().compare(it1.name, it2.name)
                })
                .map(it -> new FileWithSourceSet(it, browse(it, finder)))
                .filter(it -> it.file !== null)
                .findFirst()
    }

    @Nullable
    static File browse(final SourceSet sourceSet, @ClosureParams(value = SimpleType, options = 'java.io.File') Closure<File> finder) {
        sourceSet.resources.srcDirs.stream()
                .map(file -> finder(file))
                .filter(File::exists)
                .findFirst().orElse(null)
    }

    @Canonical
    static class FileWithSourceSet {
        SourceSet sourceSet
        File file
    }
}
