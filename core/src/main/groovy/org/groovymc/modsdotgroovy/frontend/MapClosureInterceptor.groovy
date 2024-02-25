package org.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileStatic
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore

/**
 * Intercepts map-style closure calls on the frontend and automatically delegates it to the backend.<br>
 * Useful for allowing the frontend to still delegate unrecognised nests to the backend to be handled by plugins.<br>
 * Classes that use this trait to need to implement a non-null {@code private final ModsDotGroovyCore core}.
 */
@CompileStatic
trait MapClosureInterceptor {
    abstract ModsDotGroovyCore getCore()

    @Lazy
    private final Logger log = LogManager.getLogger('MDG - Frontend')

    void methodMissing(final String name, def args) {
        args = args as Object[]
        log.debug "methodMissing(name: $name, args: $args) stack: ${core.layeredMap.stack}"
        if (args.length === 1 && args[0] instanceof Closure) {
            final closure = args[0] as Closure
            core.push(name)
            final map = new MapClosureDslBuilder(core)
            closure.delegate = map
            closure.call(map)
            core.pop()
        }
    }
}
