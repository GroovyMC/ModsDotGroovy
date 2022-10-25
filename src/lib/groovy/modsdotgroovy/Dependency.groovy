/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

package modsdotgroovy

import groovy.transform.CompileStatic

@CompileStatic
class Dependency {
    /**
     * The ID of the mod this dependency is depending on.
     */
    String modId = null

    /**
     * Does this dependency have to exist? If not, ordering must also be specified.
     */
    boolean mandatory = true

    /**
     * A version range of the versions of the mod you're compatible with.
     */
    VersionRange versionRange = null

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

    /**
     * Set the version range using a string. The range provided can either be in the maven format ({@code "[1.0.0,)"}),
     * or in the SemVer format ({@code ">=1.0.0"}).
     */
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
        Objects.requireNonNull(modId, 'Missing modId for dependency')
        Objects.requireNonNull(versionRange, "Missing versionRange for dependency $modId")

        return new Dependency(modId: modId, mandatory: mandatory, versionRange: versionRange, ordering: ordering, side: side)
    }

}
