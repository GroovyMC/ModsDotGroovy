/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

package modsdotgroovy

import groovy.transform.CompileStatic

@CompileStatic
class ForgeDependency extends Dependency {
    {
        modId = 'forge'
        mandatory = true
        ordering = DependencyOrdering.NONE
    }
}
