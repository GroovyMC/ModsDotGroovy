/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

import groovy.transform.CompileStatic

@CompileStatic
enum DependencySide {
    CLIENT, SERVER, BOTH

    DependencySide() {}
}
