import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder

@CompileStatic
@Log4j2(category = 'MDG - Fabric Frontend')
class MixinsBuilder extends DslBuilder {
    void mixin(final String config, final Environment environment = null) {
        log.debug "mixins(config: $config)"
        core.push('mixin')
        core.put('config', config)
        if (environment !== null) {
            core.put('environment', environment)
        }
        core.pop()
    }

    void mixin(@DelegatesTo(value = MixinBuilder, strategy = Closure.DELEGATE_FIRST)
               @ClosureParams(value = SimpleType, options = 'MixinBuilder')
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
