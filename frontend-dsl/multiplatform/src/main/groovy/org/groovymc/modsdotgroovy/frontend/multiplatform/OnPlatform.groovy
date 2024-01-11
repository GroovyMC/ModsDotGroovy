package org.groovymc.modsdotgroovy.frontend.multiplatform

import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.core.Platform

@Log4j2(category = 'MDG - Multiplatform Frontend')
trait OnPlatform {
    abstract ModsDotGroovyCore getCore()

    void onForge(final Closure closure) {
        log.debug "onForge(closure)"
        if (core.platform() === Platform.FORGE)
            closure.call()
    }

    void onFabric(final Closure closure) {
        log.debug "onFabric(closure)"
        if (core.platform() === Platform.FABRIC)
            closure.call()
    }

    void onQuilt(final Closure closure) {
        log.debug "onQuilt(closure)"
        if (core.platform() === Platform.QUILT)
            closure.call()
    }

    void onNeoForge(final Closure closure) {
        log.debug "onNeoForge(closure)"
        if (getPlatform() === Platform.NEOFORGE)
            closure.call()
    }

    void onSpigot(final Closure closure) {
        log.debug "onSpigot(closure)"
        if (getPlatform() === Platform.SPIGOT)
            closure.call()
    }
}