package io.github.groovymc.modsdotgroovy.frontend

import groovy.util.logging.Log4j2
import io.github.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import static groovy.lang.Closure.DELEGATE_ONLY

@CompileStatic
@Log4j2(category = 'MDG - Frontend')
class ModsBuilder implements PropertyInterceptor, MapClosureInterceptor {
    private final ModsDotGroovyCore core

    void modInfo(@DelegatesTo(value = ModInfoBuilder, strategy = DELEGATE_ONLY)
                 @ClosureParams(value = SimpleType, options = 'io.github.groovymc.modsdotgroovy.frontend.ModInfoBuilder')
                 final Closure closure) {
        log.debug "modInfo(closure)"
        core.push('modInfo')
        final modInfoBuilder = new ModInfoBuilder(core)
        closure.resolveStrategy = DELEGATE_ONLY
        closure.delegate = modInfoBuilder
        closure.call(modInfoBuilder)
        core.pop()
    }

    @SuppressWarnings('GroovyUnusedDeclaration') // Used by the Groovy compiler for coercing an implicit `it` closure
    ModsBuilder() {
        log.debug "new io.github.groovymc.modsdotgroovy.frontend.ModsBuilder()"
        this.core = null
    }

    ModsBuilder(final ModsDotGroovyCore core) {
        log.debug "new io.github.groovymc.modsdotgroovy.frontend.ModsBuilder(core: $core)"
        this.core = core
    }
}
