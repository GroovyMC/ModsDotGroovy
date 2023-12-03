package org.groovymc.modsdotgroovy.gradle.tasks

import groovy.transform.CompileStatic
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.jetbrains.annotations.Nullable

@CacheableTask
@CompileStatic
abstract class GatherForgePlatformDetails extends AbstractGatherPlatformDetailsTask {
    private final ConfigurableFileCollection dependencyJars = objectFactory.fileCollection()

    // defined here to trigger the task when dep versions change
    @InputFiles
    @Classpath
    protected ConfigurableFileCollection getDependencyJars() {
        return dependencyJars
    }

    GatherForgePlatformDetails() {
        this.configurationName.convention('minecraft')
        this.dependencyJars.from(providerFactory.provider(() -> configuration ?: []))
    }

    @TaskAction
    void run() throws IllegalStateException {
        @Nullable String minecraftVersion = this.minecraftVersion.getOrNull()
        @Nullable String platformVersion = this.platformVersion.getOrNull()

        if (minecraftVersion === null || platformVersion === null) {
            final @Nullable Dependency dep = this.dependencies?.find()
            if (dep === null)
                throw new IllegalStateException("""
                    Could not find Minecraft dependency in configuration \"${this.configurationName.get()}\" for project \"${project.name}\".
                    Try manually setting the minecraftVersion and platformVersion properties for this task.
                """.strip().stripIndent())

            final String[] version = dep.version.split('-')
            if (version.length === 1)
                throw new IllegalStateException("""
                    Could not find Forge version in Minecraft dependency version \"$version\" for project \"${project.name}\".
                    Try manually setting the minecraftVersion and platformVersion properties for this task.
                """.strip().stripIndent())

            minecraftVersion ?= version[0]
            platformVersion ?= version[1].split('_mapped_')[0]
        }

        this.writePlatformDetails(minecraftVersion, platformVersion)
    }

    private @Nullable DependencySet getDependencies() {
        return configuration?.dependencies
    }

    private @Nullable Configuration getConfiguration() {
        try {
            return project.configurations.named(configurationName.get()).getOrNull()
        } catch (UnknownConfigurationException ignored) {
            project.logger.warn "Warning: Configuration \"${configurationName.get()}\" not found for project \"${project.name}\""
            return null
        }
    }
}
