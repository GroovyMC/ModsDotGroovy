package org.groovymc.modsdotgroovy.frontend.fabric

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor
import org.jetbrains.annotations.Nullable

@CompileStatic
@Log4j2(category = 'MDG - Fabric Frontend')
class IconBuilder extends DslBuilder implements PropertyInterceptor {
    @Nullable String x16 = null
    @Nullable String x32 = null
    @Nullable String x64 = null
    @Nullable String x128 = null

    @CompileDynamic
    void setProperty(final String name, def value) {
        log.debug "setProperty(name: $name, value: $value) stack: ${core.layeredMap.stack}"
        if (name.startsWith('x')) core.put(name[1..-1], value)
        else core.put(name, value)
    }

    IconBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
