import groovy.transform.CompileStatic

@CompileStatic
enum Environment {
    /**@
     * Runs everywhere. Default.
     */
    ANY("*"),

    /**@
     * Runs on the physical client.
     */
    CLIENT("client"),

    /**@
     * Runs on the dedicated server.
     */
    SERVER("dedicated_server")
    
    public final String value

    Environment(final String value) {
        this.value = value
    }

    @Override
    String toString() {
        return value
    }
}
