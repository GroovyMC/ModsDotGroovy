package ga.ozli.projects.flexiblemodsdotgroovy

import ga.ozli.projects.flexiblemodsdotgroovy.plugins.ForgePlugin
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovyjarjarantlr4.v4.runtime.misc.Nullable

@CompileStatic
class PluginAwareMap {
    @Delegate
    protected Map data = [:]

    Map getData() {
        return this.data
    }

    private void setData(Map newData) {
        this.data = newData
    }

    Map<String, ModsDotGroovyPlugin> pluginsMap = [:]

    Map<String, ModsDotGroovyPlugin> getPluginsMap() {
        return this.pluginsMap
    }

    protected PriorityQueue<ModsDotGroovyPlugin> plugins = new PriorityQueue<>(
            new Comparator<ModsDotGroovyPlugin>() {
                @Override
                int compare(ModsDotGroovyPlugin o1, ModsDotGroovyPlugin o2) {
                    return o1.priority <=> o2.priority
                }
            }
    )

    protected PriorityQueue<ModsDotGroovyPlugin> getPlugins() {
        return this.plugins
    }

    protected PluginAwareMap(@Nullable PluginAwareMap parent) {
        this.getPlugins().addAll(ServiceLoader.load(ModsDotGroovyPlugin).iterator()
                .findAll((ModsDotGroovyPlugin plugin) -> plugin.shouldRun(parent, this)))

        // for debugging
        this.getPlugins().addAll(List.of(new ForgePlugin()).iterator()
                .findAll((ModsDotGroovyPlugin plugin) -> plugin.shouldRun(parent, this)))

        this.getPlugins().each { getPluginsMap()[it.name] = it }

        this.setData this.getData().withDefault { requestedKey ->
            getPlugins().find {
                it.getFallbackFor(this.getData(), requestedKey as String)
            }
        }

        loadDefaults()
    }

    private void loadDefaults() {
        for (plugin in this.getPlugins()) {
            switch (plugin.getDefaults(this.getData())?.v1) {
                case PluginMode.OVERWRITE:
                    this.setData plugin.getDefaults(this.getData()).v2
                    return // The highest priority plugin that overwrites the defaults wins
                case PluginMode.MERGE:
                    this.setData merge(getData(), plugin.getDefaults(this.getData()).v2)
                    break
            }
        }
    }

    void build() {
        for (plugin in this.getPlugins()) {
            switch (plugin.build(this.getData())?.v1) {
                case PluginMode.OVERWRITE:
                    this.setData plugin.build(this.getData()).v2
                    return
                case PluginMode.MERGE:
                    this.setData merge(this.getData(), plugin.build(this.getData()).v2)
                    break
            }
        }
    }

    @CompileDynamic
    void put(final String key, final def value) {
        def newValue = null
        for (plugin in plugins) {
            final result
            if (plugin.respondsTo("set${key.capitalize()}"))
                result = plugin."set${key.capitalize()}"(value)
            else
                result = plugin.set(key, value)

            if (result) {
                newValue = result
                break
            }
        }
        getData()[key] = newValue ?= value
    }

    @CompileDynamic
    static Map merge(Map left, Map right) {
        return right.inject(new LinkedHashMap<>(left)) { map, entry ->
            if (map[entry.key] instanceof Map && entry.value instanceof Map)
                map[entry.key] = merge(map[entry.key] as Map, entry.value as Map)
            else if (map[entry.key] instanceof Collection && entry.value instanceof Collection)
                map[entry.key] += entry.value as Collection
            else
                map[entry.key] = entry.value

            return map
        }
    }
}
