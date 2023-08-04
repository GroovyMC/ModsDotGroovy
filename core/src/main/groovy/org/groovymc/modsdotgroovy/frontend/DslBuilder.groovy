package org.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileStatic
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore

@CompileStatic
abstract class DslBuilder {
    protected final ModsDotGroovyCore core

    @SuppressWarnings('GroovyUnusedDeclaration') // Used by the Groovy compiler for coercing an implicit `it` closure
    DslBuilder() {
        this.core = null
    }

    DslBuilder(final ModsDotGroovyCore core) {
        this.core = core
    }
}
