package org.groovymc.modsdotgroovy.frontend

import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.groovymc.modsdotgroovy.core.Platform

import static groovy.lang.Closure.DELEGATE_FIRST

@CompileStatic
@Log4j2(category = 'MDG - Multiplatform Frontend')
class ModsBuilder extends DslBuilder implements PropertyInterceptor, MapClosureInterceptor, OnPlatform {
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

    void modInfo(final String modId,
                 @DelegatesTo(value = ModInfoBuilder, strategy = DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.ModInfoBuilder') final Closure closure) {
        log.debug "modInfo(closure)"
        core.push('modInfo')
        final modInfoBuilder = new ModInfoBuilder(core)
        core.put('modId', modId)
        closure.resolveStrategy = DELEGATE_FIRST
        closure.delegate = modInfoBuilder
        closure.call(modInfoBuilder)
        core.pop()
    }

    ModsBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
