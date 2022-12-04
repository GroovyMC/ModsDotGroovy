package ga.ozli.projects.flexiblemodsdotgroovy.plugins

import ga.ozli.projects.flexiblemodsdotgroovy.ModsDotGroovyPlugin
import ga.ozli.projects.flexiblemodsdotgroovy.PluginAwareMap
import ga.ozli.projects.flexiblemodsdotgroovy.PluginMode
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Log
import groovyjarjarantlr4.v4.runtime.misc.Nullable

@Log
@CompileStatic
class DebugPlugin implements ModsDotGroovyPlugin {
    @Override
    byte getPriority() {
        return Byte.MAX_VALUE
    }

    @Override
    boolean shouldRun(PluginAwareMap parent, PluginAwareMap self) {
        log.info("shouldRun: parent = $parent, self = $self")
        return true
    }

    @Override
    @Nullable
    Tuple2<PluginMode, Map> getDefaults(final Map data) {
        log.info("getDefaults: data = $data")
        return null
    }

    @Override
    @Nullable
    Object getFallbackFor(final Map data, final String key) throws Exception {
        log.info("getFallbackFor: data = $data, key = $key")
        return null
    }

    @Override
    @CompileDynamic
    @Nullable
    def set(final String key, def objectIn) throws Exception {
        log.info("set: key = $key, objectIn = $objectIn")
        return null
    }

    @Override
    @Nullable
    Tuple2<PluginMode, Map> build(final Map data) throws Exception {
        log.info("build: data = $data")
        return null
    }
}
