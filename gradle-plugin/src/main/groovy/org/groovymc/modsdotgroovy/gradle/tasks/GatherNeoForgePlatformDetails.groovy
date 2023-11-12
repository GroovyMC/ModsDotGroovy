package org.groovymc.modsdotgroovy.gradle.tasks

import groovy.transform.CompileStatic
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction
import org.jetbrains.annotations.Nullable

@CacheableTask
@CompileStatic
abstract class GatherNeoForgePlatformDetails extends AbstractGatherPlatformDetailsTask {

    GatherNeoForgePlatformDetails() {
        this.dependencyJars.setFrom(project.provider(Collections::emptyList))
    }

    @TaskAction
    void run() throws IllegalStateException {
        @Nullable String minecraftVersion = this.minecraftVersion.getOrNull()
        @Nullable String platformVersion = this.platformVersion.getOrNull()

        if (minecraftVersion === null || platformVersion === null) {
            final @Nullable def neoGradleUserdevExtension = project.extensions.findByName('userDev')

            final @Nullable Dependency dep = this.dependencies?.find {group == 'net.neoforged' && name == 'neoforge' }
            if (dep === null)
                throw new IllegalStateException("""
                    Could not find Minecraft dependency in configuration \"${this.configurationName.get()}\" for project \"${project.name}\".
                    Try manually setting the minecraftVersion and platformVersion properties for this task.
                """.strip().stripIndent())

            final String[] version = dep.version.split('.', 3)
            if (version.length === 1)
                throw new IllegalStateException("""
                    Could not find Forge version in Minecraft dependency version \"$version\" for project \"${project.name}\".
                    Try manually setting the minecraftVersion and platformVersion properties for this task.
                """.strip().stripIndent())

            minecraftVersion ?= "1.${version[0]}.${version[1]}".toString()
            platformVersion ?= dep.version
        }

        this.writePlatformDetails(minecraftVersion, platformVersion)
    }
}
