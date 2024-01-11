import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.Platform
import org.groovymc.modsdotgroovy.frontend.MapClosureInterceptor
import org.groovymc.modsdotgroovy.frontend.ModsDotGroovyFrontend
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

import org.groovymc.modsdotgroovy.frontend.quilt.*
import org.jetbrains.annotations.Nullable

// WIP, based on the Fabric frontend. Resources used:
// https://github.com/QuiltMC/rfcs/blob/main/specification/0002-quilt.mod.json.md
// https://modder.wiki.quiltmc.org/versions/1.19/getting_started/setup/migrating_from_fabric/

/**
 * This is the Quilt frontend layer
 */
@PackageScope
@CompileStatic
@Log4j2(category = 'MDG - Quilt Frontend')
class QuiltModsDotGroovy extends ModsDotGroovyFrontend implements PropertyInterceptor, MapClosureInterceptor {
    /**@
     * Needed for internal mechanisms. Must always be 1.
     */
    int schemaVersion = 1

    /**@
     * A unique identifier for the organization behind or developers of the mod. The group string must match the
     * {@code ^[a-zA-Z0-9-_.]+$} regular expression, and must not begin with the reserved namespace {@code loader.plugin.}
     * <p>It is recommended, but not required, to follow Maven's
     * <a href="https://maven.apache.org/guides/mini/guide-naming-conventions.html">guide to naming conventions</a>.</p>
     */
    String group

    /**@
     * Defines the mod's identifier - a string of Latin letters, digits, underscores with length from 2 to 64.
     */
    String id

    /**@
     * Defines the mod's version - a string value, optionally matching the <a href="https://semver.org/">Semantic Versioning 2.0.0</a> specification.
     */
    String version

    /**@
     * Influences whether or not a mod candidate should be loaded or not.
     */
    LoadType loadType = LoadType.ALWAYS

    /**@
     * A list of Maven repository URL strings where dependencies can be looked for in addition to Quilt's central repository.
     */
    @Nullable List<String> repositories = null

    /**@
     * The intermediate mappings used for this mod. The intermediate mappings string must be a valid maven coordinate
     * and match the {@code ^[a-zA-Z0-9-_.]+:[a-zA-Z0-9-_.]+$} regular expression. This field currently only officially
     * supports {@code org.quiltmc:hashed} and {@code net.fabricmc:intermediary}.
     */
    @Nullable String intermediateMappings = null

    /**@
     * A single path to a mixin configuration file relative to the root of the mod JAR.
     */
    @Nullable String mixin = null

    /**@
     * A list of paths to mixin configuration files relative to the root of the mod JAR.
     */
    void setMixins(final List<String> mixins) {
        core.put('mixin', mixins)
    }

    /**@
     * A single path to a access widener file relative to the root of the mod JAR.
     */
    @Nullable String accessWidener = null

    /**@
     * A list of paths to access widener files relative to the root of the mod JAR.
     */
    void setAccessWideners(final List<String> accessWideners) {
        core.put('accessWidener', accessWideners)
    }

    /**@
     * Optional metadata that can be used by mods to display information about the mods installed.
     */
    void metadata(@DelegatesTo(value = MetadataBuilder, strategy = Closure.DELEGATE_FIRST)
                  @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.MetadataBuilder')
                  final Closure closure) {
        log.debug 'metadata(closure)'
        core.push('metadata')
        final metadataBuilder = new MetadataBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = metadataBuilder
        closure.call(metadataBuilder)
        core.pop()
    }

    /**@
     * Describes other mods/APIs that this package provides.
     */
    void provides(@DelegatesTo(value = ProvidesBuilder, strategy = Closure.DELEGATE_FIRST)
                  @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.ProvidesBuilder')
                  final Closure closure) {
        log.debug 'provides(closure)'
        core.push('provides')
        final providesBuilder = new ProvidesBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = providesBuilder
        closure.call(providesBuilder)
        core.pop()
    }

