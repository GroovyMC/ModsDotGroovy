package org.groovymc.modsdotgroovy.frontend.quilt

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.core.versioning.VersionRange
import org.groovymc.modsdotgroovy.core.versioning.VersionRangeAware
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

@CompileStatic
@Log4j2(category = 'MDG - Quilt Frontend')
class DependenciesBuilder extends DslBuilder implements PropertyInterceptor {
    void mod(final String id, def versions) {
        core.put(id, versions)
    }

    void mod(@DelegatesTo(value = DependencyBuilder, strategy = Closure.DELEGATE_FIRST)
             @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.DependencyBuilder')
             final Closure closure) {
        log.debug 'mod(closure)'
        core.push('mod')
        final dependencyBuilder = new DependencyBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = dependencyBuilder
        closure.call(dependencyBuilder)
        core.pop()
    }

    void mod(final String id,
             @DelegatesTo(value = DependencyBuilder, strategy = Closure.DELEGATE_FIRST)
             @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.DependencyBuilder')
             final Closure closure) {
        log.debug 'mod(string, closure)'
        core.push('mod')
        final dependencyBuilder = new DependencyBuilder(core)
        core.put('id', id)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = dependencyBuilder
        closure.call(dependencyBuilder)
        core.pop()
    }

    DependenciesBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
