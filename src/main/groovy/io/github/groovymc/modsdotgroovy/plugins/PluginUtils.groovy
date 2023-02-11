package io.github.groovymc.modsdotgroovy.plugins

import groovy.transform.CompileDynamic
import io.github.groovymc.modsdotgroovy.PluginResult

/**
 * Utility methods for ModsDotGroovy plugins.
 */
@CompileDynamic
class PluginUtils {
    /**
     * Helper function for moving a property to somewhere else on the stack.
     * @param stack Where you want to move the property to
     * @param value The value of the property, to optionally transform while moving it
     * @return
     */
    static Tuple2<PluginResult, ?> move(final Deque<String> stack, def value) {
        return new Tuple2<PluginResult, ?>(PluginResult.TRANSFORM, new Tuple2<Deque<String>, ?>(stack, value))
    }

    /**
     * Helper function for moving a property to somewhere else on the stack.
     * @param stack Where you want to move the property to
     * @param value The value of the property, to optionally transform while moving it
     * @return
     */
    static Tuple2<PluginResult, ?> move(final List<String> stack, def value) {
        return move(new ArrayDeque<String>(stack), value)
    }
}
