/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

package modsdotgroovy

class FabricLoaderDependency extends Dependency {
    {
        modId = 'fabricloader'
        mandatory = true
        ordering = DependencyOrdering.NONE
    }
}
