package org.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.jetbrains.annotations.Nullable

import static groovy.lang.Closure.DELEGATE_FIRST

@CompileStatic
@Log4j2(category = 'MDG - Frontend')
class DependenciesBuilder implements MapClosureInterceptor {
    private final ModsDotGroovyCore core

    @Nullable String forge
    @Nullable String minecraft

    @SuppressWarnings('GroovyUnusedDeclaration') // Used by the Groovy compiler for coercing an implicit `it` closure
    DependenciesBuilder() {
        log.debug "new org.groovymc.modsdotgroovy.frontend.DependenciesBuilder()"
        this.core = null
    }

    DependenciesBuilder(final ModsDotGroovyCore core) {
        log.debug "new org.groovymc.modsdotgroovy.frontend.DependenciesBuilder(core: $core)"
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

    void mod(final String modId,
             @DelegatesTo(value = DependencyBuilder, strategy = DELEGATE_FIRST)
             @ClosureParams(value = SimpleType, options = 'modsdotgroovy.DependencyBuilder') final Closure closure) {
        log.debug "mod(string, closure)"
        core.push('dependency')
        final dependencyBuilder = new DependencyBuilder(core)
        core.put('modId', modId)
        closure.delegate = dependencyBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(dependencyBuilder)
        core.pop()
    }

    @CompileDynamic
    void setProperty(final String name, final def value) {
        mod {
            modId = name
            versionRange = value
        }
    }
}
