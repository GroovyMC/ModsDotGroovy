package io.github.groovymc.modsdotgroovy.plugin

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovyjarjarantlr4.v4.runtime.misc.Nullable

@CompileStatic
interface ModsDotGroovyPlugin {
    /**
     * Byte.MAX_VALUE = highest priority, Byte.MIN_VALUE = lowest priority.
     * Higher priority plugins will have their actions executed first.
     * @return the priority of this plugin
     */
    default byte getPriority() {
        return 0
    }

    /**
     * The name of the plugin, used for logging and debugging purposes.
     */
    default String getName() {
        final String simpleName = getClass().simpleName
        return simpleName.endsWith('Plugin') ? simpleName[0..-7] : simpleName
    }

    /**
     * The version of the plugin, used for logging and debugging purposes.
     */
    default float getVersion() {
        return 1.00f
    }

    default void init() {
        println "[$name] Plugin $name v$version initialized"
    }

    /**
     * A generic method that's called when a property is set.
     * Used as a fallback for when a plugin doesn't implement an explicit setter.
     * @param stack
     * @param name
     * @param value
     * @return <PluginResult | Tuple2<PluginResult, ?> | Object>
     *     If you return a Tuple2, the first value must be a PluginResult and the second value is either:
     *         - The type of the changed value.
     *         - Another Tuple2 of the stack and the changed value, used for moving the changed value to a different location.
     *     If you return a PluginResult, it'll be treated as Tuple2<(yourPluginResult), null>.
     *     If you return an Object, it'll be treated as Tuple2<PluginResult.TRANSFORM, (yourObject)>.
     *     If you return null or don't return anything (void), it'll be treated as Tuple2<PluginResult.VALIDATE, null>.
     */
    @CompileDynamic
    default def set(final Deque<String> stack, final String name, def value) {
        return PluginResult.UNHANDLED
    }

    /**
     * @return A map of default values to use as a fallback when a property is not set and no other plugin has set it.
     */
    @Nullable
    default Map getDefaults() {
        return null
    }
}
