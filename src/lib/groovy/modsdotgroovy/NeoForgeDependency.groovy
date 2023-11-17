/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

package modsdotgroovy

import groovy.transform.CompileStatic

@CompileStatic
class NeoForgeDependency extends Dependency {
    {
        modId = 'neoforge'
        mandatory = true
        ordering = DependencyOrdering.NONE
    }
}
