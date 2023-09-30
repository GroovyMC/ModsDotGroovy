import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.frontend.MapClosureInterceptor
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor
import org.groovymc.modsdotgroovy.frontend.neoforge.NeoForgeModsBuilder
import org.groovymc.modsdotgroovy.frontend.neoforge.NeoForgeModInfoBuilder
import org.jetbrains.annotations.ApiStatus

@PackageScope
@CompileStatic
@ApiStatus.Experimental // NeoForged hasn't yet finalised their mods.toml spec at the time of writing
@Log4j2(category = 'MDG - NeoForge Frontend')
class NeoForgeModsDotGroovy extends ModsDotGroovy implements PropertyInterceptor, MapClosureInterceptor {
    /**@
     * Alias for <code>mods { modInfo {} }</code>
     * @param closure
     */
    @Override
    void mod(@DelegatesTo(value = NeoForgeModInfoBuilder, strategy = Closure.DELEGATE_FIRST)
             @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.ModInfoBuilder')
             final Closure closure) {
        mods { modInfo(closure) }
    }

    @Override
    void mods(@DelegatesTo(value = NeoForgeModsBuilder, strategy = Closure.DELEGATE_FIRST)
              @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.NeoForgeModsBuilder')
              final Closure closure) {
        log.debug "mods(closure)"
        core.push('mods')
        final modsBuilder = new NeoForgeModsBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = modsBuilder
        closure.call(modsBuilder)
        core.pop()
    }

    private NeoForgeModsDotGroovy(final Map<String, ?> environment) {
        super(environment)
    }

    static NeoForgeModsDotGroovy make(@DelegatesTo(value = NeoForgeModsDotGroovy, strategy = Closure.DELEGATE_FIRST)
                                      @ClosureParams(value = SimpleType, options = 'NeoForgeModsDotGroovy') final Closure closure) {
        return make(closure, [:])
    }

    static NeoForgeModsDotGroovy make(@DelegatesTo(value = NeoForgeModsDotGroovy, strategy = Closure.DELEGATE_FIRST)
                                      @ClosureParams(value = SimpleType, options = 'NeoForgeModsDotGroovy') final Closure closure,
                                      final Binding scriptBinding) {
        return make(closure, scriptBinding.variables)
    }

    static NeoForgeModsDotGroovy make(@DelegatesTo(value = NeoForgeModsDotGroovy, strategy = Closure.DELEGATE_FIRST)
                                      @ClosureParams(value = SimpleType, options = 'NeoForgeModsDotGroovy') final Closure closure,
                                      final Map<String, ?> environment) {
        final NeoForgeModsDotGroovy val = new NeoForgeModsDotGroovy(environment)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = val
        closure.call(val)
        return val
    }
}
