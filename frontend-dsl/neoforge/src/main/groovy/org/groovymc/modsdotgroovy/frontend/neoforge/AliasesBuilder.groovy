package org.groovymc.modsdotgroovy.frontend.neoforge

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.core.versioning.VersionRangeAware
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.MapClosureInterceptor
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Nullable

@CompileStatic
@ApiStatus.Experimental
@Log4j2(category = 'MDG - NeoForge Frontend')
class AliasesBuilder extends DslBuilder implements MapClosureInterceptor {
    @VersionRangeAware
    @Nullable String forge = null

    void alias(@DelegatesTo(value = AliasBuilder, strategy = Closure.DELEGATE_FIRST)
               @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.AliasBuilder')
               final Closure closure) {
        log.debug 'alias(closure)'
        core.push('alias')
        final aliasBuilder = new AliasBuilder(core)
        closure.delegate = aliasBuilder
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call(aliasBuilder)
        core.pop()
    }

    @CompileDynamic
    void setProperty(final String name, final def value) {
        alias {
            modId = name
            versionRange = value
        }
    }

    AliasesBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
