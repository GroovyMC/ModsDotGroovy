package org.groovymc.modsdotgroovy.frontend.multiplatform.fabric

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.MapClosureInterceptor
import org.groovymc.modsdotgroovy.frontend.multiplatform.OnPlatform

@CompileStatic
@Log4j2(category = 'MDG - NeoForge Frontend')
class MixinsBuilder extends DslBuilder implements MapClosureInterceptor, OnPlatform {
    void mixin(final String config) {
        log.debug "mixin(config: $config)"
        core.push('mixin')
        core.put('config', config)
        core.pop()
    }

    void mixin(@DelegatesTo(value = MixinBuilder, strategy = Closure.DELEGATE_FIRST)
               @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.multiplatform.fabric.MixinBuilder')
               final Closure closure) {
        log.debug 'mixin(closure)'
        core.push('mixin')
        final mixinBuilder = new MixinBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = mixinBuilder
        closure.call(mixinBuilder)
        core.pop()
    }

    MixinsBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
