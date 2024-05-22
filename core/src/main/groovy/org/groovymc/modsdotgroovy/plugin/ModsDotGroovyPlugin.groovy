package org.groovymc.modsdotgroovy.plugin

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.logging.log4j.core.Logger
import org.groovymc.modsdotgroovy.core.ConversionSettings
import org.groovymc.modsdotgroovy.core.MapTransform
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Nullable

import java.util.function.BiConsumer

@CompileStatic
abstract class ModsDotGroovyPlugin {
    /**
     * Byte.MAX_VALUE = highest priority, Byte.MIN_VALUE = lowest priority.
     * Higher priority plugins will have their actions executed first.
     * @return the priority of this plugin
     */
    byte getPriority() {
        return 0
    }

    /**
     * The name of the plugin, used for logging and debugging purposes.
     */
    String getName() {
        final String simpleName = getClass().simpleName
        return simpleName.endsWith('Plugin') ? simpleName[0..-7] : simpleName
    }

    abstract Logger getLog()

    /**
     * The version of the plugin, used for logging and debugging purposes.
     */
    String getVersion() {
        return this.class.package.implementationVersion ?: ''
    }

    /**
     * Called when the plugin is initialized. Use this to setup any resources you need and get information about the
     * environment you're running in.
     */
    void init(final Map<String, ?> environment, ConversionSettings conversionSettings) {}

    List<MapTransform> mapTransforms() {
        return []
    }

    /**
     * A generic method that's called when a property is set.
     * Used as a fallback for when a plugin doesn't implement an explicit setter.
     * @param stack
     * @param name
     * @param value
     * @return <<T extends PluginResult> | Object | null | void>
     *     If you return a PluginResult, it'll be treated as-is.
     *     If you return an Object, it'll be treated as {@code new PluginResult.Change(newValue: (yourObject))}.
     *     If you return null or don't return anything (void), it'll be treated as {@code new PluginResult.Validate()}.
     */
    @CompileDynamic
    def set(final List<String> stack, final String name, def value) {
        return new PluginResult.Unhandled()
    }

    /**
     * A generic method that's called when leaving a nest.
     * Used as a fallback for when a plugin doesn't implement an explicit onNestLeave.
     * @param stack
     * @param value
     * @return <<T extends PluginResult> | Object | null | void>
     *     If you return a PluginResult, it'll be treated as-is.
     *     If you return an Object, it'll be treated as {@code new PluginResult.Change(newValue: (yourObject))}.
     *     If you return null or don't return anything (void), it'll be treated as {@code new PluginResult.Validate()}.
     */
    @CompileDynamic
    def onNestLeave(final List<String> stack, Map value) {
        return new PluginResult.Unhandled()
    }

    /**
     * Called when ModsDotGroovy is building the final map.
     * @param buildingMap The map that's being built so far.
     * @return A map of values you want to add or override in the final map.
     */
    @Nullable
    Map build(Map buildingMap) {
        return null
    }

    private final Map<NestKey, Object> nests = [:]

    @ApiStatus.Internal
    final void initializeNest(final NestKey key, final Object nest) {
        nests[key] = nest
    }

    private BiConsumer<List<String>, Object> putter
    private ModsDotGroovyCore core

    @ApiStatus.Internal
    final void initializeStackPut(BiConsumer<List<String>, Object> putter, ModsDotGroovyCore core) {
        this.putter = putter
        this.core = core
    }

    final void put(final List<String> stack, final Object value, final boolean reentrant) {
        if (reentrant) {
            core.putStacked(stack, value)
        } else {
            putter.accept(stack, value)
        }
    }

    @Nullable
    final getNest(final NestKey key) {
        return nests[key]
    }
}
