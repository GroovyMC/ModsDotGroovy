package org.groovymc.modsdotgroovy.gradle.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.SourceSet
import org.jetbrains.annotations.Nullable

@CacheableTask
@CompileStatic
abstract class GatherNeoForgePlatformDetails extends AbstractGatherPlatformDetailsTask {

    @Override
    void run() throws IllegalStateException {
        var versions = calculateVersions()

        @Nullable String minecraftVersion = this.minecraftVersion.getOrNull() ?: versions[0]
        @Nullable String platformVersion = this.platformVersion.getOrNull() ?: versions[1]

        if (minecraftVersion === null || platformVersion === null)
            throw new IllegalStateException("""
                Could not find Minecraft or NeoForge version in run configs for project \"${project.name}\".
                Try manually setting the minecraftVersion and platformVersion properties for this task.
            """.strip().stripIndent())

        this.writePlatformDetails(minecraftVersion, platformVersion)
    }

    /**
     * Due to gradle classloader limitations, this has to be done dynamically
     */
    @CompileDynamic
    private @Nullable String[] calculateVersions() {
        @Nullable String mcVersion = null
        @Nullable String neoForgeVersion = null

        final NamedDomainObjectContainer runs = project.extensions.findByName('runs') as NamedDomainObjectContainer
        for (run in runs) {
            try {
                final taskDependencyUtils = Class.forName('net.neoforged.gradle.common.util.TaskDependencyUtils', true, run.class.classLoader)

                for (SourceSet sourceSet in run.modSources.get()) {


                    final @Nullable runtimeDef = taskDependencyUtils.findRuntimeDefinition(project, sourceSet).orElse(null)
                    if (runtimeDef === null) continue

                    final spec = runtimeDef.specification

                    println spec

                    mcVersion ?= spec.minecraftVersion
                    neoForgeVersion ?= spec.forgeVersion
                }
            } catch (e) {
                throw e
            }
        }

        return new String[] { mcVersion, neoForgeVersion }
    }
}
