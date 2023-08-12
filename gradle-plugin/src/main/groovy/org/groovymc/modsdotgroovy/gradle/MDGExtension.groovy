/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

package org.groovymc.modsdotgroovy.gradle

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.groovymc.modsdotgroovy.core.Platform
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet

import javax.inject.Inject

@CompileStatic
abstract class MDGExtension {
    @PackageScope static final String NAME = 'modsDotGroovy'

    abstract Property<Boolean> getAutomaticConfiguration()
    abstract Property<SourceSet> getSource()
    abstract Property<MultiloaderConfiguration> getMultiloader()
    abstract MapProperty<String, Object> getArguments()
    abstract ListProperty<String> getCatalogs()
    abstract Property<Boolean> getSetupDsl()
    abstract Property<Boolean> getSetupPlugins()

    protected final Project project

    private final ObjectFactory objectFactory
    private final ListProperty<Platform> platforms = objectFactory.listProperty(Platform)
    private final ListProperty<String> environmentBlacklist = objectFactory.listProperty(String)

    @Inject
    MDGExtension(final Project project, final ObjectFactory objectFactory) {
        this.project = project
        this.objectFactory = objectFactory
        automaticConfiguration.convention(true)
        platforms.convention([Platform.FORGE])
        arguments.convention([:])
        catalogs.convention(['libs'])
        setupDsl.convention(true)
        setupPlugins.convention(true)
        environmentBlacklist.convention(['pass', 'password', 'token', 'key', 'secret'])
    }

    void setPlatform(final String name) {
        this.platforms.set([Platform.of(name)])
    }

    void setPlatform(final Platform platform) {
        this.platforms.set([platform])
    }

    ListProperty<Platform> getPlatforms() {
        return this.platforms
    }

    void setPlatforms(final List<String> platforms) {
        this.platforms.set(platforms.collect { Platform.of(it) })
    }

    void setPlatforms(final String[] platforms) {
        this.setPlatforms(platforms as List<String>)
    }

    ListProperty<String> getEnvironmentBlacklist() {
        return this.environmentBlacklist
    }

    void setEnvironmentBlacklist(final List<String> blacklist) {
        this.environmentBlacklist.set(blacklist)
    }

    void setEnvironmentBlacklist(final String[] blacklist) {
        this.setEnvironmentBlacklist(blacklist as List<String>)
    }

    void multiloader(@DelegatesTo(value = MultiloaderConfiguration, strategy = Closure.DELEGATE_FIRST)
                     @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.MDGExtension$MultiloaderConfiguration') final Closure closure) {
        final conf = new MultiloaderConfiguration()
        closure.delegate = conf
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call(conf)
        multiloader.set(conf)
    }

    static class MultiloaderConfiguration {
        Project common
        List<Project> forge = []
        List<Project> fabric = []
        List<Project> quilt = []
    }
}
