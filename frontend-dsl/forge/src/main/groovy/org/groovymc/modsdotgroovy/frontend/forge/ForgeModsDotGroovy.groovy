package org.groovymc.modsdotgroovy.frontend.forge

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.frontend.MapClosureInterceptor
import org.groovymc.modsdotgroovy.frontend.ModsDotGroovyFrontend
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor
import org.groovymc.modsdotgroovy.frontend.forge.ModInfoBuilder
import org.groovymc.modsdotgroovy.frontend.forge.ModsBuilder
import org.groovymc.rootpackagetransformer.RootPackage
import org.jetbrains.annotations.Nullable

@PackageScope
@CompileStatic
@Log4j2(category = 'MDG - Forge Frontend')
@RootPackage
class ForgeModsDotGroovy extends ModsDotGroovyFrontend implements PropertyInterceptor, MapClosureInterceptor {
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

    /**@
     * If your mod is purely client-side and has no multiplayer functionality (be it dedicated servers or Open to LAN),
     * set this to true, and Forge will set the correct {@link ModInfoBuilder#displayTest} for you and skip loading your mod on dedicated servers.
     */
    boolean clientSideOnly = false

    /**@
     * Alias for <code>mods { modInfo {} }</code>
     * @param closure
     */
    void mod(@DelegatesTo(value = ModInfoBuilder, strategy = Closure.DELEGATE_FIRST)
             @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.forge.ModInfoBuilder')
             final Closure closure) {
        mods { modInfo(closure) }
    }

    void mods(@DelegatesTo(value = ModsBuilder, strategy = Closure.DELEGATE_FIRST)
              @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.forge.ModsBuilder')
              final Closure closure) {
        log.debug "mods(closure)"
        core.push('mods')
        final modsBuilder = new ModsBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = modsBuilder
        closure.call(modsBuilder)
        core.pop()
    }

    protected ForgeModsDotGroovy(final Map<String, ?> environment) {
        super(environment)
    }

    static ForgeModsDotGroovy make(@DelegatesTo(value = ForgeModsDotGroovy, strategy = Closure.DELEGATE_FIRST)
                                   @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.forge.ForgeModsDotGroovy') final Closure closure,
                                   final Map<String, ?> environment = [:]) {
        final ForgeModsDotGroovy val = new ForgeModsDotGroovy(environment)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = val
        closure.call(val)
        return val
    }
}
