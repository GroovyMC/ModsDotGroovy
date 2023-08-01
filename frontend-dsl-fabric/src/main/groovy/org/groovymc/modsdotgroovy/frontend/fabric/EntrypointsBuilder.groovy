package org.groovymc.modsdotgroovy.frontend.fabric

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.MapClosureInterceptor
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

@CompileStatic
@Log4j2(category = 'MDG - Fabric Frontend')
class EntrypointsBuilder implements PropertyInterceptor, MapClosureInterceptor {
    private final ModsDotGroovyCore core

    void main(@DelegatesTo(value = EntrypointBuilder, strategy = Closure.DELEGATE_FIRST)
              @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.EntrypointBuilder')
              final Closure closure) {
        entrypoint('main', closure)
    }

    void main(final String value) {
        entrypoint('main', value)
    }

    void client(final String value) {
        entrypoint('client', value)
    }

    void server(final String value) {
        entrypoint('server', value)
    }

    void client(@DelegatesTo(value = EntrypointBuilder, strategy = Closure.DELEGATE_FIRST)
                @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.EntrypointBuilder')
                final Closure closure) {
        entrypoint('client', closure)
    }

    void server(@DelegatesTo(value = EntrypointBuilder, strategy = Closure.DELEGATE_FIRST)
                @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.EntrypointBuilder')
                final Closure closure) {
        entrypoint('server', closure)
    }

    void entrypoint(final String type,
                    @DelegatesTo(value = EntrypointBuilder, strategy = Closure.DELEGATE_FIRST)
                    @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.fabric.EntrypointBuilder')
                    final Closure closure) {
        log.debug "entrypoint(closure)"
        core.push('entrypoint')
        core.put('type', type)
        final entrypointBuilder = new EntrypointBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = entrypointBuilder
        closure.call(entrypointBuilder)
        core.pop()
    }

    void entrypoint(final String name, final String value) {
        core.push('entrypoint')
        core.put('type', name)
        core.put('value', value)
        core.pop()
    }

    @CompileDynamic
    void setProperty(final String name, final def value) {
        entrypoint(name, value)
    }

    @CompileDynamic
    void methodMissing(final String name, def args) {
        args = args as List
        log.debug "methodMissing(name: $name, args: $args) stack: ${core.getStack()}"
        if (args.size() > 0 && args[0] instanceof String) {
            entrypoint(name, args[0] as String)
        }
    }

    @SuppressWarnings('GroovyUnusedDeclaration') // Used by the Groovy compiler for coercing an implicit `it` closure
    EntrypointsBuilder() {
        log.debug "new org.groovymc.modsdotgroovy.frontend.fabric.EntrypointsBuilder()"
        this.core = null
    }

    EntrypointsBuilder(final ModsDotGroovyCore core) {
        log.debug "new org.groovymc.modsdotgroovy.frontend.fabric.EntrypointsBuilder(core: $core)"
        this.core = core
    }
}
