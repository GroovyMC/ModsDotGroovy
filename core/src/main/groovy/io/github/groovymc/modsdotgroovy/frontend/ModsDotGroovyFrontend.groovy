package io.github.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileStatic
import io.github.groovymc.modsdotgroovy.core.ModsDotGroovyCore

@CompileStatic
abstract class ModsDotGroovyFrontend {
    final ModsDotGroovyCore core = new ModsDotGroovyCore()
}
