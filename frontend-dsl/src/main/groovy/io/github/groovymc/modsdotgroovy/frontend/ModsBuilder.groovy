package io.github.groovymc.modsdotgroovy.frontend

import io.github.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import static groovy.lang.Closure.DELEGATE_ONLY

@CompileStatic
class ModsBuilder implements PropertyInterceptor, MapClosureInterceptor {
    private final ModsDotGroovyCore core

    boolean insideModsBuilder = true

    void modInfo(@DelegatesTo(value = ModInfoBuilder, strategy = DELEGATE_ONLY)
                 @ClosureParams(value = SimpleType, options = 'io.github.groovymc.modsdotgroovy.frontend.ModInfoBuilder')
                 final Closure closure) {
        println "[Frontend] modInfo(closure)"
        core.push('modInfo')
        final modInfoBuilder = new ModInfoBuilder(core)
        closure.resolveStrategy = DELEGATE_ONLY
        closure.delegate = modInfoBuilder
        closure.call(modInfoBuilder)
        core.pop()
    }

    @SuppressWarnings('GroovyUnusedDeclaration') // Used by the Groovy compiler for coercing an implicit `it` closure
    ModsBuilder() {
        println "[Frontend] new io.github.groovymc.modsdotgroovy.frontend.ModsBuilder()"
        this.core = null
    }

    ModsBuilder(final ModsDotGroovyCore core) {
        println "[Frontend] new io.github.groovymc.modsdotgroovy.frontend.ModsBuilder(core: $core)"
        this.core = core
    }
}
