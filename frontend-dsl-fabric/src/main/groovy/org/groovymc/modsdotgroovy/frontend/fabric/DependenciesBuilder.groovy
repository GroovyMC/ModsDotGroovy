package org.groovymc.modsdotgroovy.frontend.fabric


import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

@CompileStatic
@Log4j2(category = 'MDG - Fabric Frontend')
class DependenciesBuilder implements PropertyInterceptor {
    private final ModsDotGroovyCore core

    void mod(final String modId, final def versionRange) {
        core.put(modId, versionRange)
    }

    void mod(@DelegatesTo(value = DependencyBuilder, strategy = Closure.DELEGATE_FIRST)
             @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.DependencyBuilder')
             final Closure closure) {
        log.debug 'mod(closure)'
        core.push('mod')
        final dependencyBuilder = new DependencyBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = dependencyBuilder
        closure.call(dependencyBuilder)
        core.pop()
    }

    @SuppressWarnings('GroovyUnusedDeclaration') // Used by the Groovy compiler for coercing an implicit `it` closure
    DependenciesBuilder() {
        log.debug "new org.groovymc.modsdotgroovy.frontend.fabric.DependenciesBuilder()"
        this.core = null
    }

    DependenciesBuilder(final ModsDotGroovyCore core) {
        log.debug "new org.groovymc.modsdotgroovy.frontend.fabric.DependenciesBuilder(core: $core)"
        this.core = core
    }
}
