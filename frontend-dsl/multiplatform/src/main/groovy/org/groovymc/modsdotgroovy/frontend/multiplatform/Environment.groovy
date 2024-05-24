package org.groovymc.modsdotgroovy.frontend.multiplatform

import groovy.transform.CompileStatic
import org.groovymc.rootpackagetransformer.RootPackage

@CompileStatic
@RootPackage
enum Environment {
    /**@
     * Runs everywhere. Default.
     */
    ANY("*"),

    /**@
     * Runs on the client side.
     */
    CLIENT("client"),

    /**@
     * Runs on the server side.
     */
    SERVER("server")
    
    public final String value

    Environment(final String value) {
        this.value = value
    }

    @Override
    String toString() {
        return value
    }
}
