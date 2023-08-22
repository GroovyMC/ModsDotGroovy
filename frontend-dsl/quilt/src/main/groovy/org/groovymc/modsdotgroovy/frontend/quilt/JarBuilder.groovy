package org.groovymc.modsdotgroovy.frontend.quilt

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

@CompileStatic
@Log4j2(category = 'MDG - Quilt Frontend')
class JarBuilder extends DslBuilder implements PropertyInterceptor {
    /**@
     * The path to the jar dependency
     */
    String file

    JarBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
