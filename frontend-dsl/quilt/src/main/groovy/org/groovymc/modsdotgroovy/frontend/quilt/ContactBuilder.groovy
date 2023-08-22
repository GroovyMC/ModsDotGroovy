package org.groovymc.modsdotgroovy.frontend.quilt

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

@CompileStatic
@Log4j2(category = 'MDG - Quilt Frontend')
class ContactBuilder extends DslBuilder implements PropertyInterceptor {
    /**@
     * Contact e-mail pertaining to the mod. Must be a valid e-mail address.
     */
    String email

    /**@
     * Project or user homepage. Must be a valid HTTP/HTTPS address.
     */
    String homepage

    /**@
     * Project issue tracker. Must be a valid HTTP/HTTPS address.
     */
    String issues

    /**@
     * Project source code repository. Must be a valid URL - it can, however, be a specialized URL for a given VCS (such as Git or Mercurial).
     */
    String sources

    ContactBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
