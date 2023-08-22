package org.groovymc.modsdotgroovy.frontend.quilt

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

@CompileStatic
@Log4j2(category = 'MDG - Quilt Frontend')
class JarsBuilder extends DslBuilder implements PropertyInterceptor {
    void jar(final String file) {
        log.debug "jar(file: $file)"
        core.push('jar')
        core.put('file', file)
        core.pop()
    }

    void jar(@DelegatesTo(value = JarBuilder, strategy = Closure.DELEGATE_FIRST)
             @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.JarBuilder')
             final Closure closure) {
        log.debug 'jar(closure)'
        core.push('jar')
        final jarBuilder = new JarBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = jarBuilder
        closure.call(jarBuilder)
        core.pop()
    }

    JarsBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
