package ga.ozli.projects.flexiblemodsdotgroovy

import ga.ozli.projects.flexiblemodsdotgroovy.frontend.PropertyInterceptor
import groovy.transform.CompileStatic

@CompileStatic
class ModInfoBuilder implements PropertyInterceptor {
    private final ModsDotGroovyCore core

    String modId = null

    ModInfoBuilder() {
        println "[Frontend] new ModInfoBuilder()"
        this.core = null
    }

    ModInfoBuilder(final ModsDotGroovyCore core) {
        println "[Frontend] new ModInfoBuilder(core: $core)"
        this.core = core
    }
}
