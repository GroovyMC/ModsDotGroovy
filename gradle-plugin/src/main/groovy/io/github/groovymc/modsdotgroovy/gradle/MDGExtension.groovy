/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

package io.github.groovymc.modsdotgroovy.gradle

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet

import javax.inject.Inject

@CompileStatic
abstract class MDGExtension {
    public static final String NAME = 'modsDotGroovy'

    abstract Property<Boolean> getAutomaticConfiguration()
    abstract Property<SourceSet> getSource()
    abstract Property<MultiloaderConfiguration> getMultiloader()
    abstract MapProperty<String, Object> getArguments()
    abstract ListProperty<String> getCatalogs()
    abstract Property<Boolean> getSetupDsl()

    protected final Project project

    private final ObjectFactory objectFactory
    private final ListProperty<Platform> platforms = objectFactory.listProperty(Platform)

    @Inject
    MDGExtension(final Project project, final ObjectFactory objectFactory) {
        this.project = project
        this.objectFactory = objectFactory
        automaticConfiguration.set(true)
        platforms.set([Platform.FORGE])
        arguments.set([:])
        catalogs.set(['libs'])
        setupDsl.set(true)
    }

    void setPlatform(final String name) {
        this.platforms.set([Platform.byName(name)])
    }

    void setPlatform(final Platform platform) {
        this.platforms.set([platform])
    }

    ListProperty<Platform> getPlatforms() {
        return this.platforms
    }

    void setPlatforms(final List<String> platforms) {
        this.platforms.set(platforms.collect {Platform.byName(it)})
    }

    void setPlatforms(final String[] platforms) {
        this.setPlatforms(platforms as List<String>)
    }

    void multiloader(@DelegatesTo(value = MultiloaderConfiguration, strategy = Closure.DELEGATE_FIRST)
                     @ClosureParams(value = SimpleType, options = 'io.github.groovymc.modsdotgroovy.MDGExtension$MultiloaderConfiguration') final Closure closure) {
        final conf = new MultiloaderConfiguration()
        closure.delegate = conf
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call(conf)
        multiloader.set(conf)
    }

    enum Platform {
        FORGE {
            @Override
            String toString() {
                return 'forge'
            }
        },
        FABRIC {
            @Override
            String toString() {
                return 'fabric'
            }
        },
        QUILT {
            @Override
            String toString() {
                return 'quilt'
            }
        },
        MULTILOADER {
            @Override
            String toString() {
                return 'multiloader'
            }
        }

        Platform() {}

        static Platform byName(String name) {
            switch (name.toLowerCase(Locale.ROOT)) {
                case 'forge': return FORGE
                case 'fabric': return FABRIC
                case 'quilt': return QUILT
                case 'multiloader': return MULTILOADER
                default: throw new IllegalArgumentException("Unknown project platform: $name")
            }
        }
    }

    static class MultiloaderConfiguration {
        Project common
        List<Project> forge = []
        List<Project> fabric = []
        List<Project> quilt = []
    }
}
