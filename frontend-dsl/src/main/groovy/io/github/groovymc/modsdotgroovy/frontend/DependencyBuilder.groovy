package io.github.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import io.github.groovymc.modsdotgroovy.core.ModsDotGroovyCore

@CompileStatic
@Log4j2(category = 'MDG - Frontend')
class DependencyBuilder implements PropertyInterceptor, MapClosureInterceptor {
    private final ModsDotGroovyCore core

    String modId

    boolean mandatory = true

    String versionRange

    String ordering = 'NONE'

    String side = 'BOTH'

    DependencyBuilder() {
        log.debug "new io.github.groovymc.modsdotgroovy.frontend.DependencyBuilder()"
        this.core = null
    }

    DependencyBuilder(final ModsDotGroovyCore core) {
        log.debug "new io.github.groovymc.modsdotgroovy.frontend.DependencyBuilder(core: $core)"
        this.core = core
    }
}
