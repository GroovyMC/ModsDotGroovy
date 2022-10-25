/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

package modsdotgroovy

class QuiltLoaderDependency extends Dependency {
    {
        modId = 'quilt_loader'
        mandatory = true
        ordering = DependencyOrdering.NONE
    }
}
