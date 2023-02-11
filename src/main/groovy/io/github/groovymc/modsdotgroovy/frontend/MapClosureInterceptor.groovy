package io.github.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileDynamic

/**
 * Intercepts map-style closure calls on the frontend and automatically delegates it to the backend.<br>
 * Useful for allowing the frontend to still delegate unrecognised nests to the backend to be handled by plugins.<br>
 * Classes that use this trait to need to implement a non-null {@code private final ModsDotGroovyCore core}.
 */
trait MapClosureInterceptor {
    @CompileDynamic
    void methodMissing(final String name, def args) {
        args = args as List
        println "[Frontend] methodMissing(name: $name, args: $args) stack: ${core.stack}"
        if (args.size() > 0 && args[0] instanceof Closure) {
            final closure = args[0] as Closure
            core.push(name)
            final map = [:].withTraits(PropertyInterceptor, MapClosureInterceptor)
            closure.delegate = map
            closure.call(map)
            core.pop()
        }
    }
}
