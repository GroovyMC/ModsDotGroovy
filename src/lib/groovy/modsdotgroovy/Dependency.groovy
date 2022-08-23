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

@CompileStatic
class Dependency {
    /**
     * The ID of the mod this dependency is depending on.
     */
    String modId

    /**
     * Does this dependency have to exist? If not, ordering must also be specified.
     */
    boolean mandatory = true

    /**
     * A version range of the versions of the mod you're compatible with.
     */
    VersionRange versionRange

    /**
     * An ordering relationship for the dependency - BEFORE or AFTER required if the relationship is not mandatory
     */
    DependencyOrdering ordering = DependencyOrdering.NONE

    /**
     * Side this dependency is applied on - BOTH, CLIENT or SERVER
     */
    DependencySide side = DependencySide.BOTH

    Map asForgeMap() {
        final map = [:]
        map['mandatory'] = mandatory
        map['versionRange'] = versionRange.toForge()
        if (ordering !== null)
            map['ordering'] = ordering
        if (side !== null)
            map['side'] = side
        map['modId'] = modId
        return map
    }

    void setVersionRange(VersionRange range) {
        this.versionRange = range
    }

    void setVersionRange(String range) {
        this.versionRange = VersionRange.of(range)
    }

    void setVersionRange(List range) {
        this.versionRange = new VersionRange()
        this.versionRange.versions = range.collectMany {VersionRange.of(it as String).versions}
    }

    VersionRange getVersionRange() {
        return versionRange
    }

    Map asQuiltMap() {
        final map = [:]
        map['id'] = modId
        map['optional'] = !mandatory
        map['versions'] = versionRange.toQuilt()
        /*
        TODO: Last checked (20220821), the spec for side-specific dependencies on Quilt is up in the air. Once Quilt
        settles on something and implements it, this can be implemented correctly.
         */
        /*
        switch (side) {
            case DependencySide.BOTH:
                break
            case DependencySide.CLIENT:
                map['environment'] = 'client'
                break
            case DependencySide.SERVER:
                map['environment'] = 'dedicated_server'
                break
        }*/
        return map
    }

    Dependency copy() {
        if (!modId) throw new IllegalArgumentException("Missing modId for dependency")
        if (!versionRange) throw new IllegalArgumentException("Missing versionRange for dependency $modId")
        final dep = new Dependency()
        dep.modId = modId
        dep.mandatory = mandatory
        dep.versionRange = versionRange
        dep.ordering = ordering
        dep.side = side
        return dep
    }

}
