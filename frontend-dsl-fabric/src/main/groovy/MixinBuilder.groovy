import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

@CompileStatic
@Log4j2(category = 'MDG - Fabric Frontend')
class MixinBuilder implements PropertyInterceptor {
    private final ModsDotGroovyCore core

    /**@
     * The path to the mixin configuration file inside your mod's JAR.
     */
    String config

    /**@
     * The same as upper level environment field.
     */
    Environment environment = Environment.ANY

    @SuppressWarnings('GroovyUnusedDeclaration') // Used by the Groovy compiler for coercing an implicit `it` closure
    MixinBuilder() {
        log.debug "new MixinBuilder()"
        this.core = null
    }

    MixinBuilder(final ModsDotGroovyCore core) {
        log.debug "new MixinBuilder(core: $core)"
        this.core = core
    }
}
