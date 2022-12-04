package ga.ozli.projects.flexiblemodsdotgroovy

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import static groovy.lang.Closure.DELEGATE_FIRST

@CompileStatic
class ModsBuilder extends PluginAwareMap {
    private List mods = [] // todo: make a PluginAwareList

    ModsBuilder(PluginAwareMap parent) {
        super(parent)
    }

    List/*<Map<String, ?>>*/ getMods() {
        return mods
    }

    void modInfo(@DelegatesTo(value = ModInfoBuilder, strategy = DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'modsdotgroovy.ModInfoBuilder') final Closure closure) {
        println 'frontend called modInfo'
        mods << put('modInfo', new Tuple2<PluginAwareMap, Closure>(this, closure))
    }

    void mod(@DelegatesTo(value = ModInfoBuilder, strategy = DELEGATE_FIRST)
             @ClosureParams(value = SimpleType, options = 'modsdotgroovy.ModInfoBuilder') final Closure closure) {
        println 'frontend called mod'
        mods << put('mod', new Tuple2<PluginAwareMap, Closure>(this, closure))
    }
}
