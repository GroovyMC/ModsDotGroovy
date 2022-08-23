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

package modsdotgroovy


import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.apache.groovy.lang.annotation.Incubating

import static groovy.lang.Closure.DELEGATE_FIRST

@Incubating
@CompileStatic
class DependenciesBuilder extends HashMap {

    private List<Dependency> dependencies = []

    private Platform platform

    DependenciesBuilder(Platform platform) {
        this.platform = platform
    }

    void mod(@DelegatesTo(value = Dependency, strategy = DELEGATE_FIRST)
             @ClosureParams(value = SimpleType, options = 'modsdotgroovy.Dependency') final Closure closure) {
        final dep = new Dependency()
        closure.delegate = dep
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(dep)
        dependencies << dep.copy()
    }

    void mod(final String modId,
             @DelegatesTo(value = Dependency, strategy = DELEGATE_FIRST)
             @ClosureParams(value = SimpleType, options = 'modsdotgroovy.Dependency') final Closure closure) {
        final dep = new Dependency()
        dep.modId = modId
        closure.delegate = dep
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(dep)
        dependencies << dep.copy()
    }

    void minecraft(@DelegatesTo(value = MinecraftDependency, strategy = DELEGATE_FIRST)
                   @ClosureParams(value = SimpleType, options = 'modsdotgroovy.MinecraftDependency') final Closure closure) {
        final minecraftDependency = new MinecraftDependency()
        closure.delegate = minecraftDependency
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(minecraftDependency)
        dependencies << minecraftDependency.copy()
    }

    void setMinecraft(final String versionRange) {
        final minecraftDependency = new MinecraftDependency()
        minecraftDependency.versionRange = versionRange
        dependencies << minecraftDependency.copy()
    }

    void setMinecraft(final NumberRange versionRange) {
        final minecraftDependency = new MinecraftDependency()
        final String mavenVersionRange = "[${versionRange.from},${versionRange.to})"
        minecraftDependency.versionRange = mavenVersionRange
        dependencies << minecraftDependency.copy()
    }

    // same as NumberRange but added this for better IDE support
    void setMinecraft(final List<BigDecimal> versionRange) {
        setMinecraft(new NumberRange(versionRange[0], versionRange[1]))
    }

    void forge(@DelegatesTo(value = ForgeDependency, strategy = DELEGATE_FIRST)
               @ClosureParams(value = SimpleType, options = 'modsdotgroovy.ForgeDependency') final Closure closure) {
        if (platform == Platform.FORGE) {
            final forgeDependency = new ForgeDependency()
            closure.delegate = forgeDependency
            closure.resolveStrategy = DELEGATE_FIRST
            closure.call(forgeDependency)
            dependencies << forgeDependency.copy()
        }
    }

    void setForge(final String versionRange) {
        if (platform == Platform.FORGE) {
            final forgeDependency = new ForgeDependency()
            forgeDependency.versionRange = versionRange
            dependencies << forgeDependency.copy()
        }
    }

    void quiltLoader(@DelegatesTo(value = QuiltLoaderDependency, strategy = DELEGATE_FIRST)
               @ClosureParams(value = SimpleType, options = 'modsdotgroovy.QuiltLoaderDependency') final Closure closure) {
        if (platform == Platform.QUILT) {
            final quiltLoaderDependency = new QuiltLoaderDependency()
            closure.delegate = quiltLoaderDependency
            closure.resolveStrategy = DELEGATE_FIRST
            closure.call(quiltLoaderDependency)
            dependencies << quiltLoaderDependency.copy()
        }
    }

    void setQuiltLoader(final String versionRange) {
        if (platform == Platform.QUILT) {
            final quiltLoaderDependency = new QuiltLoaderDependency()
            quiltLoaderDependency.versionRange = versionRange
            dependencies << quiltLoaderDependency.copy()
        }
    }

    void methodMissing(String name,
                       @DelegatesTo(value = Dependency, strategy = DELEGATE_FIRST)
                       @ClosureParams(value = SimpleType, options = 'modsdotgroovy.Dependency') final Closure closure) {
        mod(name, closure)
    }

    VersionRange versionRange(@DelegatesTo(value = VersionRange.SingleVersionData, strategy = DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'modsdotgroovy.VersionRange$SingleVersionData') final Closure closure) {
        VersionRange.SingleVersionData version = new VersionRange.SingleVersionData()
        closure.delegate = version
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(version)
        VersionRange range = new VersionRange()
        range.versions.add(version)
        return range
    }

    VersionRange versions(@DelegatesTo(value = VersionsBuilder, strategy = DELEGATE_FIRST)
                  @ClosureParams(value = SimpleType, options = 'modsdotgroovy.VersionsBuilder') final Closure closure) {
        VersionsBuilder builder = new VersionsBuilder()
        closure.delegate = builder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(builder)
        return builder.build()
    }

    List<Dependency> build() {
        this.each { key, value ->
            key = key as String

            final dependency = new Dependency()
            if (value instanceof Closure) {
                final closure = value as Closure
                dependency.modId = key as String
                closure.delegate = dependency
                closure.resolveStrategy = DELEGATE_FIRST
                closure.call(dependency)
            } else {
                // assume key and value are both strings
                dependency.modId = key as String
                dependency.versionRange = value as String
            }
            dependencies << dependency.copy()
        }
        return dependencies
    }
}
