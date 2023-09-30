package org.groovymc.modsdotgroovy.frontend.neoforge

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.core.versioning.VersionRangeAware
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.MapClosureInterceptor
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor
import org.jetbrains.annotations.ApiStatus

@CompileStatic
@ApiStatus.Experimental
@Log4j2(category = 'MDG - NeoForge Frontend')
class AliasBuilder extends DslBuilder implements PropertyInterceptor, MapClosureInterceptor {
    String modId

    @VersionRangeAware
    String versionRange

    AliasBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
