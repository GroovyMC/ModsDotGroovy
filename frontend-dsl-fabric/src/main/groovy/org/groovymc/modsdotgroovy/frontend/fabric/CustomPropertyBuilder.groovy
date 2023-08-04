package org.groovymc.modsdotgroovy.frontend.fabric

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

@CompileStatic
@Log4j2(category = 'MDG - Fabric Frontend')
class CustomPropertyBuilder implements PropertyInterceptor {
    private final ModsDotGroovyCore core

    @CompileDynamic
    void property(final String name, final String value) {
        this."$name" = value
    }

    @SuppressWarnings('GroovyUnusedDeclaration') // Used by the Groovy compiler for coercing an implicit `it` closure
    CustomPropertyBuilder() {
        log.debug "new org.groovymc.modsdotgroovy.frontend.fabric.CustomPropertyBuilder()"
        this.core = null
    }

    CustomPropertyBuilder(final ModsDotGroovyCore core) {
        log.debug "new org.groovymc.modsdotgroovy.frontend.fabric.CustomPropertyBuilder(core: $core)"
        this.core = core
    }
}
