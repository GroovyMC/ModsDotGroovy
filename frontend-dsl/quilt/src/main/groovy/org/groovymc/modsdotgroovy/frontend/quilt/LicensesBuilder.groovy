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
class LicensesBuilder extends DslBuilder implements PropertyInterceptor {
    void license(String string) {
        log.debug "license(string)"
        core.put('license', string)
    }

    void license(@DelegatesTo(value = LicenseBuilder, strategy = Closure.DELEGATE_FIRST)
                     @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.LicenseBuilder')
                     final Closure closure) {
        log.debug "license(closure)"
        core.push('license')
        final licenseBuilder = new LicenseBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = licenseBuilder
        closure.call(licenseBuilder)
        core.pop()
    }

    LicensesBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
