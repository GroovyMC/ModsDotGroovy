package org.groovymc.modsdotgroovy.gradle

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurablePublishArtifact
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet
import org.groovymc.modsdotgroovy.core.Platform

import javax.inject.Inject

@CompileStatic
abstract class MDGExtension {
    private static final String EXPOSE_SOURCE_SET = 'shareModsDotGroovy'
    private static final String DEFAULT_MDG = "mods.groovy"

    final Property<Boolean> setupDsl
    final Property<Boolean> setupPlugins
    final Property<Boolean> setupTasks
    final ListProperty<Platform> platforms
    final MDGConversionOptions conversionOptions
    final Property<FileCollection> modsDotGroovyFile
    final Multiplatform multiplatform

    @PackageScope final Property<Boolean> multiplatformFlag
    private final SourceSet sourceSet

    private final Project project

    @Inject
    MDGExtension(SourceSet sourceSet, Project project) {
        this.project = project
        this.sourceSet = sourceSet

        this.setupDsl = project.objects.property(Boolean)
        this.setupPlugins = project.objects.property(Boolean)
        this.setupTasks = project.objects.property(Boolean)
        this.platforms = project.objects.listProperty(Platform)
        this.conversionOptions = project.objects.newInstance(MDGConversionOptions)
        this.modsDotGroovyFile = project.objects.property(FileCollection)

        this.platforms.convention(inferPlatforms(project))

        this.modsDotGroovyFile.convention(sourceSet.resources.matching {
            include DEFAULT_MDG
        })

        this.multiplatformFlag = project.objects.property(Boolean)
        this.multiplatformFlag.convention(false)

        this.multiplatform = new Multiplatform()

        this.setupPlugins.convention(false)
        this.setupTasks.convention(false)
        this.setupDsl.convention(false)
    }

    void conversionOptions(Action<MDGConversionOptions> action) {
        action.execute(conversionOptions)
    }

    private static List<Platform> inferPlatforms(Project project) {
        if (project.plugins.findPlugin('net.minecraftforge.gradle')) return List.of(Platform.FORGE)
        else if (project.plugins.findPlugin('net.neoforged.gradle.userdev')) return List.of(Platform.NEOFORGE)
        else if (project.plugins.findPlugin('fabric-loom')) return List.of(Platform.FABRIC)
        else if (project.plugins.findPlugin('org.quiltmc.loom')) return List.of(Platform.QUILT)
        else return List.of()
    }

    void multiplatform(Action<Multiplatform> action) {
        action.execute(multiplatform)
    }

    void enable() {
        setupDsl.set(true)
        setupPlugins.set(true)
        setupTasks.set(true)
    }

    void platform(Platform platform) {
        platforms.add(platform)
    }

    void setPlatform(Platform platform) {
        this.platforms.set(List.of(platform))
    }

    @CompileStatic
    class Multiplatform {
        void from(String projectPath) {
            from(projectPath, sourceSet.name)
        }

        void from(String projectPath, String sourceSetName) {
            var configurationName = forSourceSetName(sourceSetName, EXPOSE_SOURCE_SET)
            var consumingConfiguration = project.configurations.detachedConfiguration(
                    project.dependencies.project(path: projectPath, configuration: configurationName)
            )
            consumingConfiguration.canBeResolved = true
            consumingConfiguration.canBeConsumed = false
            getModsDotGroovyFile().set(consumingConfiguration)
            multiplatformFlag.set(true)
        }

        void expose(Object file, Action<? super ConfigurablePublishArtifact> configureAction) {
            setupDsl.set(false)
            setupPlugins.set(false)
            setupTasks.set(false)
            var configurationName = forSourceSetName(sourceSet.name, EXPOSE_SOURCE_SET)
            var exposingConfiguration = project.configurations.maybeCreate(configurationName)
            exposingConfiguration.canBeResolved = false
            exposingConfiguration.canBeConsumed = true

            project.artifacts {
                add(configurationName, file, configureAction)
            }
        }

        void expose(Object file) {
            expose(file, {})
        }

        void expose() {
            var file = sourceSet.resources.matching {
                include DEFAULT_MDG
            }
            expose(project.provider { file.singleFile })
        }
    }

    @PackageScope static String forSourceSetName(String sourceSetName, String name) {
        return sourceSetName == SourceSet.MAIN_SOURCE_SET_NAME ? name : "${sourceSetName}${name.capitalize()}"
    }
}
