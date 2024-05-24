package org.groovymc.modsdotgroovy.frontend.multiplatform

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.MapClosureInterceptor
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor
import org.groovymc.modsdotgroovy.frontend.multiplatform.fabric.ContactBuilder
import org.groovymc.modsdotgroovy.frontend.multiplatform.fabric.EntrypointsBuilder
import org.groovymc.modsdotgroovy.frontend.multiplatform.fabric.PersonsBuilder
import org.jetbrains.annotations.Nullable
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore

import static groovy.lang.Closure.DELEGATE_FIRST

@CompileStatic
@Log4j2(category = 'MDG - Multiplatform Frontend')
class ModInfoBuilder extends DslBuilder implements PropertyInterceptor, MapClosureInterceptor, OnPlatform {
    /**
     * The modId of the mod. This should match the value of your mod's {@literal @}GMod/{@literal @}Mod annotated main class.
     */
    String modId

    /**
     * The friendly name of the mod. This is the name that will be displayed in the in-game Mods screen.<br>
     * Defaults to a capitalized version of the modId if omitted/null.
     */
    @Nullable String displayName

    /**@
     * A list of Maven repository URL strings where dependencies can be looked for in addition to Quilt's central repository.
     */
    @Nullable List<String> repositories = null

    /**@
     * Influences whether or not a mod candidate should be loaded or not.
     */
    @Nullable LoadType loadType = null

    /**@
     * The intermediate mappings used for this mod. The intermediate mappings string must be a valid maven coordinate
     * and match the {@code ^[a-zA-Z0-9-_.]+:[a-zA-Z0-9-_.]+$} regular expression. This field currently only officially
     * supports {@code org.quiltmc:hashed} and {@code net.fabricmc:intermediary}.
     */
    @Nullable String intermediateMappings = "net.fabricmc:intermediary"

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

    // todo: change type to a File instead of a String
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
    DisplayTest displayTest = DisplayTest.MATCH_VERSION

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

    /**@
     * A list of authors of the mod.
     */
    void authors(@DelegatesTo(value = PersonsBuilder, strategy = DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.multiplatform.fabric.PersonsBuilder')
                 final Closure closure) {
        log.debug "authors(closure)"
        core.push('authors')
        final authorsBuilder = new PersonsBuilder(core, 'author')
        closure.resolveStrategy = DELEGATE_FIRST
        closure.delegate = authorsBuilder
        closure.call(authorsBuilder)
        core.pop()
    }

    /**@
     * A list of authors of the mod.
     */
    void contributors(@DelegatesTo(value = PersonsBuilder, strategy = DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.multiplatform.fabric.PersonsBuilder')
                 final Closure closure) {
        log.debug "contributors(closure)"
        core.push('contributors')
        final authorsBuilder = new PersonsBuilder(core, 'contributors')
        closure.resolveStrategy = DELEGATE_FIRST
        closure.delegate = authorsBuilder
        closure.call(authorsBuilder)
        core.pop()
    }

    void dependencies(@DelegatesTo(value = DependenciesBuilder, strategy = DELEGATE_FIRST)
                      @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.multiplatform.DependenciesBuilder')
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
                  @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.multiplatform.FeaturesBuilder')
                  final Closure closure) {
        log.debug "features(closure)"
        core.push('features')
        final featuresBuilder = new FeaturesBuilder(core)
        closure.delegate = featuresBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(featuresBuilder)
        core.pop()
    }

    /**@
     * Defines the contact information for the project.
     * The list is not exhaustive - mods may provide additional, non-standard keys (such as discord, slack, twitter, etc) - if possible, they should be valid URLs.
     * @param closure
     */
    void contact(@DelegatesTo(value = ContactBuilder, strategy = DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.multiplatform.fabric.ContactBuilder')
                 final Closure closure) {
        log.debug "contact(closure)"
        core.push('contact')
        final contactBuilder = new ContactBuilder(core)
        closure.resolveStrategy = DELEGATE_FIRST
        closure.delegate = contactBuilder
        closure.call(contactBuilder)
        core.pop()
    }

    /**@
     * Defines main classes of your mod, that will be loaded. Each entry point can contain any number of classes to load.
     * Ignored on Forge.
     */
    void entrypoints(@DelegatesTo(value = EntrypointsBuilder, strategy = DELEGATE_FIRST)
                     @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.multiplatform.fabric.EntrypointsBuilder')
                     final Closure closure) {
        log.debug "entrypoints(closure)"
        core.push('entrypoints')
        final entrypointsBuilder = new EntrypointsBuilder(core)
        closure.resolveStrategy = DELEGATE_FIRST
        closure.delegate = entrypointsBuilder
        closure.call(entrypointsBuilder)
        core.pop()
    }

    ModInfoBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
