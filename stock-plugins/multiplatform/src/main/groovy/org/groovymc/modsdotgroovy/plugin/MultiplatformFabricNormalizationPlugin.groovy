package org.groovymc.modsdotgroovy.plugin

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.core.Logger
import org.groovymc.modsdotgroovy.core.Platform

@CompileStatic
@SuppressWarnings('GroovyUnusedDeclaration') // All these methods are dynamically called by ModsDotGroovyCore
@Log4j2(category = 'MDG - MultiplatformFabricNormalizationPlugin')
class MultiplatformFabricNormalizationPlugin extends ModsDotGroovyPlugin implements FabricLikeFilter {
    private boolean isFabric

    @Override
    Logger getLog() {
        return log
    }

    @Override
    byte getPriority() {
        return 5
    }

    @Override
    void init(final Map<String, ?> environment) {
        isFabric = (environment['platform'].invokeMethod('name', null) as String) == Platform.FABRIC.name()
    }

    def setModId(final String modId) {
        if (isFabric)
            return PluginResult.rename('id', modId)
    }

    def setDisplayName(final String value) {
        if (isFabric)
            return PluginResult.rename('name', value)
    }

    def setDisplayUrl(final String value) {
        if (isFabric)
            return PluginResult.move(['contact'], 'homepage', value)
    }

    @Override
    boolean filterAtLevel() {
        return isFabric
    }

    class Features {
        def onNestLeave(final Deque<String> stack, final Map value) {
            if (isFabric)
                return PluginResult.remove()
        }
    }
}
