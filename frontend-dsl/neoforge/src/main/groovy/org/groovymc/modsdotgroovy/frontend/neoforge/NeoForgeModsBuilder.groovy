package org.groovymc.modsdotgroovy.frontend.neoforge

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.MapClosureInterceptor
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

import static groovy.lang.Closure.DELEGATE_FIRST

@CompileStatic
@Log4j2(category = 'MDG - NeoForge Frontend')
class NeoForgeModsBuilder extends DslBuilder implements PropertyInterceptor, MapClosureInterceptor {
    void modInfo(@DelegatesTo(value = NeoForgeModInfoBuilder, strategy = DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.NeoForgeModInfoBuilder')
                 final Closure closure) {
        log.debug 'modInfo(closure)'
        core.push('modInfo')
        final modInfoBuilder = new NeoForgeModInfoBuilder(core)
        closure.resolveStrategy = DELEGATE_FIRST
        closure.delegate = modInfoBuilder
        closure.call(modInfoBuilder)
        core.pop()
    }

    void modInfo(final String modId,
                 @DelegatesTo(value = NeoForgeModInfoBuilder, strategy = DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.NeoForgeModInfoBuilder')
                 final Closure closure) {
        log.debug 'modInfo(closure)'
        core.push('modInfo')
        final modInfoBuilder = new NeoForgeModInfoBuilder(core)
        core.put('modId', modId)
        closure.resolveStrategy = DELEGATE_FIRST
        closure.delegate = modInfoBuilder
        closure.call(modInfoBuilder)
        core.pop()
    }

    NeoForgeModsBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
