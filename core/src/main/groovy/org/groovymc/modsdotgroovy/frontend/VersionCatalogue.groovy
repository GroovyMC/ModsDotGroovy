package org.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileStatic

@CompileStatic
class VersionCatalogue {
    final Map<String, String> versions
    final Map<String, Library> libraries
    final Map<String, List<Library>> bundles
    final Map<String, Plugin> plugins

    static class Library {
        final String group
        final String name
        final String version

        Library(final Map map) {
            group = map.group as String
            name = map.name as String
            version = map.version as String
        }

        String getModule() {
            return "${group}:${name}"
        }
    }

    static class Plugin {
        final String id
        final String version

        Plugin(final Map map) {
            id = map.id as String
            version = map.version as String
        }
    }

    VersionCatalogue(final Map map) {
        versions = map.versions as Map<String, String> ?: [:]
        libraries = (map.libraries as Map ?: [:]).collectEntries {k, v ->
            [(k): new Library(v as Map)]
        } as Map<String, Library>
        bundles = (map.bundles as Map ?: [:]).collectEntries {k, v ->
            [(k): (v as List).collect {new Library(it as Map)}]
        } as Map<String, List<Library>>
        plugins = (map.plugins as Map ?: [:]).collectEntries {k, v ->
            [(k): new Plugin(v as Map)]
        } as Map<String, Plugin>
    }
}
