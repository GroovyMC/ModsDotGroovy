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
import org.gradle.api.provider.Property

@CompileStatic
abstract class MDGExtension {
    public static final String NAME = 'modsDotGroovy'
    abstract Property<String> getDslVersion()
    abstract Property<Boolean> getAutomaticConfiguration()
    abstract Property<Type> getType()

    MDGExtension() {
        automaticConfiguration.set(true)
        type.set(Type.FORGE)
    }

    String mdgDsl(String version = null) {
        version = version ?: getDslVersion().get()
        return "io.github.groovymc.modsdotgroovy:dsl:$version"
    }

    void type(String type) {
        switch (type.toLowerCase(Locale.ROOT)) {
            case 'quilt':
                this.type.set(Type.QUILT)
                break
            case 'forge':
                this.type.set(Type.FORGE)
                break
            default:
                throw new IllegalArgumentException("Unknown project type :$type")
        }
    }

    enum Type {
        QUILT {
            @Override
            String toString() {
                return "quilt"
            }
        },
        FORGE {
            @Override
            String toString() {
                return "forge"
            }
        }
    }
}
