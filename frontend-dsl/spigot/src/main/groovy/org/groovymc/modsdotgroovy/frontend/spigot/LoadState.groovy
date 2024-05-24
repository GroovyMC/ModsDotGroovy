package org.groovymc.modsdotgroovy.frontend.spigot

import groovy.transform.CompileStatic
import org.groovymc.rootpackagetransformer.RootPackage

@CompileStatic
@RootPackage
enum LoadState {
    STARTUP,
    POSTWORLD

    /**@
     * Allows POST_WORLD to also work as an alias.
     */
    static final LoadState POST_WORLD = POSTWORLD

    LoadState() {}
}
