/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

package modsdotgroovy

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import static groovy.lang.Closure.DELEGATE_FIRST

@CompileStatic
class VersionsBuilder {
    private VersionRange versionRange

    void versionRange(@DelegatesTo(value = VersionRange.SingleVersionRange, strategy = DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'modsdotgroovy.VersionRange$SingleVersionRange') final Closure closure) {
        VersionRange.SingleVersionRange version = new VersionRange.SingleVersionRange()
        closure.delegate = version
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(version)
        versionRange.versions.add(version)
    }

    VersionRange build() {
        return versionRange
    }
}
