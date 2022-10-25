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
class EntrypointsBuilder {
    Map entrypoints = [:]

    void propertyMissing(String name, value) {
        if (value instanceof List) {
            entrypoints[name] = value.toList()
        } else {
            entrypoints[name] = [value]
        }
    }

    /**
     * Adds an entrypoint.
     * @param name The name of the entrypoint to add.
     * @param args Either the single value of the entrypoint or a list of values.
     */
    void entrypoint(String name, args) {
        propertyMissing(name, args)
    }

    Map adapted(@DelegatesTo(value = AdaptedBuilder, strategy = DELEGATE_FIRST)
                @ClosureParams(value = SimpleType, options = 'modsdotgroovy.AdaptedBuilder') final Closure closure) {
        final AdaptedBuilder builder = new AdaptedBuilder()
        closure.delegate = builder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(builder)
        return builder.build()
    }
}
