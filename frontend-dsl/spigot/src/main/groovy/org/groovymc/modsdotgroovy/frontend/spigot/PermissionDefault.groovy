package org.groovymc.modsdotgroovy.frontend.spigot

import groovy.transform.CompileStatic
import org.groovymc.rootpackagetransformer.RootPackage

@CompileStatic
@RootPackage
enum PermissionDefault {
    /**@
     * Default to always granting the player the permission.
     */
    TRUE,

    /**@
     * Default to not granting the player the permission.
     */
    FALSE,

    /**@
     * Default will be true if the player is op.
     */
    OP,

    /**@
     * Opposite of {@link #OP} - default will be false if the player is op.
     */
    NOT_OP('not op')

    public final String value

    PermissionDefault(final String value) {
        this.value = value
    }

    PermissionDefault() {
        this.value = name().toLowerCase()
    }
}
