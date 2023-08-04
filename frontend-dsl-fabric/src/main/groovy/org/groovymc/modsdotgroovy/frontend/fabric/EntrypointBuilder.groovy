package org.groovymc.modsdotgroovy.frontend.fabric

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

@CompileStatic
@Log4j2(category = 'MDG - Fabric Frontend')
class EntrypointBuilder extends DslBuilder implements PropertyInterceptor {
    String adapter

    String value

    EntrypointBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
