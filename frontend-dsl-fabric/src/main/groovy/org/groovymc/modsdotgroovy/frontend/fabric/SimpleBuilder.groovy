package org.groovymc.modsdotgroovy.frontend.fabric

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

@CompileStatic
@Log4j2(category = 'MDG - Fabric Frontend')
class SimpleBuilder implements PropertyInterceptor {
    private final ModsDotGroovyCore core

    @SuppressWarnings('GroovyUnusedDeclaration') // Used by the Groovy compiler for coercing an implicit `it` closure
    SimpleBuilder() {
        log.debug "new org.groovymc.modsdotgroovy.frontend.fabric.SimpleBuilder()"
        this.core = null
    }

    SimpleBuilder(final ModsDotGroovyCore core) {
        log.debug "new org.groovymc.modsdotgroovy.frontend.fabric.SimpleBuilder(core: $core)"
        this.core = core
    }
}
