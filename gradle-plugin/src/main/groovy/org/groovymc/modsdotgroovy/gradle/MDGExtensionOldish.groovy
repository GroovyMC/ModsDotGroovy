package org.groovymc.modsdotgroovy.gradle

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.groovymc.modsdotgroovy.core.Platform

import javax.inject.Inject

@CompileStatic
class MDGExtensionOldish {
    @PackageScope static final String NAME = 'modsDotGroovy'

    private final MapProperty<Platform, String[]> platforms
    private final SetProperty<String> environmentBlacklist
    private final Property<Boolean> setupDsl
    private final Property<Boolean> setupPlugins
    private final Property<Boolean> setupTasks
    //private final transient Project project

    @Inject
    MDGExtensionOldish(ObjectFactory objects, Project project) {
        this.platforms = objects.mapProperty(Platform, String[])
        this.platforms.convention(inferPlatforms(project))

        this.environmentBlacklist = objects.setProperty(String)
        this.environmentBlacklist.convention(['pass', 'password', 'token', 'key', 'secret'])

        this.setupDsl = objects.property(Boolean)
        this.setupDsl.convention(true)

        this.setupPlugins = objects.property(Boolean)
        this.setupPlugins.convention(true)

        this.setupTasks = objects.property(Boolean)
        this.setupTasks.convention(true)

        //this.project = project
    }

    MapProperty<Platform, String[]> getPlatforms() {
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

    void setPlatforms(Map<Platform, ?> platforms) {
        this.platforms.set(platforms.collectEntries { Platform platform, def paths ->
            if (paths instanceof String) return [platform, [paths] as String[]]
            else if (paths instanceof List<String>) return [platform, paths.toArray(String[]::new)]
            else throw new IllegalArgumentException("Invalid platform paths: $paths. Expected String or List<String>, got ${paths.getClass().getName()}")
        })
    }

//    void setPlatform(Platform platform) {
//        final String[] projectPath = [project.path]
//        this.platforms.set(Map.of(platform, projectPath))
//    }

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

    private static Map<Platform, String[]> inferPlatforms(Project project) {
        final String[] projectPath = [project.path]
        if (project.subprojects.isEmpty()) {
            // no subprojects, so assume a single platform
            if (project.plugins.findPlugin('net.minecraftforge.gradle')) return Map.of(Platform.FORGE, projectPath)
            else if (project.plugins.findPlugin('net.neoforged.gradle.userdev')) return Map.of(Platform.NEOFORGE, projectPath)
            else if (project.plugins.findPlugin('fabric-loom')) return Map.of(Platform.FABRIC, projectPath)
            else if (project.plugins.findPlugin('org.quiltmc.loom')) return Map.of(Platform.QUILT, projectPath)
        } else {
            // subprojects, so account for the possibility of multiple platforms and guess based on the names of the subprojects
            // (we can't check the subprojects' plugins from the root project because they haven't been applied yet)
            final Map<Platform, List<String>> platforms = [:]
            for (Project subproject in project.subprojects) {
                switch (subproject.name) {
                    case 'forge':
                        if (platforms.containsKey(Platform.FORGE)) platforms[Platform.FORGE] << subproject.path
                        else platforms[Platform.FORGE] = [subproject.path]
                        break
                    case 'neoforge':
                        if (platforms.containsKey(Platform.NEOFORGE)) platforms[Platform.NEOFORGE] << subproject.path
                        else platforms[Platform.NEOFORGE] = [subproject.path]
                        break
                    case 'fabric':
                        if (platforms.containsKey(Platform.FABRIC)) platforms[Platform.FABRIC] << subproject.path
                        else platforms[Platform.FABRIC] = [subproject.path]
                        break
                    case 'quilt':
                        if (platforms.containsKey(Platform.QUILT)) platforms[Platform.QUILT] << subproject.path
                        else platforms[Platform.QUILT] = [subproject.path]
                        break
                    case 'spigot':
                        if (platforms.containsKey(Platform.SPIGOT)) platforms[Platform.SPIGOT] << subproject.path
                        else platforms[Platform.SPIGOT] = [subproject.path]
                        break
                }
            }

            return platforms.collectEntries { Platform platform, List<String> paths -> [platform, paths.toArray(String[]::new)] }
        }

        return Map.of()
    }
}
