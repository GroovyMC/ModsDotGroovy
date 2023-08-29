package org.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.versioning.VersionRange
import org.groovymc.modsdotgroovy.core.versioning.VersionRangeAware

/**
 * Intercepts property access on the frontend and automatically delegates it to the backend.<br>
 * Classes that use this trait to need to implement a non-null {@code private final ModsDotGroovyCore core}.
 */
@CompileStatic
@Log4j2(category = 'MDG - Frontend')
trait PropertyInterceptor {
    @CompileDynamic
    void setProperty(final String name, def value) {
        log.debug "setProperty(name: $name, value: $value) stack: ${core.getStack()}"

        // if the value is a String and the field is annotated with @VersionRangeAware, convert it to a VersionRange
        if (value instanceof String) {
            try {
                if (this.class.getDeclaredField(name).isAnnotationPresent(VersionRangeAware)) {
                    core.put(name, new VersionRange(value))
                    return
                }
            } catch (final NoSuchFieldException ignored) {}
        }

        //if (this.hasProperty(name)) this.@"$name" = value
        core.put(name, value)
    }
}
