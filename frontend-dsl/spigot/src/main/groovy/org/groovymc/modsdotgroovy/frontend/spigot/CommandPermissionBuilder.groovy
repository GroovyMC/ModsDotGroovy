package org.groovymc.modsdotgroovy.frontend.spigot

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor
import org.jetbrains.annotations.Nullable

@CompileStatic
@Log4j2(category = 'MDG - Spigot Frontend')
class CommandPermissionBuilder extends DslBuilder implements PropertyInterceptor {
    /**@
     * The most basic permission node required to use the command.
     * <p>This permission node can be used to determine if a user should be able to see this command.</p>
     */
    String node

    /**@
     * A no-permission message which is displayed to a user if they do not have the required permission to use this command.
     * <p>You may use empty quotes to indicate nothing should be displayed.</p>
     */
    @Nullable String message = null

    CommandPermissionBuilder(ModsDotGroovyCore core) {
        super(core)
    }
}
