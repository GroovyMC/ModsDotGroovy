package org.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileStatic
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.core.Platform

@CompileStatic
abstract class ModsDotGroovyFrontend {
    final ModsDotGroovyCore core
    final Platform platform

    @SuppressWarnings('GroovyUnusedDeclaration') // Used by the Groovy compiler for coercing an implicit `it` closure
    ModsDotGroovyFrontend() {
        this([:])
    }

    ModsDotGroovyFrontend(final Map<String, ?> environment) {
        this.core = new ModsDotGroovyCore(environment)
        this.platform = environment.containsKey('environment')
                ? (environment.platform as Platform)
                : Platform.UNKNOWN
    }
}
