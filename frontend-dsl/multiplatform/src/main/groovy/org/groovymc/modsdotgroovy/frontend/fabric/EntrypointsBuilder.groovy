package org.groovymc.modsdotgroovy.frontend.fabric

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.MapClosureInterceptor
import org.groovymc.modsdotgroovy.frontend.OnPlatform
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

@CompileStatic
@Log4j2(category = 'MDG - Multiloader Frontend')
class EntrypointsBuilder extends DslBuilder implements PropertyInterceptor, MapClosureInterceptor, OnPlatform {
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
        core.push('entrypoint')
        core.put('type', name)
        core.put('value', value)
        core.put('replace', true)
        core.pop()
    }

    @CompileDynamic
    void methodMissing(final String name, def args) {
        args = args as List
        log.debug "methodMissing(name: $name, args: $args) stack: ${core.getStack()}"
        if (args.size() > 0 && args[0] instanceof String) {
            entrypoint(name, args[0] as String)
        }
    }

    EntrypointsBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
