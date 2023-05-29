package io.github.groovymc.modsdotgroovy.gradle

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
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
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.annotations.Nullable

class ModsDotGroovyGradlePlugin implements Plugin<Project> {
    public static final String CONFIGURATION_NAME_ROOT = 'mdgRuntime'
    public static final String CONFIGURATION_NAME_PLUGIN = 'mdgPlugin'
    public static final String CONFIGURATION_NAME_FRONTEND = 'mdgFrontend'

    @Override
    void apply(Project project) {
        final ext = project.extensions.create(MDGExtension.NAME, MDGExtension)

        final rootConfiguration = project.configurations.register(CONFIGURATION_NAME_ROOT) {
            canBeConsumed = false
            attributes.attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category, Category.LIBRARY))
            attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling, Bundling.EXTERNAL))
            attributes.attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, project.objects.named(TargetJvmEnvironment, TargetJvmEnvironment.STANDARD_JVM))
            attributes.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage, Usage.JAVA_RUNTIME))
            attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements, LibraryElements.JAR))
        }.get()

        final pluginConfiguration = project.configurations.register(CONFIGURATION_NAME_PLUGIN) {
            extendsFrom project.configurations.named(CONFIGURATION_NAME_ROOT).get()
        }

        final frontendConfiguration = project.configurations.register(CONFIGURATION_NAME_FRONTEND) {
            extendsFrom project.configurations.named(CONFIGURATION_NAME_ROOT).get()
        }

        project.getPlugins().apply('java')

        project.afterEvaluate {
            if (ext.setupDsl.get()) {
                project.repositories.maven { MavenArtifactRepository repo ->
                    repo.name = 'Modding Inquisition Releases'
                    repo.url = 'https://maven.moddinginquisition.org/releases'
                }
                rootConfiguration.dependencies.add(project.dependencies.create('io.github.groovymc.modsdotgroovy:frontend-dsl'))
            }

            if (ext.automaticConfiguration.get()) {
                final List<MDGExtension.Platform> platforms = ext.platforms.get()
                for (MDGExtension.Platform platform : platforms.unique(false)) {
                    if (platform !== MDGExtension.Platform.MULTILOADER) {
                        final SourceSetContainer srcSets = project.extensions.getByType(JavaPluginExtension).sourceSets
                        final srcSet = ext.source.isPresent() ? ext.source.get() : browse(srcSets) { new File(it, 'mods.groovy')}
                                .map((FileWithSourceSet fileWithSourceSet) -> fileWithSourceSet.sourceSet)
                                .orElseGet(() -> srcSets.named('main').get())

                        final modsGroovy = new FileWithSourceSet(srcSet, new File(srcSet.resources.srcDirs.find(), 'mods.groovy'))

                        project.configurations.named(modsGroovy.sourceSet.compileOnlyConfigurationName) {
                            extendsFrom rootConfiguration
                        }

                        switch (platform) {
                            case MDGExtension.Platform.FORGE:
                                makeAndAppendForgeTask(modsGroovy, project).with {
                                    arguments.set(ext.arguments.get())
                                    catalogs.set(ext.catalogs.get())
                                }
                                break
                            case MDGExtension.Platform.QUILT:
                                makeAndAppendQuiltTask(modsGroovy, project).with {
                                    arguments.set(ext.arguments.get())
                                    catalogs.set(ext.catalogs.get())
                                }
                        }
                    } else {
                        final common = ext.multiloader.getOrNull()?.common ?: project.subprojects.find { name.equalsIgnoreCase('common') }

                        if (common === null)
                            throw new IllegalArgumentException("Specified platform 'multiloader' but missing common subproject")

                        final quilt = ext.multiloader.isPresent() ? ext.multiloader.get().quilt : [project.subprojects.find { name.equalsIgnoreCase('quilt') }]
                        final forge = ext.multiloader.isPresent() ? ext.multiloader.get().forge : [project.subprojects.find { name.equalsIgnoreCase('forge') }]

                        final SourceSetContainer commonSrcSets = common.extensions.getByType(JavaPluginExtension).sourceSets
                        final commonSrcSet = ext.source.isPresent() ? ext.source.get() : browse(commonSrcSets) { new File(it, 'mods.groovy') }
                                .map((FileWithSourceSet fileWithSourceSet) -> fileWithSourceSet.sourceSet)
                                .orElseGet(() -> commonSrcSets.named('main').get())

                        final modsGroovy = new FileWithSourceSet(commonSrcSet, new File(commonSrcSet.resources.srcDirs.find(), 'mods.groovy'))

                        final commonConfiguration = common.configurations.named(CONFIGURATION_NAME_ROOT) ?: common.configurations.register(CONFIGURATION_NAME_ROOT)
//                        commonConfiguration.configure {
//                            dependencies.add(common.dependencies.create(ext.frontendDsl()))
//                        }
                        common.configurations.named(modsGroovy.sourceSet.compileOnlyConfigurationName) {
                            extendsFrom rootConfiguration, commonConfiguration.get()
                        }

                        forge.each {
                            makeAndAppendForgeTask(modsGroovy, it).with {
                                dslConfiguration.set(commonConfiguration)
                                arguments.set(ext.arguments.get())
                                catalogs.set(ext.catalogs.get())
                            }
                        }
                        quilt.each {
                            makeAndAppendQuiltTask(modsGroovy, it).with {
                                dslConfiguration.set(commonConfiguration)
                                arguments.set(ext.arguments.get())
                                catalogs.set(ext.catalogs.get())
                            }
                        }
                    }
                }
            }
        }
    }

    static ConvertToTomlTask makeAndAppendForgeTask(FileWithSourceSet modsGroovy, Project project) {
        final convertTask = project.getTasks().create('modsDotGroovyToToml', ConvertToTomlTask) {
            notCompatibleWithConfigurationCache('This version of the ModsDotGroovy Gradle plugin does not support the configuration cache.')
            input.set(modsGroovy.file)
            dependsOn project.configurations.named(CONFIGURATION_NAME_ROOT)
            dependsOn project.configurations.named(CONFIGURATION_NAME_FRONTEND)
            dependsOn project.configurations.named(CONFIGURATION_NAME_PLUGIN)
        }
        project.tasks.named(modsGroovy.sourceSet.processResourcesTaskName, ProcessResources).configure {
            exclude((FileTreeElement el) -> el.file == convertTask.input.get().asFile)
            dependsOn convertTask
            from(convertTask.output.get().asFile) {
                into 'META-INF'
            }
        }
        return convertTask
    }

    static ConvertToQuiltJsonTask makeAndAppendQuiltTask(FileWithSourceSet modsGroovy, Project project) {
        final convertTask = project.getTasks().create('modsDotGroovyToQuiltJson', ConvertToQuiltJsonTask) {
            notCompatibleWithConfigurationCache('This version of the ModsDotGroovy Gradle plugin does not support the configuration cache.')
            input.set(modsGroovy.file)
            dependsOn project.configurations.named(CONFIGURATION_NAME_ROOT)
            dependsOn project.configurations.named(CONFIGURATION_NAME_FRONTEND)
            dependsOn project.configurations.named(CONFIGURATION_NAME_PLUGIN)
        }
        project.tasks.named(modsGroovy.sourceSet.processResourcesTaskName, ProcessResources).configure {
            exclude((FileTreeElement el) -> el.file == convertTask.input.get().asFile)
            dependsOn(convertTask)
            from(convertTask.output.get().asFile) {
                into ''
            }
        }
        return convertTask
    }

    static Optional<FileWithSourceSet> browse(final Collection<SourceSet> sourceSet, @ClosureParams(value = SimpleType, options = 'java.io.File') Closure<File> finder) {
        sourceSet.stream()
                .sorted { SourceSet it1, SourceSet it2 ->
                    if (it1.name == 'main') return 1
                    if (it2.name == 'main') return -1
                    return Comparator.<String>naturalOrder().compare(it1.name, it2.name)
                }
                .map {it -> new FileWithSourceSet(it, browse(it, finder)) }
                .filter { it.file !== null }
                .findFirst()
    }

    @Nullable
    @CompileStatic
    static File browse(final SourceSet sourceSet, @ClosureParams(value = SimpleType, options = 'java.io.File') Closure<File> finder) {
        sourceSet.resources.srcDirs.stream()
                .map(file -> finder(file))
                .filter(it -> it.exists())
                .findFirst().orElse(null)
    }

    @Canonical
    @CompileStatic
    static class FileWithSourceSet {
        SourceSet sourceSet
        File file
    }
}
