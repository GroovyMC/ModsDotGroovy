package org.groovymc.modsdotgroovy.frontend.neoforge

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.MapClosureInterceptor

@CompileStatic
@Log4j2(category = 'MDG - NeoForge Frontend')
class AccessTransformersBuilder extends DslBuilder implements MapClosureInterceptor {
    void accessTransformer(final String file) {
        log.debug "accessTransformer(config: $file)"
        core.push('accessTransformer')
        core.put('file', file)
        core.pop()
    }

    void accessTransformer(@DelegatesTo(value = AccessTransformerBuilder, strategy = Closure.DELEGATE_FIRST)
                           @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.AccessTransformerBuilder')
                           final Closure closure) {
        log.debug 'accessTransformer(closure)'
        core.push('accessTransformer')
        final mixinBuilder = new AccessTransformerBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = mixinBuilder
        closure.call(mixinBuilder)
        core.pop()
    }

    AccessTransformersBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
