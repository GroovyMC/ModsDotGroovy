package io.github.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileStatic
import groovyjarjarantlr4.v4.runtime.misc.Nullable
import io.github.groovymc.modsdotgroovy.core.ModsDotGroovyCore

@CompileStatic
class DependenciesBuilder implements PropertyInterceptor, MapClosureInterceptor {
    private final ModsDotGroovyCore core

    @Nullable String forge
    @Nullable String minecraft

    DependenciesBuilder() {
        println "[Frontend] new io.github.groovymc.modsdotgroovy.frontend.DependenciesBuilder()"
        this.core = null
    }

    DependenciesBuilder(final ModsDotGroovyCore core) {
        println "[Frontend] new io.github.groovymc.modsdotgroovy.frontend.DependenciesBuilder(core: $core)"
        this.core = core
    }
}
