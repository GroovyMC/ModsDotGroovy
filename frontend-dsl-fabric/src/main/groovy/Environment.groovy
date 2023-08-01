

import groovy.transform.CompileStatic

@CompileStatic
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

    Environment(String value) {
        this.value = value
    }
}
