package org.groovymc.modsdotgroovy.frontend.forge

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.MapClosureInterceptor
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor
import org.jetbrains.annotations.Nullable
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore

import static groovy.lang.Closure.DELEGATE_FIRST

@CompileStatic
@Log4j2(category = 'MDG - Forge Frontend')
class ModInfoBuilder extends DslBuilder implements PropertyInterceptor, MapClosureInterceptor {
    /**
     * The modId of the mod. This should match the value of your mod's {@literal @}GMod/{@literal @}Mod annotated main class.
     */
    String modId

    /**
     * The friendly name of the mod. This is the name that will be displayed in the in-game Mods screen.<br>
     * Defaults to a capitalized version of the modId if omitted/null.
     */
    @Nullable String displayName

    /**
     * The version number of the mod - there's a few well known ${} variables usable here or just hardcode it.<br>
     * ${file.jarVersion} will substitute the value of the Implementation-Version as read from the mod's JAR file metadata.<br>
     * See the associated build.gradle script for how to populate this completely automatically during a build.
     */
    String version

    /**
     * A URL to query for updates for this mod.<br>
     * See <a href='https://mcforge.readthedocs.io/en/latest/gettingstarted/autoupdate/'>the JSON update specification</a>
     */
    @Nullable String updateJsonUrl

    /**
     * A URL for the "homepage" for this mod, displayed in the in-game Mods screen.
     */
    @Nullable String displayUrl

    /**
     * A file name (in the root of the mod JAR) containing a logo for display in the in-game Mods screen.
     */
    @Nullable String logoFile

    /**
     * People you give credits for the mod.
     */
    @Nullable String credits

    /**
     * Display Test controls the display for your mod in the server connection screen.
     */
    DisplayTest displayTest = 'MATCH_VERSION'

    /**
     * A multi-line description text for the mod, displayed in the in-game Mods screen.
     */
    String description = ''

    /**
     * The authors of the mod. <br>
     * These will be automatically formatted as 'x, y and z' on Forge.
     */
    List<String> authors = []

    /**
     * The author of the mod. <br>
     * An alternative to {@link #authors} for single author mods.
     */
    String author = ''

    void dependencies(@DelegatesTo(value = DependenciesBuilder, strategy = DELEGATE_FIRST)
                      @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.forge.DependenciesBuilder')
                      final Closure closure) {
        log.debug "dependencies(closure)"
        core.push('dependencies')
        final dependenciesBuilder = new DependenciesBuilder(core)
        closure.delegate = dependenciesBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(dependenciesBuilder)
        core.pop()
    }

    void features(@DelegatesTo(value = FeaturesBuilder, strategy = DELEGATE_FIRST)
                  @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.forge.FeaturesBuilder')
                  final Closure closure) {
        log.debug "features(closure)"
        core.push('features')
        final featuresBuilder = new FeaturesBuilder(core)
        closure.delegate = featuresBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(featuresBuilder)
        core.pop()
    }

    ModInfoBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
