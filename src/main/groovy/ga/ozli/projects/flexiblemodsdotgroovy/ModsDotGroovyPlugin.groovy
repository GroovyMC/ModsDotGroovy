package ga.ozli.projects.flexiblemodsdotgroovy

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
        return getClass().simpleName
    }

    /**
     * The version of the plugin, used for logging and debugging purposes.
     */
    default float getVersion() {
        return 1.00f
    }

    /**
     * Called when the plugin is loaded.
     * Use this to determine if the plugin will work in the current environment and return false if not.
     * @param core the ModsDotGroovyCore instance
     */
    default boolean shouldRun(@Nullable PluginAwareMap parent, PluginAwareMap self) {
        return ModsDotGroovyCore.INSTANCE.getVersion().trunc() === 2
    }

    /**
     * @return The initial default values for the data map.
     */
    @Nullable
    default Tuple2<PluginMode, Map> getDefaults(final Map data) {
        // e.g.: return new Tuple2<>(PluginMode.OVERWRITE, ["schema_version": 1, "quilt_loader": [:]])
        return null
    }

    /**
     * Called when a property is marked as required but is null or missing from the data map.
     * @param keyPath The path to where the object will be in the Map
     * @return null for no fallback, otherwise the object to use as a fallback
     */
    @CompileDynamic
    @Nullable
    default def getFallbackFor(final Map data, final String key) throws Exception {
        // e.g.: if (keyPath == 'modLoader') return 'javafml'
        return null
    }

    /**
     * Called when a user sets a property in the MDG script.
     * Use this for isolated on-the-fly validation and/or to modify the value being stored in the data map.
     * @param keyPath
     * @param objectIn
     * @return
     * @throws Exception
     */
    @CompileDynamic
    @Nullable
    default def set(final String key, def objectIn) throws Exception {
        // e.g.: if (keyPath == 'modLoader' && objectIn !instanceof String) throw new Exception('modLoader must be a String')
        return null
    }

    /**
     * Called at the end of the MDG script, after all properties have been set.
     * Use this for validation that depends on multiple properties, i.e. if you want to make sure that three
     * different properties are all set to the same value.
     * Also use this to modify the data map before it is returned to consumers.
     * @param data
     * @return
     * @throws Exception
     */
    @Nullable
    default Tuple2<PluginMode, Map> build(final Map data) throws Exception {
        return null
    }
}
