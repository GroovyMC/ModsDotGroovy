package org.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.jetbrains.annotations.ApiStatus

@PackageScope
@CompileStatic
@ApiStatus.Internal
@Log4j2(category = 'MDG - Internal')
final class MapClosureDslBuilder extends DslBuilder implements PropertyInterceptor, MapClosureInterceptor {
    MapClosureDslBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
