package org.groovymc.modsdotgroovy.frontend.fabric

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor
import org.groovymc.rootpackagetransformer.RootPackage

@CompileStatic
@Log4j2(category = 'MDG - Fabric Frontend')
@RootPackage
class MixinBuilder extends DslBuilder implements PropertyInterceptor {
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
