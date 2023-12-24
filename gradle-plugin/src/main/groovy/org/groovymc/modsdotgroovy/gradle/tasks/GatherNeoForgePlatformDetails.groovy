package org.groovymc.modsdotgroovy.gradle.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.jetbrains.annotations.Nullable

import javax.inject.Inject

@CacheableTask
@CompileStatic
abstract class GatherNeoForgePlatformDetails extends AbstractGatherPlatformDetailsTask {
    @Inject
    GatherNeoForgePlatformDetails(String configurationName) {
        try {
            project.afterEvaluate {
                var versions = calculateVersions(configurationName)
                this.minecraftVersion.convention(versions.map { it[0] })
                this.platformVersion.convention(versions.map { it[1] })
            }
        } catch (Exception e) {
            if (project.state.executed) {
                var versions = calculateVersions(configurationName)
                this.minecraftVersion.convention(versions.map { it[0] })
                this.platformVersion.convention(versions.map { it[1] })
            } else {
                throw e
            }
        }
    }

    /**
     * Due to gradle classloader limitations, this has to be done dynamically
     */
    @CompileDynamic
    private @Nullable Provider<String[]> calculateVersions(String configurationName) {
        return project.provider {
            @Nullable String mcVersion = null
            @Nullable String neoForgeVersion = null

            try {
                final neoFormRuntime = project.extensions.getByName('userDevRuntime')
                final conf = project.configurations.getByName(configurationName)
                final runtimeDef = neoFormRuntime.findIn(conf).first()
                if (runtimeDef !== null) {
                    final spec = runtimeDef.specification

                    mcVersion ?= spec.minecraftVersion
                    neoForgeVersion ?= spec.forgeVersion
                }
            } catch (ignored) {}

            return new String[] { mcVersion, neoForgeVersion }
        }
    }
}
