package org.groovymc.modsdotgroovy.frontend.quilt

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

@CompileStatic
@Log4j2(category = 'MDG - Quilt Frontend')
class ProvidesBuilder extends DslBuilder implements PropertyInterceptor {
    void mod(final String id) {
        log.debug "provides(id: ${id})"
        core.push('providesEntry')
        core.put('id', id)
        core.pop()
    }

    void mod(@DelegatesTo(value = ProvidesEntryBuilder, strategy = Closure.DELEGATE_FIRST)
             @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.ProvidesEntryBuilder')
             final Closure closure) {
        log.debug 'provides(closure)'
        core.push('providesEntry')
        final providesBuilder = new ProvidesEntryBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = providesBuilder
        closure.call(providesBuilder)
        core.pop()
    }

    void api(final String id) {
        mod(id)
    }

    void api(@DelegatesTo(value = ProvidesEntryBuilder, strategy = Closure.DELEGATE_FIRST)
             @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.ProvidesEntryBuilder')
             final Closure closure) {
        mod(closure)
    }

    ProvidesBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
