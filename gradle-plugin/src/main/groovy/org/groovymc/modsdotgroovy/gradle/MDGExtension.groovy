package org.groovymc.modsdotgroovy.gradle

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.groovymc.modsdotgroovy.core.Platform

import javax.inject.Inject

@CompileStatic
class MDGExtension {
    @PackageScope static final String NAME = 'modsDotGroovy'

    private final Property<Boolean> multiplatform
    private final SetProperty<Platform> platforms
    private final SetProperty<String> environmentBlacklist
    private final Property<Boolean> setupDsl
    private final Property<Boolean> setupPlugins
    private final Property<Boolean> setupTasks

    @Inject
    MDGExtension(ObjectFactory objects, Project project) {
        this.multiplatform = objects.property(Boolean)
        this.multiplatform.convention(inferPlatforms(project).size() > 1)

        this.platforms = objects.setProperty(Platform)
        this.platforms.convention(inferPlatforms(project))

        this.environmentBlacklist = objects.setProperty(String)
        this.environmentBlacklist.convention(['pass', 'password', 'token', 'key', 'secret'])

        this.setupDsl = objects.property(Boolean)
        this.setupDsl.convention(true)

        this.setupPlugins = objects.property(Boolean)
        this.setupPlugins.convention(true)

        this.setupTasks = objects.property(Boolean)
        this.setupTasks.convention(true)
    }

    Property<Boolean> getMultiplatform() {
        return multiplatform
    }

    SetProperty<Platform> getPlatforms() {
        println platforms.get()
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

    void setMultiplatform(boolean multiplatform) {
        this.multiplatform.set(multiplatform)
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

    private static Set<Platform> inferPlatforms(Project project) {
//        if (project.subprojects.isEmpty()) {
            // no subprojects, so assume a single platform
            if (project.plugins.findPlugin('net.minecraftforge.gradle')) return Set.of(Platform.FORGE)
            else if (project.plugins.findPlugin('net.neoforged.gradle.userdev')) return Set.of(Platform.NEOFORGE)
            else if (project.plugins.findPlugin('fabric-loom')) return Set.of(Platform.FABRIC)
            else if (project.plugins.findPlugin('org.quiltmc.loom')) return Set.of(Platform.QUILT)
//        } else {
//            // subprojects, so account for the possibility of multiple platforms
//            final platforms = project.subprojects.collect(new HashSet<>()) { subproject ->
//                if (subproject.plugins.findPlugin('net.minecraftforge.gradle')) return Platform.FORGE
//                else if (subproject.plugins.findPlugin('net.neoforged.gradle.userdev')) return Platform.NEOFORGE
//                else if (subproject.plugins.findPlugin('fabric-loom')) return Platform.FABRIC
//                else if (subproject.plugins.findPlugin('org.quiltmc.loom')) return Platform.QUILT
//                else return null // collect() expects all iterations to return something
//            }
//
//            platforms.remove(null)
//
//            if (!platforms.isEmpty())
//                return (Set<Platform>) platforms
//        }

        return Set.of(Platform.UNKNOWN)
    }
}
