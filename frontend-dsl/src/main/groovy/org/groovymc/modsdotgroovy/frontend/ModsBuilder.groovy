package org.groovymc.modsdotgroovy.frontend

import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import static groovy.lang.Closure.DELEGATE_FIRST

@CompileStatic
@Log4j2(category = 'MDG - Frontend')
class ModsBuilder implements PropertyInterceptor, MapClosureInterceptor {
    private final ModsDotGroovyCore core

    void modInfo(@DelegatesTo(value = ModInfoBuilder, strategy = DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.ModInfoBuilder')
                 final Closure closure) {
        log.debug "modInfo(closure)"
        core.push('modInfo')
        final modInfoBuilder = new ModInfoBuilder(core)
        closure.resolveStrategy = DELEGATE_FIRST
        closure.delegate = modInfoBuilder
        closure.call(modInfoBuilder)
        core.pop()
    }

    @SuppressWarnings('GroovyUnusedDeclaration') // Used by the Groovy compiler for coercing an implicit `it` closure
    ModsBuilder() {
        log.debug "new org.groovymc.modsdotgroovy.frontend.ModsBuilder()"
        this.core = null
    }

    ModsBuilder(final ModsDotGroovyCore core) {
        log.debug "new org.groovymc.modsdotgroovy.frontend.ModsBuilder(core: $core)"
        this.core = core
    }
}
