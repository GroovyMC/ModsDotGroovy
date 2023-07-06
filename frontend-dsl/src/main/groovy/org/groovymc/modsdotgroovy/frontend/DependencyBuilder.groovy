package org.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore

@CompileStatic
@Log4j2(category = 'MDG - Frontend')
class DependencyBuilder implements PropertyInterceptor, MapClosureInterceptor {
    private final ModsDotGroovyCore core

    /**@
     * The ID of the mod this dependency is depending on.
     */
    String modId

    /**@
     * Does this dependency have to exist? If not, ordering must also be specified.
     */
    boolean mandatory = true

    /**@
     * A version range of the versions of the mod you're compatible with.
     */
    String versionRange

    /**@
     * An ordering relationship for the dependency - BEFORE or AFTER required if the relationship is not mandatory
     */
    String ordering = 'NONE'

    /**@
     * Side this dependency is applied on - BOTH, CLIENT or SERVER
     */
    String side = 'BOTH'

    @SuppressWarnings('GroovyUnusedDeclaration') // Used by the Groovy compiler for coercing an implicit `it` closure
    DependencyBuilder() {
        log.debug "new org.groovymc.modsdotgroovy.frontend.DependencyBuilder()"
        this.core = null
    }

    DependencyBuilder(final ModsDotGroovyCore core) {
        log.debug "new org.groovymc.modsdotgroovy.frontend.DependencyBuilder(core: $core)"
        this.core = core
    }
}
