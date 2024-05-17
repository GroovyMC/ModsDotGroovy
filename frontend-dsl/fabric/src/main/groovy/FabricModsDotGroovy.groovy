import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.types.core.Platform
import org.groovymc.modsdotgroovy.frontend.MapClosureInterceptor
import org.groovymc.modsdotgroovy.frontend.ModsDotGroovyFrontend
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

import org.groovymc.modsdotgroovy.frontend.fabric.*
import org.jetbrains.annotations.Nullable

/**
 * This is the Fabric frontend layer
 */
@PackageScope
@CompileStatic
@Log4j2(category = 'MDG - Fabric Frontend')
class FabricModsDotGroovy extends ModsDotGroovyFrontend implements PropertyInterceptor, MapClosureInterceptor {
    /**@
     * Needed for internal mechanisms. Must always be 1.
     * Mandatory
     */
    int schemaVersion = 1

    /**@
     * Defines the mod's identifier - a string of Latin letters, digits, underscores with length from 2 to 64.
     * Mandatory
     */
    String id

    /**@
     * Defines the mod's version - a string value, optionally matching the <a href="https://semver.org/">Semantic Versioning 2.0.0</a> specification.
     * Mandatory
     */
    String version

    /**@
     * Defines the user-friendly mod's name. If not present, assume it matches id.
     */
    @Nullable String name = null

    /**@
     * Defines the mod's description. If not present, assume empty string.
     */
    @Nullable String description = null

    /**@
     * Defines the list of ids of mod. It can be seen as the aliases of the mod. Fabric Loader will treat these ids as mods that exist.
     * If there are other mods using that id, they will not be loaded.
     */
    @Nullable List<String> provides = null

    /**@
     * Defines where mod runs: only on the client side (client mod), only on the server side (plugin) or on both sides (regular mod). Contains the environment identifier:
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
     */
    @Nullable String icon = null

    /**@
     * Defines the licensing information.
     * <p>This should provide the complete set of preferred licenses conveying the entire mod package. In other words,
     * compliance with all listed licenses should be sufficient for usage, redistribution, etc. of the mod package as a whole.</p>
     * <p>For cases where a part of code is dual-licensed, choose the preferred license. The list is not exhaustive,
     * serves primarily as a kind of hint, and does not prevent you from granting additional rights/licenses on a case-by-case basis.</p>
     * <p>To aid automated tools, it is recommended to use <a href="https://spdx.org/licenses/">SPDX License Identifiers</a> for open-source licenses.</p>
     */
    @Nullable def license = null

    @Nullable String accessWidener = null

    void icon(final int size, final String path) {
        log.debug "icon(int, string)"
        core.push('icon')
        core.put(size as String, path)
        core.pop()
    }

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
     * Defines main classes of your mod, that will be loaded. Each entry point can contain any number of classes to load.
     */
    void entrypoints(@DelegatesTo(value = EntrypointsBuilder, strategy = Closure.DELEGATE_FIRST)
                     @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.EntrypointsBuilder')
                     final Closure closure) {
        log.debug "entrypoints(closure)"
        core.push('entrypoints')
        final entrypointsBuilder = new EntrypointsBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = entrypointsBuilder
        closure.call(entrypointsBuilder)
        core.pop()
    }

    /**@
     *  A list of nested JARs inside your mod's JAR to load. Before using the field, check out the guidelines on the usage of the nested JARs.
     *  Each entry is an object containing file key. That should be a path inside your mod's JAR to the nested JAR.
     */
    void jars(@DelegatesTo(value = JarsBuilder, strategy = Closure.DELEGATE_FIRST)
              @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.JarsBuilder')
              final Closure closure) {
        log.debug "jars(closure)"
        core.push('jars')
        final jarsBuilder = new JarsBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = jarsBuilder
        closure.call(jarsBuilder)
        core.pop()
    }

    /**@
     * A dictionary of adapters for used languages to their adapter classes full names.
     */
    void languageAdapters(@DelegatesTo(value = SimpleBuilder, strategy = Closure.DELEGATE_FIRST)
                          @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.SimpleBuilder')
                          final Closure closure) {
        log.debug "languageAdapters(closure)"
        core.push('languageAdapters')
        final languageAdaptersBuilder = new SimpleBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = languageAdaptersBuilder
        closure.call(languageAdaptersBuilder)
        core.pop()
    }

    /**@
     * A list of mixin configuration files.
     */
    void mixins(@DelegatesTo(value = MixinsBuilder, strategy = Closure.DELEGATE_FIRST)
                @ClosureParams(value = SimpleType, options = 'MixinsBuilder')
                final Closure closure) {
        log.debug "mixins(closure)"
        core.push('mixins')
        final mixinsBuilder = new MixinsBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = mixinsBuilder
        closure.call(mixinsBuilder)
        core.pop()
    }

