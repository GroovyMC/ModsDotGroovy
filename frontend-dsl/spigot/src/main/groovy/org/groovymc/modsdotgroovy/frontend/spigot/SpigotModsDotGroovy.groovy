package org.groovymc.modsdotgroovy.frontend.spigot

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.frontend.MapClosureInterceptor
import org.groovymc.modsdotgroovy.frontend.ModsDotGroovyFrontend
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor
import org.groovymc.modsdotgroovy.frontend.spigot.CommandsBuilder
import org.groovymc.modsdotgroovy.frontend.spigot.PermissionsBuilder
import org.groovymc.rootpackagetransformer.RootPackage
import org.jetbrains.annotations.Nullable

// https://www.spigotmc.org/wiki/plugin-yml/

@PackageScope
@CompileStatic
@RootPackage
@Log4j2(category = 'MDG - Spigot Frontend')
class SpigotModsDotGroovy extends ModsDotGroovyFrontend implements PropertyInterceptor, MapClosureInterceptor {
    /**@
     * This attribute points to the class of your plugin that extends JavaPlugin.
     * <p>This <i>must</i> contain the full namespace including the class file itself.</p>
     */
    String main

    /**@
     * The name of your plugin.
     * <p><i>Must</i> consist of all alphanumeric characters and underscores {@code (a-z,A-Z,0-9, _)}</p>
     */
    String name

    /**@
     * The version of your plugin.
     * <p>The most common versioning format is <a href="https://semver.org/">Semantic Versioning</a>, which is written
     * as MAJOR.MINOR.PATCH (eg: 1.4.1 or 9.12.4)</p>
     */
    String version

    /**@
     * A human friendly description of the functionality your plugin provides.
     * <p>The description can have multiple lines.</p>
     */
    @Nullable String description = null

    /**@
     * The version of the API you want to use.
     * <p>1.13, 1.14, 1.15, 1.16, 1.17, 1.18, 1.19 and 1.20 are the available versions.</p>
     * <p>This will signal to the server that your plugin has been coded with a specific server version in mind, and
     * that it should not apply any sort of backwards compatibility measures. As a result you will also need to make
     * sure that you have programmed your code to account for reading of older configurations, data, etc...</p>
     * <p>Each server version can decide how compatibility is achieved, unknown or future versions will prevent the
     * plugin from enabling. As of the 1.14 release, the api-version 1.13 is still allowed - however future versions
     * may drop backwards support based on this version.</p>
     */
    @Nullable String apiVersion = null

    /**@
     * Explicitly state when a plugin should be loaded. If not supplied will default to {@link LoadState#POSTWORLD}.
     */
    @Nullable LoadState load = LoadState.POST_WORLD

    /**@
     * Uniquely identifies who developed this plugin.
     * <p>Used in some server error messages to provide helpful feedback on who to contact when an error occurs.</p>
     * <p>A <a href="https://spigotmc.org">SpigotMC.org</a> forum handle or email address is recommended.</p>
     */
    @Nullable String author = null

    /**@
     * Allows you to list multiple authors, if it is a collaborative project.
     */
    @Nullable List<String> authors = null

    /**@
     * The plugin's or author's website.
     * <p>If you have no dedicated website, a link to the page where this plugin is listed is recommended.</p>
     */
    @Nullable String website = null

    /**@
     * The name to use when logging to console instead of the plugin's name.
     */
    @Nullable String prefix = null

    /**@
     * A list of plugin names that your plugin requires to load.
     * <p>If any plugin listed here is not found your plugin will fail to load.</p>
     * <p>If multiple plugins list each other as a depend, so that there are no plugins without an unloadable
     * dependency, all will fail to load.</p>
     */
    @Nullable List<String> depend = null

    /**@
     * A list of plugin names that are required for your plugin to have full functionality, but not required for your
     * plugin to load.
     * <p>Your plugin will load after any plugins listed here.</p>
     * <p>Circular soft dependencies are loaded arbitrarily.</p>
     */
    @Nullable List<String> softDepend = null

    /**@
     * A list of plugin names that should be loaded after your plugin.
     * <p>Treated as if the listed plugin soft depends on this plugin.</p>
     * <p>Your plugin will load before any plugins listed here.</p>
     * <p>Circular soft dependencies are loaded arbitrarily.</p>
     */
    @Nullable List<String> loadBefore = null

    /**@
     * A list of maven coordinates to libraries your plugin needs which can be loaded from Maven Central.
     * <p>Helps reduce plugin size and eliminates the need for relocation.</p>
     * <p>Intended for use with large non-Minecraft dependencies. Specialized libraries should still be shaded and relocated.</p>
     */
    @Nullable List<String> libraries = null

    /**@
     * Register a list of unique command names that your plugin wishes to register.
     */
//    void setCommands(final List<String> commandNamesList) {
//        log.debug 'commands(list)'
//        core.push('commands')
//        core.put('replace', true)
//        for (final String name in commandNamesList) {
//            core.push('command')
//            core.put('name', name)
//            core.pop()
//        }
//        core.pop()
//    }

    /**@
     * Register commands for your plugin.
     */
    void commands(@DelegatesTo(value = CommandsBuilder, strategy = Closure.DELEGATE_FIRST)
                  @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.spigot.CommandsBuilder')
                  final Closure closure) {
        log.debug 'commands(closure)'
        core.push('commands')
        final commandsBuilder = new CommandsBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = commandsBuilder
        closure.call(commandsBuilder)
        core.pop()
    }

    /**@
     * Register permissions for your plugin (optional -t his can also be done from code). Each node represents a permission to register.
     * <p>Each permission can have multiple attributes.</p>
     * <p>Permission registration is optional, can also be done from code.</p>
     * <p>Permission registration allows you to set descriptions, defaults, and child-parent relationships.</p>
     * <p>Permission names should be kept in the style of {@code <pluginname>.[category].[category].<permission>}</p>
     * @param closure
     */
    void permissions(@DelegatesTo(value = PermissionsBuilder, strategy = Closure.DELEGATE_FIRST)
                     @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.spigot.PermissionsBuilder')
                     final Closure closure) {
        log.debug 'permissions(closure)'
        core.push('permissions')
        final permissionsBuilder = new PermissionsBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = permissionsBuilder
        closure.call(permissionsBuilder)
        core.pop()
    }

    private SpigotModsDotGroovy(final Map<String, ?> environment) {
        super(environment)
    }

    static SpigotModsDotGroovy make(@DelegatesTo(value = SpigotModsDotGroovy, strategy = Closure.DELEGATE_FIRST)
                                    @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.spigot.SpigotModsDotGroovy') final Closure closure,
                                    final Map<String, ?> environment = [:]) {
        final SpigotModsDotGroovy val = new SpigotModsDotGroovy(environment)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = val
        closure.call(val)
        return val
    }
}
