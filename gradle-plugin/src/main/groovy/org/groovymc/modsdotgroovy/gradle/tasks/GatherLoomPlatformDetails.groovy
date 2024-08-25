package org.groovymc.modsdotgroovy.gradle.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
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

    private String calculatePlatformVersion() {
        return artifactIds.get().findResult {
            def component = it.componentIdentifier
            if (component instanceof ModuleComponentIdentifier) {
                def module = component as ModuleComponentIdentifier
                if (module.group == targetGroup.get() && module.module == targetModule.get()) {
                    return module.version
                }
            }
            return null
        }
    }

    GatherLoomPlatformDetails() {
        // query loom after project evaluate instead of during a task run for the sake of better caching
        this.minecraftVersion.convention(getMCVersionFromLoom())
    }

    @Override
    void run() throws IllegalStateException {
        @Nullable String minecraftVersion = this.minecraftVersion.getOrNull()
        @Nullable String platformVersion = this.platformVersion.getOrNull() ?: calculatePlatformVersion()

        this.writePlatformDetails(minecraftVersion, platformVersion)
    }

    @CompileDynamic
    private Provider<String> getMCVersionFromLoom() {
        return project.provider {
            final @Nullable def loomExtension = project.extensions.findByName('loom')
            if (loomExtension?.hasProperty('minecraftVersion')) {
                return (String) loomExtension.minecraftVersion.get()
            }
            return null
        }
    }
}
