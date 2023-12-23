package org.groovymc.modsdotgroovy.gradle

import groovy.transform.CompileStatic
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.jvm.tasks.ProcessResources
import org.groovymc.modsdotgroovy.core.Platform
import org.groovymc.modsdotgroovy.gradle.tasks.*

@CompileStatic
final class ModsDotGroovyGradlePlugin implements Plugin<Project> {
    private static final String CONFIGURATION_NAME_ROOT = 'mdgRuntime'
    private static final String CONFIGURATION_NAME_PLUGIN = 'mdgPlugin'
    private static final String CONFIGURATION_NAME_FRONTEND = 'mdgFrontend'

    private static final String MDG_MAVEN_GROUP = 'org.groovymc.modsdotgroovy'
    private static final String MDG_FRONTEND_GROUP = MDG_MAVEN_GROUP + '.frontend-dsl'
    private static final String MDG_PLUGIN_GROUP = MDG_MAVEN_GROUP + '.stock-plugins'

    private static final String EXTENSION_NAME = 'modsDotGroovy'

    @Override
    void apply(Project project) {
        // setup required plugins
        project.plugins.apply('java')
        project.plugins.apply('groovy')

        JavaPluginExtension javaPluginExtension = project.extensions.getByType(JavaPluginExtension)
        SourceSetContainer sourceSets = javaPluginExtension.sourceSets

        // set up the core extension for the 'main' source set
        //noinspection ConfigurationAvoidance
        var mainExtension = project.extensions.create(EXTENSION_NAME, MDGExtension, sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME), project)
        mainExtension.setupDsl.convention(true)
        mainExtension.setupPlugins.convention(true)
        mainExtension.setupTasks.convention(true)

        sourceSets.configureEach {
            if (SourceSet.isMain(it)) {
                var extension = mainExtension
                it.extensions.add(MDGExtension, EXTENSION_NAME, extension)
            } else {
                it.extensions.create(EXTENSION_NAME, MDGExtension, it, project)
            }
        }

