import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.Platform
import org.groovymc.modsdotgroovy.frontend.MapClosureInterceptor
import org.groovymc.modsdotgroovy.frontend.ModInfoBuilder
import org.groovymc.modsdotgroovy.frontend.ModsBuilder
import org.groovymc.modsdotgroovy.frontend.ModsDotGroovyFrontend
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor
import org.groovymc.modsdotgroovy.frontend.fabric.IconBuilder
import org.jetbrains.annotations.Nullable

@PackageScope
@CompileStatic
@Log4j2(category = 'MDG - Forge Frontend')
class MultiplatformModsDotGroovy extends ModsDotGroovyFrontend implements PropertyInterceptor, MapClosureInterceptor {
    /**@
     * The name of the mod loader type to load - for regular Java FML @Mod mods it should be {@code javafml}.
     * For GroovyModLoader @GMod mods it should be {@code gml}.
     */
    String modLoader = 'javafml'

    /**@
     * A version range to match for the {@link #setModLoader(java.lang.String)}.
     */
    String loaderVersion = '[1,)'

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

    /**@
     * A URL pointing to your mod's source code repository. Ignored on Forge.
     */
    @Nullable String sourcesUrl = null

    /**@
     * Defines where mod runs: only on the client side (client mod), only on the server side (plugin) or on both sides (regular mod). Contains the environment identifier.
     * Ignored on Forge.
     */
    @Nullable Environment environment = null

    /**@
     * Defines the mod's icon. Icons are square PNG files.
     * (Minecraft resource packs use 128×128, but that is not a hard requirement - a power of two is, however, recommended.)
     * Can be provided in one of two forms:
     * <ul>
     *     <li>A path to a single PNG file.</li>
     *     <li>A dictionary of images widths to their files' paths.</li>
     * </ul>
     * Ignored on Forge.
     */
    @Nullable String icon = null

    /**@
     * Ignored on Forge.
     */
    @Nullable String accessWidener = null

    /**@
     * Ignored on Forge.
     */
    void icon(final int size, final String path) {
        log.debug "icon(int, string)"
        core.push('icon')
        core.put(size, path)
        core.pop()
    }

    /**@
     * Ignored on Forge.
     */
    void icon(@DelegatesTo(value = IconBuilder, strategy = Closure.DELEGATE_FIRST)
              @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.IconBuilder')
              final Closure closure) {
        log.debug "icon(closure)"
        core.push('icon')
        final customFieldsBuilder = new IconBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = customFieldsBuilder
        closure.call(customFieldsBuilder)
        core.pop()
    }

    /**@
     * Alias for <code>mods { modInfo {} }</code>
     * @param closure
     */
    void mod(@DelegatesTo(value = ModInfoBuilder, strategy = Closure.DELEGATE_FIRST)
             @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.ModInfoBuilder')
             final Closure closure) {
        mods { modInfo(closure) }
    }

    void mods(@DelegatesTo(value = ModsBuilder, strategy = Closure.DELEGATE_FIRST)
              @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.ModsBuilder')
              final Closure closure) {
        log.debug "mods(closure)"
        core.push('mods')
        final modsBuilder = new ModsBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = modsBuilder
        closure.call(modsBuilder)
        core.pop()
    }

    void onForge(final Closure closure) {
        log.debug "onForge(closure)"
        if (platform === Platform.FORGE)
            closure.call()
    }

    void onFabric(final Closure closure) {
        log.debug "onForge(closure)"
        if (platform === Platform.FABRIC)
            closure.call()
    }

    private MultiplatformModsDotGroovy(final Map<String, ?> environment) {
        super(environment)
    }

    static MultiplatformModsDotGroovy make(@DelegatesTo(value = MultiplatformModsDotGroovy, strategy = Closure.DELEGATE_FIRST)
                              @ClosureParams(value = SimpleType, options = 'MultiplatformModsDotGroovy') final Closure closure) {
        return make(closure, [:])
    }

    static MultiplatformModsDotGroovy make(@DelegatesTo(value = MultiplatformModsDotGroovy, strategy = Closure.DELEGATE_FIRST)
                              @ClosureParams(value = SimpleType, options = 'MultiplatformModsDotGroovy') final Closure closure,
                              final Binding scriptBinding) {
        return make(closure, scriptBinding.variables)
    }

    static MultiplatformModsDotGroovy make(@DelegatesTo(value = MultiplatformModsDotGroovy, strategy = Closure.DELEGATE_FIRST)
                              @ClosureParams(value = SimpleType, options = 'MultiplatformModsDotGroovy') final Closure closure,
                              final Map<String, ?> environment) {
        final MultiplatformModsDotGroovy val = new MultiplatformModsDotGroovy(environment)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = val
        closure.call(val)
        return val
    }
}
