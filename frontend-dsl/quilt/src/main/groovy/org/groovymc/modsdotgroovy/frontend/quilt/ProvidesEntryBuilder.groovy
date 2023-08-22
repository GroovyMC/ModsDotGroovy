package org.groovymc.modsdotgroovy.frontend.quilt

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor
import org.jetbrains.annotations.Nullable

@CompileStatic
@Log4j2(category = 'MDG - Quilt Frontend')
class ProvidesEntryBuilder extends DslBuilder implements PropertyInterceptor {
    /**@
     * A mod identifier in the form of either {@code mavenGroup:modId} or {@code modId}.
     */
    String id

    /**@
     * Should be a valid mod version. If omitted, then this defaults to the version of the providing mod.
     */
    @Nullable String version = null

    ProvidesEntryBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
