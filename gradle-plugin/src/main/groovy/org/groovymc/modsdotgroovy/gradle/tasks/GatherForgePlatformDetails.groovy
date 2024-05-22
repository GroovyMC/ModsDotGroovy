package org.groovymc.modsdotgroovy.gradle.tasks

import groovy.transform.CompileStatic
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.jetbrains.annotations.Nullable

@CacheableTask
@CompileStatic
abstract class GatherForgePlatformDetails extends AbstractGatherPlatformDetailsTask {
    @Input
    abstract ListProperty<ComponentArtifactIdentifier> getArtifactIds()

    String calculateCombinedVersion() {
        return artifactIds.get().findResult {
            def component = it.componentIdentifier
            if (component instanceof ModuleComponentIdentifier) {
                return (component as ModuleComponentIdentifier).version
            }
            return null
        }
    }

    @Override
    void run() throws IllegalStateException {
        @Nullable String minecraftVersion = this.minecraftVersion.getOrNull()
        @Nullable String platformVersion = this.platformVersion.getOrNull()

        if (minecraftVersion === null || platformVersion === null) {
            String combinedVersion = calculateCombinedVersion()
            final String[] version = combinedVersion.split('-')
            if (version.length === 1)
                throw new IllegalStateException("Could not find Forge version in Minecraft dependency version \"$version\". Try manually setting the minecraftVersion and platformVersion properties for this task.")

            minecraftVersion ?= version[0]
            platformVersion ?= version[1].split('_mapped_')[0]
        }

        this.writePlatformDetails(minecraftVersion, platformVersion)
    }
}