    /**@
     * For dependencies required to run. Without them a game will crash.
     */
    void depends(@DelegatesTo(value = DependenciesBuilder, strategy = Closure.DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.DependenciesBuilder')
                 final Closure closure) {
        dependencyBuilder('depends', closure)
    }

    /**@
     * For dependencies not required to run. Without them a game will log a warning.
     */
    void recommends(@DelegatesTo(value = DependenciesBuilder, strategy = Closure.DELEGATE_FIRST)
                    @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.DependenciesBuilder')
                    final Closure closure) {
        dependencyBuilder('recommends', closure)
    }

    /**@
     * For dependencies not required to run. Use this as a kind of metadata.
     */
    void suggests(@DelegatesTo(value = DependenciesBuilder, strategy = Closure.DELEGATE_FIRST)
                  @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.DependenciesBuilder')
                  final Closure closure) {
        dependencyBuilder('suggests', closure)
    }

    /**@
     * For mods whose together with yours might cause a game crash. With them a game will crash.
     */
    void breaks(@DelegatesTo(value = DependenciesBuilder, strategy = Closure.DELEGATE_FIRST)
                @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.DependenciesBuilder')
                final Closure closure) {
        dependencyBuilder('breaks', closure)
    }

    /**@
     * For mods whose together with yours cause some kind of bugs, etc. With them a game will log a warning.
     */
    void conflicts(@DelegatesTo(value = DependenciesBuilder, strategy = Closure.DELEGATE_FIRST)
                   @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.DependenciesBuilder')
                   final Closure closure) {
        dependencyBuilder('conflicts', closure)
    }

    /**@
     * Defines the contact information for the project.
     * The list is not exhaustive - mods may provide additional, non-standard keys (such as discord, slack, twitter, etc) - if possible, they should be valid URLs.
     * @param closure
     */
    void contact(@DelegatesTo(value = ContactBuilder, strategy = Closure.DELEGATE_FIRST)
                @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.ContactBuilder')
                final Closure closure) {
        log.debug "contact(closure)"
        core.push('contact')
        final contactBuilder = new ContactBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = contactBuilder
        closure.call(contactBuilder)
        core.pop()
    }

    /**@
     * A list of authors of the mod.
     */
    void authors(@DelegatesTo(value = PersonsBuilder, strategy = Closure.DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.PersonsBuilder')
                 final Closure closure) {
        log.debug "authors(closure)"
        core.push('authors')
        final authorsBuilder = new PersonsBuilder(core, 'author')
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = authorsBuilder
        closure.call(authorsBuilder)
        core.pop()
    }

    /**@
     * A list of contributors to the mod. Each entry is the same as in author field.
     */
    void contributors(@DelegatesTo(value = PersonsBuilder, strategy = Closure.DELEGATE_FIRST)
                      @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.PersonsBuilder')
                      final Closure closure) {
        log.debug "contributors(closure)"
        core.push('contributors')
        final authorsBuilder = new PersonsBuilder(core, 'contributor')
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = authorsBuilder
        closure.call(authorsBuilder)
        core.pop()
    }

    /**@
     * You can add any field you want to add inside custom field. Loader would ignore them. However it's highly recommended
     * to namespace your fields to avoid conflicts if your fields (names) would be added to the standard specification.
     */
    void custom(@DelegatesTo(value = CustomPropertyBuilder, strategy = Closure.DELEGATE_FIRST)
                @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.CustomPropertyBuilder')
                final Closure closure) {
        log.debug "custom(closure)"
        core.push('custom')
        final customPropertyBuilder = new CustomPropertyBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = customPropertyBuilder
        closure.call(customPropertyBuilder)
        core.pop()
    }

    private FabricModsDotGroovy(final Map<String, ?> environment) {
        super(environment)
    }

    private void dependencyBuilder(final String name,
                                   @DelegatesTo(value = DependenciesBuilder, strategy = Closure.DELEGATE_FIRST)
                                   @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.DependenciesBuilder')
                                   final Closure closure) {
        log.debug "${name}(closure)"
        core.push(name)
        final dependenciesBuilder = new DependenciesBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = dependenciesBuilder
        closure.call(dependenciesBuilder)
        core.pop()
    }

    static FabricModsDotGroovy make(@DelegatesTo(value = FabricModsDotGroovy, strategy = Closure.DELEGATE_FIRST)
                                    @ClosureParams(value = SimpleType, options = 'FabricModsDotGroovy') final Closure closure,
                                    final Map<String, ?> environment = [:]) {
        final FabricModsDotGroovy val = new FabricModsDotGroovy(environment)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = val
        closure.call(val)
        return val
    }
}
