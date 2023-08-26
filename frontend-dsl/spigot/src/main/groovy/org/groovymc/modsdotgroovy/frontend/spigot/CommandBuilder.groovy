package org.groovymc.modsdotgroovy.frontend.spigot

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor
import org.jetbrains.annotations.Nullable

@CompileStatic
@Log4j2(category = 'MDG - Spigot Frontend')
class CommandBuilder extends DslBuilder implements PropertyInterceptor {
    /**@
     * The name of a command your plugin wishes to register.
     * <p>The command name should not contain the leading '/' required to issue a command.</p>
     */
    String name

    /**@
     * A short description of what the command does. Can be used in conjuction with {@code /help}
     */
    @Nullable String description = null

    /**@
     * An alternate command name that a user may use to issue the command instead of the primary name.
     */
    @Nullable String alias = null

    /**@
     * A list of alternate command names that a user may use to issue the command instead of the primary name.
     */
    @Nullable List<String> aliases = null

    /**@
     * The most basic permission node required to use the command.
     * <p>This permission node can be used to determine if a user should be able to see this command.</p>
     * <p>Alias for {@link #permission(Closure)}
     */
    @Nullable String permission = null

    /**@
     * A no-permission message which is displayed to a user if they do not have the required permission to use this command.
     * <p>You may use empty quotes to indicate nothing should be displayed.</p>
     * <p>Alias for {@link #permission(Closure)}
     */
    @Nullable String permissionMessage = null

    /**@
     * A short description of how to use this command.
     * <p>Displayed to whoever issued the command when the plugin's command handler (onCommand typically) does not return true.</p>
     * <p>{@code <command> or ${command}} is a macro that is replaced with the command issued wherever it occurs.</p>
     * <p>For example, {@code "Usage: /${command} [start|stop]"} would be displayed as {@code "Usage: /test [start|stop]"}
     * if the command name is {@code "test"}.</p>
     */
    @Nullable String usage = null

    void permission(@DelegatesTo(value = CommandPermissionBuilder, strategy = Closure.DELEGATE_FIRST)
                    @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.spigot.CommandPermissionBuilder')
                    final Closure closure) {
        log.debug 'permission(closure)'
        core.push('permission')
        final commandPermissionBuilder = new CommandPermissionBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = commandPermissionBuilder
        closure.call(commandPermissionBuilder)
        core.pop()
    }

    /**@
     * Alias for {@link #alias}
     */
    void setAliases(final String aliases) {
        this.alias = aliases
    }

    void setAliases(final List<String> aliases) {
        this.aliases = aliases
    }

    /**@
     * Alias for {@code <command>} for use in {@link #usage}. Helps avoid typos by having an IDE hint.
     */
    static String getCommand() {
        return '<command>'
    }

    CommandBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
