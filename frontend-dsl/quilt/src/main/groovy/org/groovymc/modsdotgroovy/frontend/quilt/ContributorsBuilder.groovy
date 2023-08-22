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
class ContributorsBuilder extends DslBuilder implements PropertyInterceptor {
    void contributor(@DelegatesTo(value = ContributorBuilder, strategy = Closure.DELEGATE_FIRST)
                     @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.ContributorBuilder')
                     final Closure closure) {
        log.debug "contributor(closure)"
        core.push('contributor')
        final contributorBuilder = new ContributorBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = contributorBuilder
        closure.call(contributorBuilder)
        core.pop()
    }

    ContributorsBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
