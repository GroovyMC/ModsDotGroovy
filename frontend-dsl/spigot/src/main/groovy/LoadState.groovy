import groovy.transform.CompileStatic

@CompileStatic
enum LoadState {
    STARTUP,
    POSTWORLD

    /**@
     * Allows POST_WORLD to also work as an alias.
     */
    static final LoadState POST_WORLD = POSTWORLD

    LoadState() {}
}
