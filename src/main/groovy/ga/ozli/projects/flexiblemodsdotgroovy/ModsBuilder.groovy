package ga.ozli.projects.flexiblemodsdotgroovy

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import static groovy.lang.Closure.DELEGATE_FIRST

@CompileStatic
class ModsBuilder {
    private List mods = []

    List getMods() {
        return mods
    }

    void modInfo(@DelegatesTo(value = ModInfoBuilder, strategy = DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'modsdotgroovy.ModInfoBuilder') final Closure closure) {
        final modInfoBuilder = new ModInfoBuilder(ModsDotGroovyCore.INSTANCE)
        closure.delegate = modInfoBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(modInfoBuilder)
        mods << modInfoBuilder.build()
    }

    void mod(@DelegatesTo(value = ModInfoBuilder, strategy = DELEGATE_FIRST)
             @ClosureParams(value = SimpleType, options = 'modsdotgroovy.ModInfoBuilder') final Closure closure) {
        modInfo(closure)
    }
}
