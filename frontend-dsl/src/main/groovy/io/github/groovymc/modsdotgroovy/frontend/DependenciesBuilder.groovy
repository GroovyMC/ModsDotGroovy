package io.github.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.jetbrains.annotations.Nullable
import io.github.groovymc.modsdotgroovy.core.ModsDotGroovyCore

import static groovy.lang.Closure.DELEGATE_FIRST

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

    void mod(@DelegatesTo(value = DependencyBuilder, strategy = DELEGATE_FIRST)
             @ClosureParams(value = SimpleType, options = 'modsdotgroovy.DependencyBuilder')
             final Closure closure) {
        log.debug "mod(closure)"
        core.push('dependency')
        final dependencyBuilder = new DependencyBuilder(core)
        closure.delegate = dependencyBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(dependencyBuilder)
        core.pop()
    }
}
