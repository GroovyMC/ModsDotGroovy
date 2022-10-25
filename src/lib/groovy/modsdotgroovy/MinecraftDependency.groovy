/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

package modsdotgroovy

import groovy.transform.CompileStatic

@CompileStatic
class MinecraftDependency extends Dependency {
    {
        modId = 'minecraft'
        mandatory = true
        ordering = DependencyOrdering.NONE
    }
}
