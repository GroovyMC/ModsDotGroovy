package org.groovymc.modsdotgroovy.gradle.tasks

import groovy.transform.CompileStatic
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction
import org.jetbrains.annotations.Nullable

@CacheableTask
@CompileStatic
abstract class GatherFabricPlatformDetails extends AbstractGatherPlatformDetailsTask {

    GatherFabricPlatformDetails() {
        this.configurationName.convention('minecraft')
    }

    @TaskAction
    void run() throws IllegalStateException {
        @Nullable String minecraftVersion = this.minecraftVersion.getOrNull()
        @Nullable String platformVersion = this.platformVersion.getOrNull()

        if (minecraftVersion === null) {
            final @Nullable Dependency dep = this.dependencies?.find()
            if (dep === null)
                throw new IllegalStateException("""
                    Could not find Minecraft dependency in configuration \"${this.configurationName.get()}\" for project \"${project.name}\".
                    Try manually setting the minecraftVersion and platformVersion properties.
                """.strip().stripIndent())

            minecraftVersion = dep.version
        }

        if (platformVersion === null) {
            final @Nullable Dependency dep = project.configurations.findByName('modImplementation')?.dependencies?.find { dep ->
                dep.name == 'fabric-loader' && dep.group == 'net.fabricmc'
            }
            if (dep === null)
                throw new IllegalStateException("""
                    Could not find Fabric Loader dependency for project \"${project.name}\".
                    Try manually setting the minecraftVersion and platformVersion properties.
                """.strip().stripIndent())

            platformVersion = dep.version
        }

        this.writePlatformDetails(minecraftVersion, platformVersion)
    }
}
