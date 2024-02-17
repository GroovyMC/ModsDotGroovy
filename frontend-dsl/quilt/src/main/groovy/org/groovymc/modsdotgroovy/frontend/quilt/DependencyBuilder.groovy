package org.groovymc.modsdotgroovy.frontend.quilt

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.core.versioning.VersionRange
import org.groovymc.modsdotgroovy.core.versioning.VersionRangeAware
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor
import org.jetbrains.annotations.Nullable

@CompileStatic
@Log4j2(category = 'MDG - Quilt Frontend')
class DependencyBuilder extends DslBuilder implements PropertyInterceptor {
    /**@
     * A mod identifier in the form of either {@code mavenGroup:modId} or {@code modId}.
     */
    String id

    /**@
     * Alias for {@link #setVersions(String)}
     */
    void setVersion(def version) {
        setVersions(version)
    }

    void setVersions(def versions) {
        if (versions instanceof String || versions instanceof GString) {
            versions = VersionRange.of(versions as String)
        }
        core.put('versions', versions)
    }

    /**@
     * A short, human-readable reason for the dependency object to exist.
     */
    @Nullable String reason = null

    /**@
     * Dependencies marked as optional will only be checked if the mod/plugin specified by the {@link #id} field is present.
     */
    boolean optional = false

    /**@
     * Describes situations where this dependency can be ignored.
     */
    @Nullable String unless = null

    DependencyBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
