package io.github.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileStatic
import org.jetbrains.annotations.Nullable
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
