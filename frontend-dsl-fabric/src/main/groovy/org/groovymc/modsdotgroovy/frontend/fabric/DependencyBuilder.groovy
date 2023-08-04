package org.groovymc.modsdotgroovy.frontend.fabric

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

@CompileStatic
@Log4j2(category = 'MDG - Fabric Frontend')
class DependencyBuilder implements PropertyInterceptor {
    private final ModsDotGroovyCore core

    String modId

    def versionRange

    @SuppressWarnings('GroovyUnusedDeclaration') // Used by the Groovy compiler for coercing an implicit `it` closure
    DependencyBuilder() {
        log.debug "new org.groovymc.modsdotgroovy.frontend.fabric.DependencyBuilder()"
        this.core = null
    }

    DependencyBuilder(final ModsDotGroovyCore core) {
        log.debug "new org.groovymc.modsdotgroovy.frontend.fabric.DependencyBuilder(core: $core)"
        this.core = core
    }
}
