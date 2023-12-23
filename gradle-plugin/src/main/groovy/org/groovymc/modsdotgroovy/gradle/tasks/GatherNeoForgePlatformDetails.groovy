package org.groovymc.modsdotgroovy.gradle.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import net.neoforged.gradle.common.runs.run.RunImpl
import net.neoforged.gradle.common.runtime.definition.CommonRuntimeDefinition
import net.neoforged.gradle.common.util.TaskDependencyUtils
import net.neoforged.gradle.dsl.common.runs.run.Run
import net.neoforged.gradle.dsl.common.runtime.spec.Specification
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction
import org.jetbrains.annotations.Nullable

@CacheableTask
@CompileStatic
abstract class GatherNeoForgePlatformDetails extends AbstractGatherPlatformDetailsTask {

    @Override
    void run() throws IllegalStateException {
        @Nullable String minecraftVersion = this.minecraftVersion.getOrNull() ?: versions[0]
        @Nullable String platformVersion = this.platformVersion.getOrNull() ?: versions[1]

        if (minecraftVersion === null || platformVersion === null)
            throw new IllegalStateException("""
                Could not find Minecraft or NeoForge version in run configs for project \"${project.name}\".
                Try manually setting the minecraftVersion and platformVersion properties for this task.
            """.strip().stripIndent())

        this.writePlatformDetails(minecraftVersion, platformVersion)
    }

    @Memoized
    private @Nullable String[] getVersions() {
        @Nullable String mcVersion = null
        @Nullable String neoForgeVersion = null

        final NamedDomainObjectContainer<Run> runs = (NamedDomainObjectContainer<Run>) project.extensions.findByName('runs')
        for (run in runs) {
            if (run !instanceof RunImpl) continue

            for (sourceSet in run.modSources.get()) {
                final @Nullable runtimeDef = (CommonRuntimeDefinition) TaskDependencyUtils.findRuntimeDefinition(project, sourceSet).orElse(null)
                if (runtimeDef === null) continue

                final Specification spec = runtimeDef.specification
                mcVersion ?= spec.minecraftVersion
                neoForgeVersion ?= getForgeVersionFromSpec(spec)
            }
        }

        return new String[] { mcVersion, neoForgeVersion }
    }

    /**
     * Due to a NeoGradle 7 bug/limitation, we can't depend on the userdev module to do this statically as it'll cause
     * crashes for users when the versions don't precisely match up.
     */
    @CompileDynamic
    private static @Nullable String getForgeVersionFromSpec(Specification spec) {
        try {
            return spec.getForgeVersion()
        } catch (e) {
            return null
        }
    }
}
