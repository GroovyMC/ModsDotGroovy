package io.github.groovymc.modsdotgroovy.plugin

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2

import static java.util.ServiceLoader.Provider

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
        final ServiceLoader<ModsDotGroovyPlugin> serviceLoader = ServiceLoader.load(ModsDotGroovyPlugin)
        plugins.addAll(serviceLoader.stream().map(Provider::get).toList() as List<ModsDotGroovyPlugin>)
        log.info "Loaded plugins: ${Writer writer -> writer << plugins.collect {"[${it.name} v${it.version.toString()}]" }.join(', ')}"
    }
}