package org.groovymc.modsdotgroovy.frontend.multiplatform.fabric

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor
import org.groovymc.modsdotgroovy.frontend.multiplatform.Environment
import org.groovymc.modsdotgroovy.frontend.multiplatform.OnPlatform

@CompileStatic
@Log4j2(category = 'MDG - NeoForge Frontend')
class MixinBuilder extends DslBuilder implements PropertyInterceptor, OnPlatform {
    /**@
     * The path to the mixin configuration file inside your mod's JAR.
     */
    String config

    /**@
     * The same as upper level environment field.
     */
    Environment environment = Environment.ANY

    MixinBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
