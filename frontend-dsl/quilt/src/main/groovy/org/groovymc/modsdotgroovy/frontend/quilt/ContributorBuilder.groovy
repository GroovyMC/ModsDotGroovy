package org.groovymc.modsdotgroovy.frontend.quilt

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

@CompileStatic
@Log4j2(category = 'MDG - Quilt Frontend')
class ContributorBuilder extends DslBuilder implements PropertyInterceptor {
    /**@
     * The name of the person or organisation that contributed to this project.
     */
    String name

    /**@
     * A list of roles the contributor has.
     */
    List<String> roles = ['Contributor']

    /**@
     * An alternative to {@link #roles} for single role contributors.
     */
    String role = 'Contributor'

    ContributorBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
