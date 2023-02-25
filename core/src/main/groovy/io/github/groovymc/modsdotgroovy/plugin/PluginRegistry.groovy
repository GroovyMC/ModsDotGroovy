package io.github.groovymc.modsdotgroovy.plugin

import groovy.transform.CompileStatic
import io.github.groovymc.modsdotgroovy.ForgePlugin

@CompileStatic
final class PluginRegistry {
    @Delegate
    final PriorityQueue<ModsDotGroovyPlugin> plugins = new PriorityQueue<>(new Comparator<ModsDotGroovyPlugin>() {
        @Override
        int compare(ModsDotGroovyPlugin o1, ModsDotGroovyPlugin o2) {
            return -(o1.priority <=> o2.priority)
        }
    })

    PluginRegistry() {
        // Load plugins
        plugins.addAll(ServiceLoader.load(ModsDotGroovyPlugin).toList())
        plugins << new ForgePlugin()
        println "[PluginRegistry] Loaded plugins: ${plugins*.name}"
    }
}
