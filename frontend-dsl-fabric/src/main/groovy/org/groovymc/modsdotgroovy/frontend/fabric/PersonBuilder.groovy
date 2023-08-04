package org.groovymc.modsdotgroovy.frontend.fabric

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

@CompileStatic
@Log4j2(category = 'MDG - Fabric Frontend')
class PersonBuilder implements PropertyInterceptor {
    private final ModsDotGroovyCore core

    /**@
     * The real name, or username, of the person. Mandatory.
     */
    String name

    /**@
     * Person's contact information. The same as upper level contact. Optional.
     */
    void contact(@DelegatesTo(value = ContactBuilder, strategy = Closure.DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.ContactBuilder')
                 final Closure closure) {
        log.debug "contact(closure)"
        core.push('contact')
        final contactBuilder = new ContactBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = contactBuilder
        closure.call(contactBuilder)
        core.pop()
    }

    @SuppressWarnings('GroovyUnusedDeclaration') // Used by the Groovy compiler for coercing an implicit `it` closure
    PersonBuilder() {
        log.debug "new org.groovymc.modsdotgroovy.frontend.fabric.PersonBuilder()"
        this.core = null
    }

    PersonBuilder(final ModsDotGroovyCore core) {
        log.debug "new org.groovymc.modsdotgroovy.frontend.fabric.PersonBuilder(core: $core)"
        this.core = core
    }
}
