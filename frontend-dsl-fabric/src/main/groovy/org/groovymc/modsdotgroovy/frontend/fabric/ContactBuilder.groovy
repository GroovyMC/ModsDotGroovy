package org.groovymc.modsdotgroovy.frontend.fabric

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

@CompileStatic
@Log4j2(category = 'MDG - Fabric Frontend')
class ContactBuilder implements PropertyInterceptor {
    private final ModsDotGroovyCore core

    /**@
     * Contact e-mail pertaining to the mod. Must be a valid e-mail address.
     */
    String email

    /**@
     * IRC channel pertaining to the mod. Must be of a valid URL format - for example: irc://irc.esper.net:6667/charset for #charset at EsperNet - the port is optional, and assumed to be 6667 if not present.
     */
    String irc

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

    @SuppressWarnings('GroovyUnusedDeclaration') // Used by the Groovy compiler for coercing an implicit `it` closure
    ContactBuilder() {
        log.debug "new org.groovymc.modsdotgroovy.frontend.fabric.ContactBuilder()"
        this.core = null
    }

    ContactBuilder(final ModsDotGroovyCore core) {
        log.debug "new org.groovymc.modsdotgroovy.frontend.fabric.ContactBuilder(core: $core)"
        this.core = core
    }
}
