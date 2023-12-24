package org.groovymc.modsdotgroovy.gradle.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.CacheableTask
import org.jetbrains.annotations.Nullable

@CacheableTask
@CompileStatic
abstract class GatherFabricPlatformDetails extends AbstractGatherPlatformDetailsTask {

    GatherFabricPlatformDetails() {
        this.configurationName.convention('minecraft')

        // Todo: Use non-internal Loom API when available - https://github.com/FabricMC/fabric-loom/issues/982
        // this.minecraftVersion.convention(project.provider(this::getMCVersionFromLoom))
    }

    @Override
    void run() throws IllegalStateException {
        @Nullable String minecraftVersion = this.minecraftVersion.getOrNull()
        @Nullable String platformVersion = this.platformVersion.getOrNull()

        if (minecraftVersion === null) {
            final @Nullable def loomExtension = project.extensions.findByName('loom')
            final @Nullable String depVer = loomExtension === null ? null : getMCVersionFromLoom(loomExtension)
            if (depVer === null)
                throw new IllegalStateException("""
                    Could not find Minecraft dependency in configuration \"${this.configurationName.get()}\" for project \"${project.name}\".
                    Try manually setting the minecraftVersion and platformVersion properties for this task.
                """.strip().stripIndent())

            minecraftVersion = depVer
        }

        if (platformVersion === null) {
            final @Nullable Dependency dep = project.configurations.findByName('modImplementation')?.dependencies?.find { dep ->
                dep.name == 'fabric-loader' && dep.group == 'net.fabricmc'
            }
            if (dep === null)
                throw new IllegalStateException("""
                    Could not find Fabric Loader dependency for project \"${project.name}\".
                    Try manually setting the minecraftVersion and platformVersion properties for this task.
                """.strip().stripIndent())

            platformVersion = dep.version
        }

        this.writePlatformDetails(minecraftVersion, platformVersion)
    }

    @CompileDynamic
    private static @Nullable String getMCVersionFromLoom(@Nullable def loomExtension) {
        return loomExtension?.minecraftProvider?.minecraftVersion()
        // return loomExtension?.minecraftVersion
    }
}
