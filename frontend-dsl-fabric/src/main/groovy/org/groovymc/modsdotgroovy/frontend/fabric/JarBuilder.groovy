package org.groovymc.modsdotgroovy.frontend.fabric

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

@CompileStatic
@Log4j2(category = 'MDG - Fabric Frontend')
class JarBuilder implements PropertyInterceptor {
    private final ModsDotGroovyCore core

    /**@
     * The path to the jar dependency
     */
    String file

    @SuppressWarnings('GroovyUnusedDeclaration') // Used by the Groovy compiler for coercing an implicit `it` closure
    JarBuilder() {
        log.debug "new org.groovymc.modsdotgroovy.frontend.fabric.JarBuilder()"
        this.core = null
    }

    JarBuilder(final ModsDotGroovyCore core) {
        log.debug "new org.groovymc.modsdotgroovy.frontend.fabric.JarBuilder(core: $core)"
        this.core = core
    }
}
