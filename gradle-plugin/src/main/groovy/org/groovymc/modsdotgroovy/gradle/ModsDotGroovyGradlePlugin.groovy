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

    private MDGExtension mdgExtension

    @Lazy
    private Set<Platform> platforms = mdgExtension.platforms.get()

    @Override
    void apply(Project project) {
        // setup the extension to allow configuring mods.groovy settings in the build.gradle
        mdgExtension = project.extensions.create(MDGExtension.NAME, MDGExtension)

        // setup required plugins
        project.plugins.apply('java')
        project.plugins.apply('groovy')

        // setup MDG dependency configurations
        final rootConfiguration = project.configurations.register(CONFIGURATION_NAME_ROOT) { Configuration conf -> conf.tap {
            canBeConsumed = false
            attributes.tap {
                attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category, Category.LIBRARY))
                attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling, Bundling.EXTERNAL))
                attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, project.objects.named(TargetJvmEnvironment, TargetJvmEnvironment.STANDARD_JVM))
                attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage, Usage.JAVA_RUNTIME))
                attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements, LibraryElements.JAR))
            }
        }}
        final frontendConfiguration = project.configurations.register(CONFIGURATION_NAME_FRONTEND) { Configuration conf ->
            conf.extendsFrom rootConfiguration.get()
        }
        final pluginConfiguration = project.configurations.register(CONFIGURATION_NAME_PLUGIN) { Configuration conf ->
            conf.extendsFrom rootConfiguration.get()
        }

        // setup required MDG repositories and dependencies for better IDE support
        project.repositories.mavenCentral()
        rootConfiguration.configure(conf -> conf.dependencies.add(project.dependencies.create('org.apache.groovy:groovy:4.0.16')))

        project.afterEvaluate {
            // if asked, setup the mods.groovy DSL
            if (mdgExtension.setupDsl.get())
                setupDsl(project, frontendConfiguration)

            // if asked, setup the mods.groovy plugins
            if (mdgExtension.setupPlugins.get())
                setupPlugins(project, pluginConfiguration)

            // if asked, setup the mods.groovy Gradle tasks
            if (mdgExtension.setupTasks.get())
                setupTasks(project)

            // setup IDE support by adding the mdgFrontend configuration to the compileOnly configuration
            project.configurations.named('compileOnly').configure(conf -> conf.extendsFrom(frontendConfiguration.get()))
        }
    }

    private void setupDsl(Project project, NamedDomainObjectProvider<Configuration> frontendConfiguration) {
        // for now, assume max one platform
        // todo: multiplatform support
        final Platform platform = platforms.first()
        if (platform !in Platform.STOCK_PLATFORMS) {
            throw new UnsupportedOperationException("""
                There is no stock frontend DSL available for $platforms on this version of ModsDotGroovy.
                Possible solutions:
                - Check for updates to ModsDotGroovy
                - Use a custom frontend by setting the 'setupDsl' property to false and adding an mdgFrontend dependency
            """.stripIndent().strip())
        }

        // mdgFrontend "org.groovymc.modsdotgroovy.frontend-dsl:<platform>"
        frontendConfiguration.configure(conf -> conf.dependencies.add(project.dependencies.create(MDG_FRONTEND_GROUP + ':' + platform.name().toLowerCase(Locale.ROOT))))
    }

    private void setupPlugins(Project project, NamedDomainObjectProvider<Configuration> pluginConfiguration) {
        // for now, assume max one platform
        // todo: multiplatform support
        final Platform platform = platforms.first()
        if (platform !in Platform.STOCK_PLATFORMS)
            return // no stock plugins available for this platform

        // mdgPlugin "org.groovymc.modsdotgroovy.stock-plugins:<platform>"
        pluginConfiguration.configure(conf -> conf.dependencies.add(project.dependencies.create(MDG_PLUGIN_GROUP + ':' + platform.name().toLowerCase(Locale.ROOT))))
    }

    private void setupTasks(Project project) {
        // for now, assume max one platform
        // todo: multiplatform support
        final Platform platform = platforms.first()

        // three steps:
        // 1. register a task to gather platform's details
        // 2. register a task to convert the mods.groovy file to a platform-specific file
        // 3. configure those tasks appropriately

        final TaskProvider<ProcessResources> processResourcesTask = project.tasks.named('processResources', ProcessResources)

        switch (platform) {
            case Platform.FORGE:
                project.tasks.register('gatherForgePlatformDetails', GatherForgePlatformDetails)
                final convertTask = project.tasks.register('modsDotGroovyToToml', ModsDotGroovyToToml) { ModsDotGroovyToToml task ->
                    task.dependsOn 'gatherForgePlatformDetails'
                }
                processResourcesTask.configure { task ->
                    task.dependsOn 'modsDotGroovyToToml'
                    task.from(convertTask.get().output.get().asFile) { CopySpec spec ->
                        spec.into 'META-INF'
                    }
                }
                break
            case Platform.NEOFORGE:
                project.tasks.register('gatherNeoForgePlatformDetails', GatherNeoForgePlatformDetails)
                final convertTask = project.tasks.register('modsDotGroovyToToml', ModsDotGroovyToToml) { ModsDotGroovyToToml task ->
                    task.dependsOn 'gatherNeoForgePlatformDetails'
                }
                processResourcesTask.configure { task ->
                    task.dependsOn 'modsDotGroovyToToml'
                    task.from(convertTask.get().output.get().asFile) { CopySpec spec ->
                        spec.into 'META-INF'
                    }
                }
                break
            case Platform.FABRIC:
                project.tasks.register('gatherFabricPlatformDetails', GatherFabricPlatformDetails)
                final convertTask = project.tasks.register('modsDotGroovyToJson', ModsDotGroovyToJson) { ModsDotGroovyToJson task ->
                    task.dependsOn 'gatherFabricPlatformDetails'
                    task.outputName.set('fabric.mod.json')
                }
                processResourcesTask.configure { task ->
                    task.dependsOn 'modsDotGroovyToJson'
                    task.from(convertTask.get().output.get().asFile) { CopySpec spec ->
                        spec.into '.'
                    }
                }
                break
            case Platform.QUILT:
                project.tasks.register('gatherQuiltPlatformDetails', GatherQuiltPlatformDetails)
                final convertTask = project.tasks.register('modsDotGroovyToJson', ModsDotGroovyToJson) { ModsDotGroovyToJson task ->
                    task.dependsOn 'gatherQuiltPlatformDetails'
                    task.outputName.set('quilt.mod.json')
                }
                processResourcesTask.configure { task ->
                    task.dependsOn 'modsDotGroovyToJson'
                    task.from(convertTask.get().output.get().asFile) { CopySpec spec ->
                        spec.into '.'
                    }
                }
                break
            case Platform.SPIGOT:
                // todo: gatherSpigotPlatformDetails
                final convertTask = project.tasks.register('modsDotGroovyToYml', ModsDotGroovyToYml) { ModsDotGroovyToYml task ->
                    task.outputName.set('spigot.yml')
                }
                processResourcesTask.configure { task ->
                    task.dependsOn 'modsDotGroovyToYml'
                    task.from(convertTask.get().output.get().asFile) { CopySpec spec ->
                        spec.into '.'
                    }
                }
        }

        project.tasks.withType(AbstractMDGConvertTask).configureEach { task ->
            task.mdgRuntimeFiles.from(
                    project.configurations.named(CONFIGURATION_NAME_ROOT),
                    project.configurations.named(CONFIGURATION_NAME_PLUGIN),
                    project.configurations.named(CONFIGURATION_NAME_FRONTEND)
            )
        }
    }
}
