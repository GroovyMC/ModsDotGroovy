package io.github.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileDynamic

/**
 * Intercepts property access on the frontend and automatically delegates it to the backend.<br>
 * Classes that use this trait to need to implement a non-null {@code private final ModsDotGroovyCore core}.
 */
trait PropertyInterceptor {
    @CompileDynamic
    void setProperty(final String name, final def value) {
        println "[Frontend] setProperty(name: $name, value: $value) stack: ${core.stack}"
        if (this.hasProperty(name)) this.@"$name" = value
        core.put(name, value)
    }
}
