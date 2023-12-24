package org.groovymc.modsdotgroovy.gradle.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Nullable

@CacheableTask
@CompileStatic
abstract class GatherNeoForgePlatformDetails extends AbstractGatherPlatformDetailsTask {
    /*
    TODO: This is a string property because you cannot and should not pass configurations as inputs; however, neogradle
          needs a configuration to look in. The plan is to extract this logic entirely in the future, so as to support
          configuration caching, but it's not fun to figure out how to do that cleanly. We shouldn't be passing a
          configuration by name either, but... till we get configuration caching set up it'll have to do.
     */
    @Input
    @ApiStatus.Internal
    abstract Property<String> getCompileClasspathName()

    @Override
    void run() throws IllegalStateException {
        @Nullable String minecraftVersion = this.minecraftVersion.getOrNull()
        @Nullable String platformVersion = this.platformVersion.getOrNull()

        if (minecraftVersion === null || platformVersion === null) {
            var versions = calculateVersions()
            minecraftVersion ?= versions[0]
            platformVersion ?= versions[1]
        }

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

        try {
            final neoFormRuntime = project.extensions.getByName('userDevRuntime')
            final conf = project.configurations.getByName(compileClasspathName.get())
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
