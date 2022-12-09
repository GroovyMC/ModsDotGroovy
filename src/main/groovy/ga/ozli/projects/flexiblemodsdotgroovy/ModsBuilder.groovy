package ga.ozli.projects.flexiblemodsdotgroovy

import ga.ozli.projects.flexiblemodsdotgroovy.frontend.PropertyInterceptor
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import static groovy.lang.Closure.DELEGATE_ONLY

@CompileStatic
class ModsBuilder implements PropertyInterceptor {
    private final ModsDotGroovyCore core

    boolean insideModsBuilder = true

    void modInfo(@DelegatesTo(value = ModInfoBuilder, strategy = DELEGATE_ONLY)
                 @ClosureParams(value = SimpleType, options = 'ga.ozli.projects.flexiblemodsdotgroovy.ModInfoBuilder')
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
        println "[Frontend] new ModsBuilder()"
        this.core = null
    }

    ModsBuilder(final ModsDotGroovyCore core) {
        println "[Frontend] new ModsBuilder(core: $core)"
        this.core = core
    }
}
