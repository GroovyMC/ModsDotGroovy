package org.groovymc.modsdotgroovy.frontend.neoforge

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor
import org.jetbrains.annotations.Nullable

@CompileStatic
@Log4j2(category = 'MDG - NeoForge Frontend')
class FeaturesBuilder extends DslBuilder implements PropertyInterceptor {
    @Nullable String openGLVersion

    FeaturesBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
