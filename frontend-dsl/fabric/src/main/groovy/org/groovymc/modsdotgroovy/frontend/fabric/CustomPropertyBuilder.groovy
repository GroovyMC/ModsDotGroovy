package org.groovymc.modsdotgroovy.frontend.fabric

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

@CompileStatic
@Log4j2(category = 'MDG - Fabric Frontend')
class CustomPropertyBuilder extends DslBuilder implements PropertyInterceptor {
    @CompileDynamic
    void property(final String name, final String value) {
        this."$name" = value
    }

    CustomPropertyBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
