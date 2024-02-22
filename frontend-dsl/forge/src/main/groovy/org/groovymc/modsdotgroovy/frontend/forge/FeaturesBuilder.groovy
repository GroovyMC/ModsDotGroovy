package org.groovymc.modsdotgroovy.frontend.forge

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor
import org.jetbrains.annotations.Nullable

@CompileStatic
@Log4j2(category = 'MDG - Forge Frontend')
class FeaturesBuilder extends DslBuilder implements PropertyInterceptor {
    @Nullable String openGLVersion
    @Nullable String javaVersion

    FeaturesBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
