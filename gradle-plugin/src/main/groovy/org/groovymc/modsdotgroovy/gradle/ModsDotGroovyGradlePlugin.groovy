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
import org.gradle.api.file.RegularFile
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.jvm.tasks.ProcessResources
import org.groovymc.modsdotgroovy.core.Platform
import org.groovymc.modsdotgroovy.gradle.tasks.*
import org.jetbrains.annotations.Nullable

@CompileStatic
final class ModsDotGroovyGradlePlugin implements Plugin<Project> {
    private static final String CONFIGURATION_NAME_ROOT = 'mdgRuntime'
    private static final String CONFIGURATION_NAME_PLUGIN = 'mdgPlugin'
    private static final String CONFIGURATION_NAME_FRONTEND = 'mdgFrontend'

    private static final String MDG_MAVEN_GROUP = 'org.groovymc.modsdotgroovy'
    private static final String MDG_FRONTEND_GROUP = MDG_MAVEN_GROUP + '.frontend-dsl'
    private static final String MDG_PLUGIN_GROUP = MDG_MAVEN_GROUP + '.stock-plugins'

    private MDGExtension mdgExtension

    @Override
    void apply(Project project) {
        // setup the extension to allow configuring mods.groovy settings in the build.gradle
        mdgExtension = project.extensions.create(MDGExtension.NAME, MDGExtension)

        // setup required plugins
        project.plugins.apply('java')
        project.plugins.apply('groovy')

        project.afterEvaluate {
            final Map<Platform, String[]> platforms = mdgExtension.platforms.get()
            boolean multiplatform = platforms.size() > 1

            if (!multiplatform) throw new UnsupportedOperationException("""
                There is no support for single-platform projects yet.
                Possible solutions:
                - Use a multiplatform project
                - Check for updates to ModsDotGroovy
            """.stripIndent().strip())

            // assume the project we're applying from is the root of a multiplatform project
            // there will be a subproject for each platform

            // let's set the path to the master mods.groovy file now
            final masterModsDotGroovyFile = project.layout.projectDirectory.file('src/main/resources/mods.groovy')

            platforms.each { Platform platform, String[] projectPaths ->
                for (projectPath in projectPaths) {
                    final Project subproject = project.project(projectPath)

                    // setup MDG dependency configurations
                    final rootConfiguration = subproject.configurations.register(CONFIGURATION_NAME_ROOT) { Configuration conf -> conf.tap {
                        canBeConsumed = false
                        attributes.tap {
                            attribute(Category.CATEGORY_ATTRIBUTE, subproject.objects.named(Category, Category.LIBRARY))
                            attribute(Bundling.BUNDLING_ATTRIBUTE, subproject.objects.named(Bundling, Bundling.EXTERNAL))
                            attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, subproject.objects.named(TargetJvmEnvironment, TargetJvmEnvironment.STANDARD_JVM))
                            attribute(Usage.USAGE_ATTRIBUTE, subproject.objects.named(Usage, Usage.JAVA_RUNTIME))
                            attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, subproject.objects.named(LibraryElements, LibraryElements.JAR))
                        }
                    }}
                    final frontendConfiguration = subproject.configurations.register(CONFIGURATION_NAME_FRONTEND) { Configuration conf ->
                        conf.extendsFrom rootConfiguration.get()
                    }
                    final pluginConfiguration = subproject.configurations.register(CONFIGURATION_NAME_PLUGIN) { Configuration conf ->
                        conf.extendsFrom rootConfiguration.get()
                    }

                    // setup required MDG repositories and dependencies for better IDE support
                    subproject.repositories.mavenCentral()
                    rootConfiguration.configure(conf -> conf.dependencies.add(subproject.dependencies.create('org.apache.groovy:groovy:4.0.16')))

                    // if asked, setup the mods.groovy DSL
                    if (mdgExtension.setupDsl.get())
                        setupDsl(subproject, frontendConfiguration, multiplatform)

                    // the plugins have to be done on a per-platform basis
                    // if asked, setup the mods.groovy plugins
                    if (mdgExtension.setupPlugins.get())
                        setupPlugins(subproject, pluginConfiguration, platform, multiplatform)

                    // now the hard part - the tasks
                    // if asked, setup the mods.groovy Gradle tasks
                    if (mdgExtension.setupTasks.get())
                        setupTasks(subproject, platform, multiplatform, masterModsDotGroovyFile, project)


                    // setup IDE support by adding the mdgFrontend configuration to the compileOnly configuration
                    subproject.configurations.named('compileOnly').configure(conf -> conf.extendsFrom(frontendConfiguration.get()))
                }
            }
        }
    }

    private void setupDsl(Project project, NamedDomainObjectProvider<Configuration> frontendConfiguration, /*Platform platform, */boolean multiplatform) {
//        if (platform !in Platform.STOCK_PLATFORMS) {
//            throw new UnsupportedOperationException("""
//                There is no stock frontend DSL available for $platform on this version of ModsDotGroovy.
//                Possible solutions:
//                - Check for updates to ModsDotGroovy
//                - Use a custom frontend by setting the 'setupDsl' property to false and adding an mdgFrontend dependency
//            """.stripIndent().strip())
//        }

        // mdgFrontend "org.groovymc.modsdotgroovy.frontend-dsl:<platform>"
        //final String platformName = multiplatform ? 'multiplatform' : platform.name().toLowerCase(Locale.ROOT)
        frontendConfiguration.configure(conf -> conf.dependencies.add(project.dependencies.create(MDG_FRONTEND_GROUP + ':multiplatform')))
    }

    private void setupPlugins(Project project, NamedDomainObjectProvider<Configuration> pluginConfiguration, Platform platform, boolean multiplatform) {
        if (platform !in Platform.STOCK_PLATFORMS)
            return // no stock plugins available for this platform

        // mdgPlugin "org.groovymc.modsdotgroovy.stock-plugins:<platform>"
        pluginConfiguration.configure(conf -> {
            if (multiplatform) conf.dependencies.add(project.dependencies.create(MDG_PLUGIN_GROUP + ':multiplatform'))

            conf.dependencies.add(project.dependencies.create(MDG_PLUGIN_GROUP + ':' + platform.name().toLowerCase(Locale.ROOT)))
        })
    }

    private void setupTasks(Project project, Platform platform, boolean multiplatform, RegularFile masterModsDotGroovyFile, Project masterProject) {
        // three steps:
        // 1. register a task to gather platform's details
        // 2. register a task to convert the mods.groovy file to a platform-specific file
        // 3. configure those tasks appropriately

        final TaskProvider<ProcessResources> processResourcesTask = project.tasks.named('processResources', ProcessResources)

        switch (platform) {
            case Platform.FORGE:
                project.tasks.register('gatherForgePlatformDetails', GatherForgePlatformDetails)
                final convertTask = project.tasks.register('modsDotGroovyToToml', ModsDotGroovyToToml) { ModsDotGroovyToToml task ->
                    if (multiplatform) task.input.set(masterModsDotGroovyFile)
                    task.dependsOn 'gatherForgePlatformDetails'
                    task.platform.set(Platform.FORGE)
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
                    if (multiplatform) task.input.set(masterModsDotGroovyFile)
                    task.dependsOn 'gatherNeoForgePlatformDetails'
                    task.platform.set(Platform.NEOFORGE)
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
                    if (multiplatform) task.input.set(masterModsDotGroovyFile)
                    task.dependsOn 'gatherFabricPlatformDetails'
                    task.outputName.set('fabric.mod.json')
                    task.platform.set(Platform.FABRIC)
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
                    if (multiplatform) task.input.set(masterModsDotGroovyFile)
                    task.dependsOn 'gatherQuiltPlatformDetails'
                    task.outputName.set('quilt.mod.json')
                    task.platform.set(Platform.QUILT)
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
                    if (multiplatform) task.input.set(masterModsDotGroovyFile)
                    task.outputName.set('spigot.yml')
                    task.platform.set(Platform.SPIGOT)
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