        project.afterEvaluate {
            sourceSets.each {sourceSet ->
                MDGExtension extension = sourceSet.extensions.getByType(MDGExtension)
                extension.platforms.get().each { platform ->
                    // setup MDG dependency configurations
                    final rootConfiguration = project.configurations.register(MDGExtension.forSourceSetName(sourceSet.name, CONFIGURATION_NAME_ROOT+platform.name().capitalize())) { Configuration conf -> conf.tap {
                        canBeConsumed = false
                        attributes.tap {
                            attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category, Category.LIBRARY))
                            attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling, Bundling.EXTERNAL))
                            attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, project.objects.named(TargetJvmEnvironment, TargetJvmEnvironment.STANDARD_JVM))
                            attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage, Usage.JAVA_RUNTIME))
                            attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements, LibraryElements.JAR))
                        }
                    }}
                    final frontendConfiguration = project.configurations.register(MDGExtension.forSourceSetName(sourceSet.name, CONFIGURATION_NAME_FRONTEND+platform.name().capitalize())) { Configuration conf ->
                        conf.extendsFrom rootConfiguration.get()
                    }
                    final pluginConfiguration = project.configurations.register(MDGExtension.forSourceSetName(sourceSet.name, CONFIGURATION_NAME_PLUGIN+platform.name().capitalize())) { Configuration conf ->
                        conf.extendsFrom rootConfiguration.get()
                    }

                    // setup required MDG repositories and dependencies for better IDE support
                    project.repositories.mavenCentral()
                    rootConfiguration.configure(conf -> conf.dependencies.add(project.dependencies.create('org.apache.groovy:groovy:4.0.16')))

                    // if asked, setup the mods.groovy DSL
                    if (extension.setupDsl.get())
                        setupDsl(project, frontendConfiguration, platform, extension.multiplatform.get())

                    // the plugins have to be done on a per-platform basis
                    // if asked, setup the mods.groovy plugins
                    if (extension.setupPlugins.get())
                        setupPlugins(project, pluginConfiguration, platform, extension.multiplatform.get())

                    // now the hard part - the tasks
                    // if asked, setup the mods.groovy Gradle tasks
                    if (extension.setupTasks.get())
                        setupTasks(project, sourceSet, platform, rootConfiguration, pluginConfiguration, frontendConfiguration, extension.modsDotGroovyFile, extension.conversionOptions)


                    // setup IDE support by adding the mdgFrontend configuration to the compileOnly configuration
                    project.configurations.named(sourceSet.compileOnlyConfigurationName).configure(conf -> conf.extendsFrom(frontendConfiguration.get()))
                }
            }
        }
    }

    private static void setupDsl(Project project, NamedDomainObjectProvider<Configuration> frontendConfiguration, Platform platform, boolean multiplatform) {
        if (platform !in Platform.STOCK_PLATFORMS) {
            throw new UnsupportedOperationException("""
                There is no stock frontend DSL available for $platform on this version of ModsDotGroovy.
                Possible solutions:
                - Check for updates to ModsDotGroovy
                - Use a custom frontend by setting the 'setupDsl' property to false and adding an mdgFrontend dependency
            """.stripIndent().strip())
        }

        // mdgFrontend "org.groovymc.modsdotgroovy.frontend-dsl:<platform>"
        final String platformName = multiplatform ? 'multiplatform' : platform.name().toLowerCase(Locale.ROOT)
        frontendConfiguration.configure(conf -> conf.dependencies.add(project.dependencies.create(MDG_FRONTEND_GROUP + ':' + platformName)))
    }

    private static void setupPlugins(Project project, NamedDomainObjectProvider<Configuration> pluginConfiguration, Platform platform, boolean multiplatform) {
        if (platform !in Platform.STOCK_PLATFORMS)
            return // no stock plugins available for this platform

        // mdgPlugin "org.groovymc.modsdotgroovy.stock-plugins:<platform>"
        pluginConfiguration.configure(conf -> {
            if (multiplatform) conf.dependencies.add(project.dependencies.create(MDG_PLUGIN_GROUP + ':multiplatform'))

            conf.dependencies.add(project.dependencies.create(MDG_PLUGIN_GROUP + ':' + platform.name().toLowerCase(Locale.ROOT)))
        })
    }

    private static void setupTasks(Project project, SourceSet sourceSet, Platform platform, Provider<Configuration> root, Provider<Configuration> plugin, Provider<Configuration> frontend, Provider<FileCollection> masterModsDotGroovyFile, MDGConversionOptions conversionOptions) {
        // three steps:
        // 1. register a task to gather platform's details
        // 2. register a task to convert the mods.groovy file to a platform-specific file
        // 3. configure those tasks appropriately

        final TaskProvider<ProcessResources> processResourcesTask = project.tasks.named(sourceSet.processResourcesTaskName, ProcessResources)

        TaskProvider<? extends AbstractMDGConvertTask> convertTask = null

        switch (platform) {
            case Platform.FORGE:
                final gatherTask = project.tasks.register(MDGExtension.forSourceSetName(sourceSet.name, 'gatherForgePlatformDetails'), GatherForgePlatformDetails)
                convertTask = project.tasks.register(MDGExtension.forSourceSetName(sourceSet.name, 'modsDotGroovyToTomlForge'), ModsDotGroovyToToml) { ModsDotGroovyToToml task ->
                    task.dependsOn gatherTask
                    task.platform.set(Platform.FORGE)
                }
                processResourcesTask.configure { task ->
                    task.dependsOn convertTask
                    task.from(convertTask.get().output.get().asFile) { CopySpec spec ->
                        spec.into 'META-INF'
                    }
                }
                break
            case Platform.NEOFORGE:
                final gatherTask = project.tasks.register(MDGExtension.forSourceSetName(sourceSet.name, 'gatherNeoForgePlatformDetails'), GatherNeoForgePlatformDetails)
                convertTask = project.tasks.register(MDGExtension.forSourceSetName(sourceSet.name, 'modsDotGroovyToTomlNeoForge'), ModsDotGroovyToToml) { ModsDotGroovyToToml task ->
                    task.dependsOn gatherTask
                    task.platform.set(Platform.NEOFORGE)
                }
                processResourcesTask.configure { task ->
                    task.dependsOn convertTask
                    task.from(convertTask.get().output.get().asFile) { CopySpec spec ->
                        spec.into 'META-INF'
                    }
                }
                break
            case Platform.FABRIC:
                final gatherTask = project.tasks.register(MDGExtension.forSourceSetName(sourceSet.name, 'gatherFabricPlatformDetails'), GatherFabricPlatformDetails)
                convertTask = project.tasks.register(MDGExtension.forSourceSetName(sourceSet.name, 'modsDotGroovyToJsonFabric'), ModsDotGroovyToJson) { ModsDotGroovyToJson task ->
                    task.dependsOn gatherTask
                    task.outputName.set('fabric.mod.json')
                    task.platform.set(Platform.FABRIC)
                }
                processResourcesTask.configure { task ->
                    task.dependsOn convertTask
                    task.from(convertTask.get().output.get().asFile) { CopySpec spec ->
                        spec.into '.'
                    }
                }
                break
            case Platform.QUILT:
                final gatherTask = project.tasks.register(MDGExtension.forSourceSetName(sourceSet.name, 'gatherQuiltPlatformDetails'), GatherQuiltPlatformDetails)
                convertTask = project.tasks.register(MDGExtension.forSourceSetName(sourceSet.name, 'modsDotGroovyToJsonQuilt'), ModsDotGroovyToJson) { ModsDotGroovyToJson task ->
                    task.dependsOn gatherTask
                    task.outputName.set('quilt.mod.json')
                    task.platform.set(Platform.QUILT)
                }
                processResourcesTask.configure { task ->
                    task.dependsOn convertTask
                    task.from(convertTask.get().output.get().asFile) { CopySpec spec ->
                        spec.into '.'
                    }
                }
                break
            case Platform.SPIGOT:
                // todo: gatherSpigotPlatformDetails
                convertTask = project.tasks.register(MDGExtension.forSourceSetName(sourceSet.name, 'modsDotGroovyToYmlSpigot'), ModsDotGroovyToYml) { ModsDotGroovyToYml task ->
                    task.outputName.set('spigot.yml')
                    task.platform.set(Platform.SPIGOT)
                }
                processResourcesTask.configure { task ->
                    task.dependsOn convertTask
                    task.from(convertTask.get().output.get().asFile) { CopySpec spec ->
                        spec.into '.'
                    }
                }
        }

        if (convertTask != null) {
            convertTask.configure { task ->
                task.input.fileProvider(masterModsDotGroovyFile.map { it.singleFile })
                task.mdgRuntimeFiles.from(
                        root,
                        plugin,
                        frontend
                )
                task.conversionOptions.set(conversionOptions)
            }
        }
    }
}
