package org.groovymc.modsdotgroovy.plugin

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.Platform
import org.jetbrains.annotations.Nullable

@CompileStatic
@Log4j2(category = 'MDG - PluginRegistry')
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
        plugins.addAll(ServiceLoader.load(ModsDotGroovyPlugin, this.class.classLoader).asCollection())
        if (plugins.isEmpty()) throw new IllegalStateException('No plugins found!')
        log.info "Loaded plugins: ${Writer writer -> writer << plugins.collect {"[${it.name} v${it.version}]" }.join(', ')}"
    }
}
