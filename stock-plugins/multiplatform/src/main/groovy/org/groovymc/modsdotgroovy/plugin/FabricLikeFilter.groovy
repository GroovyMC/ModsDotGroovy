package org.groovymc.modsdotgroovy.plugin

import groovy.transform.CompileStatic

@CompileStatic
@SuppressWarnings('GroovyUnusedDeclaration') // All these methods are dynamically called by ModsDotGroovyCore
trait FabricLikeFilter {
    abstract boolean filterAtLevel()

    def setLogoFile(final value) {
        if (filterAtLevel())
            return PluginResult.remove()
    }

    def setDisplayTest(final value) {
        if (filterAtLevel())
            return PluginResult.remove()
    }

    def setUpdateJsonUrl(final value) {
        if (filterAtLevel())
            return PluginResult.remove()
    }
}