    /**@
     * Defines main classes of your mod, that will be loaded. Each entry point can contain any number of classes to load.
     */
    void entrypoints(@DelegatesTo(value = EntrypointsBuilder, strategy = Closure.DELEGATE_FIRST)
                     @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.EntrypointsBuilder')
                     final Closure closure) {
        log.debug 'entrypoints(closure)'
        core.push('entrypoints')
        final entrypointsBuilder = new EntrypointsBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = entrypointsBuilder
        closure.call(entrypointsBuilder)
        core.pop()
    }

    /**@
     * Defines loader plugins
     */
    void plugins(@DelegatesTo(value = SimpleBuilder, strategy = Closure.DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.SimpleBuilder')
                 final Closure closure) {
        log.debug 'plugins(Closure)'
        core.push('plugins')
        final pluginsBuilder = new SimpleBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = pluginsBuilder
        closure.call(pluginsBuilder)
        core.pop()
    }

    /**@
     * A list of nested JARs inside your mod's JAR to load. Before using the field, check out the guidelines on the usage of the nested JARs.
     * Each entry is an object containing file key. That should be a path inside your mod's JAR to the nested JAR.
     */
    void jars(@DelegatesTo(value = JarsBuilder, strategy = Closure.DELEGATE_FIRST)
              @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.JarsBuilder')
              final Closure closure) {
        log.debug 'jars(closure)'
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
                          @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.SimpleBuilder')
                          final Closure closure) {
        log.debug 'languageAdapters(closure)'
        core.push('languageAdapters')
        final languageAdaptersBuilder = new SimpleBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = languageAdaptersBuilder
        closure.call(languageAdaptersBuilder)
        core.pop()
    }

    /**@
     * Defines mods that this mod will not function without.
     */
    void depends(@DelegatesTo(value = DependenciesBuilder, strategy = Closure.DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.DependenciesBuilder')
                 final Closure closure) {
        dependencyBuilder('depends', closure)
    }

    /**@
     * Defines mods that this mod either breaks or is broken by.
     */
    void breaks(@DelegatesTo(value = DependenciesBuilder, strategy = Closure.DELEGATE_FIRST)
                @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.DependenciesBuilder')
                final Closure closure) {
        dependencyBuilder('breaks', closure)
    }

    /**@
     * Contains flags and options related to Minecraft specifically.
     */
    void minecraft(@DelegatesTo(value = MinecraftBuilder, strategy = Closure.DELEGATE_FIRST)
                   @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.MinecraftBuilder')
                   final Closure closure) {
        log.debug 'minecraft(closure)'
        core.push('minecraft')
        final minecraftBuilder = new MinecraftBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = minecraftBuilder
        closure.call(minecraftBuilder)
        core.pop()
    }

    private QuiltModsDotGroovy(final Map<String, ?> environment) {
        super(environment)
    }

    private void dependencyBuilder(final String name,
                                   @DelegatesTo(value = DependenciesBuilder, strategy = Closure.DELEGATE_FIRST)
                                   @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.DependenciesBuilder')
                                   final Closure closure) {
        log.debug "${name}(closure)"
        core.push(name)
        final dependenciesBuilder = new DependenciesBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = dependenciesBuilder
        closure.call(dependenciesBuilder)
        core.pop()
    }

    static QuiltModsDotGroovy make(@DelegatesTo(value = QuiltModsDotGroovy, strategy = Closure.DELEGATE_FIRST)
                                   @ClosureParams(value = SimpleType, options = 'QuiltModsDotGroovy') final Closure closure) {
        return make(closure, [:])
    }

    static QuiltModsDotGroovy make(@DelegatesTo(value = QuiltModsDotGroovy, strategy = Closure.DELEGATE_FIRST)
                                   @ClosureParams(value = SimpleType, options = 'QuiltModsDotGroovy') final Closure closure,
                                   final Binding scriptBinding) {
        return make(closure, scriptBinding.variables)
    }

    static QuiltModsDotGroovy make(@DelegatesTo(value = QuiltModsDotGroovy, strategy = Closure.DELEGATE_FIRST)
                                   @ClosureParams(value = SimpleType, options = 'QuiltModsDotGroovy') final Closure closure,
                                   final Map<String, ?> environment) {
        final QuiltModsDotGroovy val = new QuiltModsDotGroovy(environment)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = val
        closure.call(val)
        return val
    }
}
