package org.groovymc.modsdotgroovy.frontend.neoforge

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

@CompileStatic
@Log4j2(category = 'MDG - NeoForge Frontend')
class AccessTransformerBuilder extends DslBuilder implements PropertyInterceptor {
    /**@
     * The path to the access transformer file inside your mod's JAR.
     */
    String file

    AccessTransformerBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
