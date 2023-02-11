package io.github.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileStatic
import io.github.groovymc.modsdotgroovy.core.ModsDotGroovyCore

@CompileStatic
abstract class ModsDotGroovyFrontend {
    // todo: add `final` modifier?
    ModsDotGroovyCore core = new ModsDotGroovyCore()
}
