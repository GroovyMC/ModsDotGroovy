package org.groovymc.modsdotgroovy.frontend.fabric

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore

@CompileStatic
@Log4j2(category = 'MDG - Fabric Frontend')
class PersonsBuilder {
    private final ModsDotGroovyCore core
    private final String fieldName

    @SuppressWarnings('GroovyUnusedDeclaration') // Used by the Groovy compiler for coercing an implicit `it` closure
    PersonsBuilder() {
        log.debug "new org.groovymc.modsdotgroovy.frontend.fabric.PersonsBuilder()"
        this.core = null
        this.fieldName = null
    }

    PersonsBuilder(final ModsDotGroovyCore core, final String fieldName) {
        log.debug "new org.groovymc.modsdotgroovy.frontend.fabric.PersonsBuilder(core: $core)"
        this.core = core
        this.fieldName = fieldName
    }

    void person(final String name,
                @DelegatesTo(value = ContactBuilder, strategy = Closure.DELEGATE_FIRST)
                @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.ContactBuilder')
                final Closure contact = null) {
        log.debug "${fieldName}(name: $name)"
        core.push(fieldName)
        core.put('name', name)
        if (contact != null) {
            final personBuilder = new PersonBuilder(core)
            core.push("contact")
            contact.resolveStrategy = Closure.DELEGATE_FIRST
            contact.delegate = personBuilder
            contact.call(personBuilder)
            core.pop()
        }
        core.pop()
    }

    void person(@DelegatesTo(value = PersonBuilder, strategy = Closure.DELEGATE_FIRST)
                @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.PersonBuilder')
                final Closure closure) {
        log.debug "${fieldName}(closure)"
        core.push(fieldName)
        final personBuilder = new PersonBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = personBuilder
        closure.call(personBuilder)
        core.pop()
    }
}