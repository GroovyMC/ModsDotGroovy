package org.groovymc.modsdotgroovy.gradle

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.groovymc.modsdotgroovy.core.Platform

import javax.inject.Inject

@CompileStatic
class MDGExtension {
    @PackageScope static final String NAME = 'modsDotGroovy'

    private final SetProperty<Platform> platforms
    private final SetProperty<String> environmentBlacklist
    private final Property<Boolean> setupDsl
    private final Property<Boolean> setupPlugins
    private final Property<Boolean> setupTasks

    @Inject
    MDGExtension(ObjectFactory objects, Project project) {
        this.platforms = objects.setProperty(Platform)
        this.platforms.convention(inferPlatforms(project.plugins))

        this.environmentBlacklist = objects.setProperty(String)
        this.environmentBlacklist.convention(['pass', 'password', 'token', 'key', 'secret'])

        this.setupDsl = objects.property(Boolean)
        this.setupDsl.convention(true)

        this.setupPlugins = objects.property(Boolean)
        this.setupPlugins.convention(true)

        this.setupTasks = objects.property(Boolean)
        this.setupTasks.convention(true)
    }

    SetProperty<Platform> getPlatforms() {
        return platforms
    }

    SetProperty<String> getEnvironmentBlacklist() {
        return environmentBlacklist
    }

    Property<Boolean> getSetupDsl() {
        return setupDsl
    }

    Property<Boolean> getSetupPlugins() {
        return setupPlugins
    }

    Property<Boolean> getSetupTasks() {
        return setupTasks
    }

    void setPlatforms(Platform[] platforms) {
        this.platforms.set(Set.of(platforms))
    }

    void setPlatform(Platform platform) {
        this.platforms.set(Set.of(platform))
    }

    void setEnvironmentBlacklist(String... blacklist) {
        this.environmentBlacklist.set(Set.of(blacklist))
    }

    void setSetupDsl(boolean setupDsl) {
        this.setupDsl.set(setupDsl)
    }

    void setSetupPlugins(boolean setupPlugins) {
        this.setupPlugins.set(setupPlugins)
    }

    void setSetupTasks(boolean setupTasks) {
        this.setupTasks.set(setupTasks)
    }

    private static Set<Platform> inferPlatforms(PluginContainer plugins) {
        // check what plugins are applied to the project to determine what platforms are being targeted
        // to start, let's assume no subprojects.
        // todo: multiplatform support with subprojects
        final Set<Platform> platforms = []
        if (plugins.findPlugin('net.minecraftforge.gradle')) platforms << Platform.FORGE
        else if (plugins.findPlugin('net.neoforged.gradle.userdev')) platforms << Platform.NEOFORGE
        else if (plugins.findPlugin('fabric-loom')) platforms << Platform.FABRIC
        else if (plugins.findPlugin('org.quiltmc.loom')) platforms << Platform.QUILT
        return platforms
    }
}
