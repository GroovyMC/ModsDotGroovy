package org.groovymc.modsdotgroovy.frontend.spigot

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor
import org.jetbrains.annotations.Nullable

@CompileStatic
@Log4j2(category = 'MDG - Spigot Frontend')
class PermissionBuilder extends DslBuilder implements PropertyInterceptor {
    /**@
     * The permission node to register.
     */
    String node

    /**@
     * A short description of what this permission allows. Allows programmatic access and helps server administrators.
     */
    @Nullable String description = null

    /**@
     * Sets the default value of the permission.
     */
    @Nullable PermissionDefault permissionDefault = PermissionDefault.OP

    /**@
     * Sets the children of this permission. Child node keys are permission names.
     * <p>If the value is {@code true}, the child permission inherits the parent permission.</p>
     * <p>If the value is {@code false}, the child permission inherits the <i>inverse</i> parent permission.</p>
     * <p>Cam also contain other permission nodes.</p>
     */
    @Nullable Map<String, Boolean> children = null

    PermissionBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
