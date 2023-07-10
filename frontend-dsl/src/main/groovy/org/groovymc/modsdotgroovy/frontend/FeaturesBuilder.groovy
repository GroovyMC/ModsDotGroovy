package org.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.jetbrains.annotations.Nullable

@CompileStatic
@Log4j2(category = 'MDG - Frontend')
class FeaturesBuilder implements PropertyInterceptor {
    private final ModsDotGroovyCore core

    @Nullable String openGLVersion

    @SuppressWarnings('GroovyUnusedDeclaration') // Used by the Groovy compiler for coercing an implicit `it` closure
    FeaturesBuilder() {
        log.debug "new org.groovymc.modsdotgroovy.frontend.FeaturesBuilder()"
        this.core = null
    }

    FeaturesBuilder(final ModsDotGroovyCore core) {
        log.debug "new org.groovymc.modsdotgroovy.frontend.FeaturesBuilder(core: $core)"
        this.core = core
    }
}
