package io.github.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileStatic
import io.github.groovymc.modsdotgroovy.core.ModsDotGroovyCore

@CompileStatic
abstract class ModsDotGroovyFrontend {
    final ModsDotGroovyCore core

    ModsDotGroovyFrontend(final Map<String, ?> environment) {
        this.core = new ModsDotGroovyCore(environment)
    }
}
