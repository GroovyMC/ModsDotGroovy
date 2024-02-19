package org.groovymc.modsdotgroovy.frontend.quilt

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

@CompileStatic
@Log4j2(category = 'MDG - Quilt Frontend')
class LicenseBuilder extends DslBuilder implements PropertyInterceptor {
    String name

    /**
     * An SPDX license identifier
     */
    String id

    String url

    String description

    LicenseBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
