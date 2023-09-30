package org.groovymc.modsdotgroovy.frontend.neoforge

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.MapClosureInterceptor
import org.groovymc.modsdotgroovy.frontend.ModInfoBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

import static groovy.lang.Closure.DELEGATE_FIRST

@CompileStatic
@Log4j2(category = 'MDG - NeoForge Frontend')
class NeoForgeModInfoBuilder extends ModInfoBuilder implements PropertyInterceptor, MapClosureInterceptor {
    void aliases(@DelegatesTo(value = AliasesBuilder, strategy = DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.AliasesBuilder')
                 final Closure closure) {
        log.debug 'aliases(closure)'
        core.push('aliases')
        final aliasesBuilder = new AliasesBuilder(core)
        closure.delegate = aliasesBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(aliasesBuilder)
        core.pop()
    }

    NeoForgeModInfoBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
