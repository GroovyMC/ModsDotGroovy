package org.groovymc.modsdotgroovy.frontend.quilt

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
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
    void setVersion(final String version) {
        core.put('versions', version)
    }

    void setVersions(final String versions) {
        core.put('versions', versions)
    }

    @Deprecated
    void setVersions(final List<String> versions) {
        core.put('versions', versions)
    }

    // todo: support object type for versions field. https://github.com/QuiltMC/rfcs/blob/main/specification/0002-quilt.mod.json.md#object

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
