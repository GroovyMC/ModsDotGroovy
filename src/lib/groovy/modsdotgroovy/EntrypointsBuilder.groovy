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
        final oldVal = this.entrypoints[name]
        if (oldVal === null) {
            if (value instanceof List) {
                this.entrypoints[name] = value.toList()
            } else {
                this.entrypoints[name] = [value]
            }
        } else {
            (oldVal as List).add(value)
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

    /**
     * Adds client entrypoints.
     * @param entrypoints the entrypoints to add
     */
    void setClient(List<String> entrypoints) {
        propertyMissing(ModsDotGroovy.platform === Platform.QUILT ? 'client_init' : 'client', entrypoints)
    }

    /**
     * Adds server entrypoints.
     * @param entrypoints the entrypoints to add
     */
    void setServer(List<String> entrypoints) {
        propertyMissing(ModsDotGroovy.platform === Platform.QUILT ? 'server_init' : 'server', entrypoints)
    }

    /**
     * Adds main entrypoints.
     * @param entrypoints the entrypoints to add
     */
    void setMain(List<String> entrypoints) {
        propertyMissing(ModsDotGroovy.platform === Platform.QUILT ? 'init' : 'main', entrypoints)
    }

    /**
     * Adds a client entrypoint.
     * @param entrypoint the entrypoint to add
     */
    void setClient(String entrypoint) {
        setClient([entrypoint])
    }

    /**
     * Adds a server entrypoint.
     * @param entrypoint the entrypoint to add
     */
    void setServer(String entrypoint) {
        setServer([entrypoint])
    }

    /**
     * Adds a main entrypoint.
     * @param entrypoint the entrypoint to add
     */
    void setMain(String entrypoint) {
        setMain([entrypoint])
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
