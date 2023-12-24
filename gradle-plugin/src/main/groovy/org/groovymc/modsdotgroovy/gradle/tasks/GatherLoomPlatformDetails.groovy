package org.groovymc.modsdotgroovy.gradle.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.internal.component.external.model.ModuleComponentArtifactIdentifier
import org.jetbrains.annotations.Nullable

@CacheableTask
@CompileStatic
abstract class GatherLoomPlatformDetails extends AbstractGatherPlatformDetailsTask {
    @Input
    abstract ListProperty<ComponentArtifactIdentifier> getArtifactIds()

    @Input
    abstract Property<String> getTargetGroup()

    @Input
    abstract Property<String> getTargetModule()

    String calculatePlatformVersion() {
        return artifactIds.get().findResult {
            if (it instanceof ModuleComponentArtifactIdentifier) {
                if (it.componentIdentifier.group == targetGroup.get() && it.componentIdentifier.module == targetModule.get()) {
                    return it.componentIdentifier.version
                }
            }
            return null
        }
    }

    GatherLoomPlatformDetails() {
        // query loom after project evaluate instead of during a task run for the sake of better caching
        try {
            project.afterEvaluate {
                var mcVersion = getMCVersionFromLoom()
                if (mcVersion !== null) {
                    this.minecraftVersion.convention(mcVersion)
                }
            }
        } catch (Exception e) {
            if (project.state.executed) {
                var mcVersion = getMCVersionFromLoom()
                if (mcVersion !== null) {
                    this.minecraftVersion.convention(mcVersion)
                }
            } else {
                throw e
            }
        }
    }

    @Override
    void run() throws IllegalStateException {
        @Nullable String minecraftVersion = this.minecraftVersion.getOrNull()
        @Nullable String platformVersion = this.platformVersion.getOrNull()

        if (platformVersion == null) {
            platformVersion = calculatePlatformVersion()
        }

        this.writePlatformDetails(minecraftVersion, platformVersion)
    }

    @CompileDynamic
    private @Nullable String getMCVersionFromLoom() {
        // Todo: Use non-internal Loom API when available - https://github.com/FabricMC/fabric-loom/issues/982
        final @Nullable def loomExtension = project.extensions.findByName('loom')
        return loomExtension?.minecraftProvider?.minecraftVersion()
    }
}
