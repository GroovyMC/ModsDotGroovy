/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

package modsdotgroovy

import groovy.transform.CompileStatic

@CompileStatic
class AdaptedBuilder {
    /**
     * Points towards the implementation of the entrypoint, to be loaded by the adapter.
     */
    String value = null

    /**
     * The adapter to use when loading the entrypoint value.
     */
    String adapter = 'default'

    Map build() {
        Objects.requireNonNull(value, 'A value must be provided for an entrypoint with an adapter.')

        return ['adapter': adapter, 'value': value]
    }
}
