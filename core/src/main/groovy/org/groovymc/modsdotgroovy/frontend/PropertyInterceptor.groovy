package org.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileStatic
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.core.versioning.VersionRange
import org.groovymc.modsdotgroovy.core.versioning.VersionRangeAware

/**
 * Intercepts property access on the frontend and automatically delegates it to the backend.<br>
 * Classes that use this trait to need to implement a non-null {@code private final ModsDotGroovyCore core}.
 */
@CompileStatic
trait PropertyInterceptor {
    abstract ModsDotGroovyCore getCore()

    @Lazy
    private final Logger log = LogManager.getLogger('MDG - Frontend')

    void setProperty(final String name, def value) {
        log.debug "setProperty(name: $name, value: $value) stack: ${core.getStack()}"

        // if the value is a String and the field is annotated with @VersionRangeAware, convert it to a VersionRange
        if (value instanceof String || value instanceof GString) {
            try {
                if (this.class.getDeclaredField(name).isAnnotationPresent(VersionRangeAware)) {
                    core.put(name, VersionRange.of(value.toString()))
                    return
                }
            } catch (final NoSuchFieldException ignored) {}
        }

        //if (this.hasProperty(name)) this.@"$name" = value
        core.put(name, value)
    }
}
