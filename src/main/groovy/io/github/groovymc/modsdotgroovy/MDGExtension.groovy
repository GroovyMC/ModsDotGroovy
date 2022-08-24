/*
 * MIT License
 *
 * Copyright (c) 2022 GroovyMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.groovymc.modsdotgroovy

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
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

    protected final Project project

    MDGExtension(final Project project) {
        this.project = project
        automaticConfiguration.set(true)
        platforms.set([])
    }

    String mdgDsl(String version = null) {
        version = version ?: getDslVersion().get()
        return "io.github.groovymc.modsdotgroovy:dsl:$version"
    }

    void platform(String name) {
        this.platforms.set([Platform.byName(name)])
    }

    void platforms(List<String> platforms) {
        this.platforms.set(platforms.collect {Platform.byName(it)})
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
    }
}
