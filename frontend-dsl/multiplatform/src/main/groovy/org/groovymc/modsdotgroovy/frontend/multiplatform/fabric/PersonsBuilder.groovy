package org.groovymc.modsdotgroovy.frontend.multiplatform.fabric

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.multiplatform.OnPlatform

@CompileStatic
@Log4j2(category = 'MDG - Fabric Frontend')
class PersonsBuilder extends DslBuilder implements OnPlatform {
    private final String fieldName

    void person(final String name,
                @DelegatesTo(value = ContactBuilder, strategy = Closure.DELEGATE_FIRST)
                @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.multiplatform.fabric.ContactBuilder')
                final Closure closure = null) {
        log.debug "${fieldName}(name: $name)"
        core.push(fieldName)
        core.put('name', name)
        if (closure !== null) {
            core.push('contact')
            final contactBuilder = new ContactBuilder(core)
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure.delegate = contactBuilder
            closure.call(contactBuilder)
            core.pop()
        }
        core.pop()
    }

    void person(@DelegatesTo(value = PersonBuilder, strategy = Closure.DELEGATE_FIRST)
                @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.multiplatform.fabric.PersonBuilder')
                final Closure closure) {
        log.debug "${fieldName}(closure)"
        core.push(fieldName)
        final personBuilder = new PersonBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = personBuilder
        closure.call(personBuilder)
        core.pop()
    }

    PersonsBuilder(final ModsDotGroovyCore core, final String fieldName) {
        super(core)
        this.fieldName = fieldName
    }
}
