package org.groovymc.modsdotgroovy.frontend.multiplatform

import groovy.transform.CompileStatic
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.types.core.Platform

@CompileStatic
trait OnPlatform {
    abstract ModsDotGroovyCore getCore()

    @Lazy
    private final Logger log = LogManager.getLogger('MDG - Multiplatform Frontend')

    void onForge(final Runnable runnable) {
        log.debug "onForge(closure)"
        if (core.platform === Platform.FORGE)
            runnable.run()
    }

    void onFabric(final Runnable runnable) {
        log.debug "onFabric(closure)"
        if (core.platform === Platform.FABRIC)
            runnable.run()
    }

    void onQuilt(final Runnable runnable) {
        log.debug "onQuilt(closure)"
        if (core.platform === Platform.QUILT)
            runnable.run()
    }

    void onNeoForge(final Runnable runnable) {
        log.debug "onNeoForge(closure)"
        if (core.platform === Platform.NEOFORGE)
            runnable.run()
    }

    void onSpigot(final Runnable runnable) {
        log.debug "onSpigot(closure)"
        if (core.platform === Platform.SPIGOT)
            runnable.run()
    }
}
