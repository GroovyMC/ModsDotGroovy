package io.github.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileStatic
import io.github.groovymc.modsdotgroovy.core.ModsDotGroovyCore

@CompileStatic
abstract class ModsDotGroovyFrontend {
    protected final ModsDotGroovyCore core = new ModsDotGroovyCore()
}
