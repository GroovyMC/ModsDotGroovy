package org.groovymc.modsdotgroovy.frontend.quilt

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.frontend.MapClosureInterceptor
import org.groovymc.modsdotgroovy.frontend.ModsDotGroovyFrontend
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor

import org.groovymc.modsdotgroovy.frontend.quilt.*
import org.groovymc.rootpackagetransformer.RootPackage
import org.jetbrains.annotations.Nullable

// Resources used:
// https://github.com/QuiltMC/rfcs/blob/main/specification/0002-quilt.mod.json.md
// https://modder.wiki.quiltmc.org/versions/1.19/getting_started/setup/migrating_from_fabric/

/**
 * This is the Quilt frontend layer
 */
@PackageScope
@CompileStatic
@Log4j2(category = 'MDG - Quilt Frontend')
@RootPackage
class QuiltModsDotGroovy extends ModsDotGroovyFrontend implements PropertyInterceptor, MapClosureInterceptor {
    /**@
     * Needed for internal mechanisms. Must always be 1.
     */
    int schemaVersion = 1

    /**@
     * A single path to a mixin configuration file relative to the root of the mod JAR.
     */
    @Nullable String mixin = null

    /**@
     * A list of paths to mixin configuration files relative to the root of the mod JAR.
     */
    void setMixins(final List<String> mixins) {
        core.put('mixin', mixins)
    }

    /**@
     * A single path to a access widener file relative to the root of the mod JAR.
     */
    @Nullable String accessWidener = null

    /**@
     * A list of paths to access widener files relative to the root of the mod JAR.
     */
    void setAccessWideners(final List<String> accessWideners) {
        core.put('accessWidener', accessWideners)
    }

    /**@
     * Contains flags and options related to Minecraft specifically.
     */
    void minecraft(@DelegatesTo(value = MinecraftBuilder, strategy = Closure.DELEGATE_FIRST)
                   @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.MinecraftBuilder')
                   final Closure closure) {
        log.debug 'minecraft(closure)'
        core.push('minecraft')
        final minecraftBuilder = new MinecraftBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = minecraftBuilder
        closure.call(minecraftBuilder)
        core.pop()
    }

    void quiltLoader(@DelegatesTo(value = QuiltLoaderBuilder, strategy = Closure.DELEGATE_FIRST)
                          @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.QuiltLoaderBuilder')
                          final Closure closure) {
        log.debug 'quiltLoader(closure)'
        core.push('quiltLoader')
        final quiltLoaderBuilder = new QuiltLoaderBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = quiltLoaderBuilder
        closure.call(quiltLoaderBuilder)
        core.pop()
    }

    private QuiltModsDotGroovy(final Map<String, ?> environment) {
        super(environment)
    }

    static QuiltModsDotGroovy make(@DelegatesTo(value = QuiltModsDotGroovy, strategy = Closure.DELEGATE_FIRST)
                                   @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.QuiltModsDotGroovy') final Closure closure,
                                   final Map<String, ?> environment = [:]) {
        final QuiltModsDotGroovy val = new QuiltModsDotGroovy(environment)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = val
        closure.call(val)
        return val
    }
}
