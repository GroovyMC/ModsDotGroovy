package org.groovymc.modsdotgroovy.frontend.neoforge

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.frontend.MapClosureInterceptor
import org.groovymc.modsdotgroovy.frontend.ModsDotGroovyFrontend
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor
import org.groovymc.modsdotgroovy.frontend.neoforge.AccessTransformersBuilder
import org.groovymc.modsdotgroovy.frontend.neoforge.MixinsBuilder
import org.groovymc.modsdotgroovy.frontend.neoforge.ModInfoBuilder
import org.groovymc.modsdotgroovy.frontend.neoforge.ModsBuilder
import org.groovymc.rootpackagetransformer.RootPackage
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Nullable

import static groovy.lang.Closure.DELEGATE_FIRST

@PackageScope
@CompileStatic
@ApiStatus.Experimental // NeoForged hasn't yet finalised their mods.toml spec at the time of writing
@Log4j2(category = 'MDG - NeoForge Frontend')
@RootPackage
class NeoForgeModsDotGroovy extends ModsDotGroovyFrontend implements PropertyInterceptor, MapClosureInterceptor {
    /**@
     * The name of the mod loader type to load - for regular Java FML @Mod mods it should be {@code javafml}.
     * For GroovyModLoader @GMod mods it should be {@code gml}.
     */
    String modLoader = 'javafml'

    /**@
     * A version range to match for the {@link #setModLoader(java.lang.String)}.
     */
    def loaderVersion = '[1,)'

    /**@
     * The license for your mod. This is mandatory metadata and allows for easier comprehension of your redistributive properties.<br>
     * Review your options at <a href="https://choosealicense.com/">https://choosealicense.com/</a>.<br>
     * All rights reserved is the default copyright stance, and is thus the default here.
     */
    String license = 'All Rights Reserved'

    /**@
     * A URL to refer people to when problems occur with this mod.
     */
    @Nullable String issueTrackerUrl = null

    boolean showAsResourcePack = false
    boolean showAsDataPack = false

    List<String> services

    List<String> accessTransformers

    /**@
     * Alias for <code>mods { modInfo {} }</code>
     * @param closure
     */
    void mod(@DelegatesTo(value = ModInfoBuilder, strategy = DELEGATE_FIRST)
             @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.ModInfoBuilder')
             final Closure closure) {
        mods { modInfo(closure) }
    }

    void mods(@DelegatesTo(value = ModsBuilder, strategy = DELEGATE_FIRST)
              @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.ModsBuilder')
              final Closure closure) {
        log.debug "mods(closure)"
        core.push('mods')
        final modsBuilder = new ModsBuilder(core)
        closure.resolveStrategy = DELEGATE_FIRST
        closure.delegate = modsBuilder
        closure.call(modsBuilder)
        core.pop()
    }

    void mixins(@DelegatesTo(value = MixinsBuilder, strategy = DELEGATE_FIRST)
                @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.MixinsBuilder')
                final Closure closure) {
        log.debug "mixins(closure)"
        core.push('mixins')
        final mixinsBuilder = new MixinsBuilder(core)
        closure.delegate = mixinsBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(mixinsBuilder)
        core.pop()
    }

    void accessTransformers(@DelegatesTo(value = AccessTransformersBuilder, strategy = DELEGATE_FIRST)
                            @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.AccessTransformersBuilder')
                            final Closure closure) {
        log.debug "accessTransformers(closure)"
        core.push('accessTransformers')
        final accessTransformersBuilder = new AccessTransformersBuilder(core)
        closure.delegate = accessTransformersBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(accessTransformersBuilder)
        core.pop()
    }

    protected NeoForgeModsDotGroovy(final Map<String, ?> environment) {
        super(environment)
    }

    static NeoForgeModsDotGroovy make(@DelegatesTo(value = NeoForgeModsDotGroovy, strategy = DELEGATE_FIRST)
                                      @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.NeoForgeModsDotGroovy') final Closure closure,
                                      final Map<String, ?> environment = [:]) {
        final NeoForgeModsDotGroovy val = new NeoForgeModsDotGroovy(environment)
        closure.resolveStrategy = DELEGATE_FIRST
        closure.delegate = val
        closure.call(val)
        return val
    }
}
