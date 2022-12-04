package ga.ozli.projects.flexiblemodsdotgroovy

import ga.ozli.projects.flexiblemodsdotgroovy.plugins.DebugPlugin
import ga.ozli.projects.flexiblemodsdotgroovy.plugins.ForgePlugin
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovyjarjarantlr4.v4.runtime.misc.Nullable

@CompileStatic
class PluginAwareMap {
    final List<String> BLACKLIST = ['data', 'pluginsMap', 'plugins', 'data']

    //@Delegate - commented out for now due to IDE syntax highlighting bug
    Map data = [:]

    void propertyMissing(String name, def value) {
        data.put(name, value)
    }

    def propertyMissing(String name) {
        data.get(name)
    }

    @CompileDynamic
    void setProperty(String name, def value) {
        println "name: $name"
        println "value: $value"
        this.@"$name" = value
        if (name !in BLACKLIST)
            data.put(name, value)
    }

    @CompileDynamic
    def getProperty(String name) {
        if (name in BLACKLIST) this.@"$name"
        else return data.get(name)
    }

    public Map<String, ModsDotGroovyPlugin> pluginsMap = [:]

    protected final PriorityQueue<ModsDotGroovyPlugin> plugins = new PriorityQueue<>(
            new Comparator<ModsDotGroovyPlugin>() {
                @Override
                int compare(ModsDotGroovyPlugin o1, ModsDotGroovyPlugin o2) {
                    return -(o1.priority <=> o2.priority)
                }
            }
    )

    PluginAwareMap(@Nullable PluginAwareMap parent) {
        plugins.addAll(ModsDotGroovyCore.PLUGINS
                .findAll((ModsDotGroovyPlugin plugin) -> plugin.shouldRun(parent, this)))

        plugins.each { pluginsMap[it.name] = it }

        data = data.withDefault { requestedKey ->
            plugins.find {
                it.getFallbackFor(data, requestedKey as String)
            }
        }

        loadDefaults()
    }

    private void loadDefaults() {
        for (plugin in plugins) {
            final @Nullable defaults = plugin.getDefaults(data)
            if (defaults === null) continue
            switch (defaults.v1) {
                case PluginMode.OVERWRITE:
                    data = defaults.v2
                    return // The highest priority plugin that overwrites the defaults wins
                case PluginMode.MERGE:
                    data = merge(data, defaults.v2)
            }
        }
    }

    void build() {
        for (plugin in plugins) {
            final @Nullable buildResult = plugin.build(data)
            if (buildResult === null) continue
            switch (buildResult.v1) {
                case PluginMode.OVERWRITE:
                    data = buildResult.v2
                    return
                case PluginMode.MERGE:
                    data = merge(data, buildResult.v2)
                    break
            }
        }
    }

    Map toMap() {
        return data
    }

    @CompileDynamic
    def put(final String key, final def value) {
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
        data[key] = newValue ?: value
        return data[key]
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
