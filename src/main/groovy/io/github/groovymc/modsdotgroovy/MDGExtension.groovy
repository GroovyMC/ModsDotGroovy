/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

package io.github.groovymc.modsdotgroovy

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet

@CompileStatic
abstract class MDGExtension {
    public static final String NAME = 'modsDotGroovy'
    abstract Property<String> getDslVersion()
    abstract Property<Boolean> getAutomaticConfiguration()
    abstract ListProperty<Platform> getPlatforms()
    abstract Property<SourceSet> getSource()
    abstract Property<MultiloaderConfiguration> getMultiloader()
    abstract MapProperty<String, Object> getArguments()
    abstract ListProperty<String> getCatalogs()

    protected final Project project

    MDGExtension(final Project project) {
        this.project = project
        automaticConfiguration.set(true)
        platforms.set([Platform.FORGE])
        arguments.set([:])
        catalogs.set(['libs'])
    }

    String mdgDsl(String version = null) {
        version = version ?: getDslVersion().get()
        return "org.groovymc.modsdotgroovy:dsl:$version"
    }

    void platform(String name) {
        this.platforms.set([Platform.byName(name)])
    }

    void platforms(List<String> platforms) {
        this.platforms.set(platforms.collect {Platform.byName(it)})
    }

    void platforms(String[] platforms) {
        this.platforms(Arrays.asList(platforms))
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
        QUILT {
            @Override
            String toString() {
                return 'quilt'
            }
        },
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
        MULTILOADER {
            @Override
            String toString() {
                return 'multiloader'
            }
        }

        static Platform byName(String name) {
            switch (name.toLowerCase(Locale.ROOT)) {
                case 'quilt':
                    return QUILT
                case 'forge':
                    return FORGE
                case 'fabric':
                    return FABRIC
                case 'multiloader':
                    return MULTILOADER
                default:
                    throw new IllegalArgumentException("Unknown project platform :$name")
            }
        }
    }

    static class MultiloaderConfiguration {
        Project common
        List<Project> forge = []
        List<Project> quilt = []
        List<Project> fabric = []
    }
}
