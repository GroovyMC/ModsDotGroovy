package org.groovymc.modsdotgroovy.frontend.quilt

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor
import org.jetbrains.annotations.Nullable

@CompileStatic
@Log4j2(category = 'MDG - Quilt Frontend')
class EntrypointBuilder extends DslBuilder implements PropertyInterceptor {
    @Nullable String adapter = null

    String value

    EntrypointBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
