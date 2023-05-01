package io.github.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.jetbrains.annotations.Nullable
import io.github.groovymc.modsdotgroovy.core.ModsDotGroovyCore

@CompileStatic
@Log4j2(category = 'MDG - Frontend')
class DependenciesBuilder implements PropertyInterceptor, MapClosureInterceptor {
    private final ModsDotGroovyCore core

    @Nullable String forge
    @Nullable String minecraft

    DependenciesBuilder() {
        log.debug "new io.github.groovymc.modsdotgroovy.frontend.DependenciesBuilder()"
        this.core = null
    }

    DependenciesBuilder(final ModsDotGroovyCore core) {
        log.debug "new io.github.groovymc.modsdotgroovy.frontend.DependenciesBuilder(core: $core)"
        this.core = core
    }
}
