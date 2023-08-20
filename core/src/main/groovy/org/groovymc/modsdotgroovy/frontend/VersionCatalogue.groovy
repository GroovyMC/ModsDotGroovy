package org.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileStatic

@CompileStatic
class VersionCatalogue {
    public final Map<String, String> versions
    public final Map<String, List<String>> libraries = null
    public final Map<String, ?> bundles = null
    public final Map<String, ?> plugins = null

    Map<String, List<String>> getLibraries() {
        throw new UnsupportedOperationException(
                'This version of mod.groovy only supports the "versions" part of Gradle version catalogues')
    }

    Map<String, ?> getBundles() {
        throw new UnsupportedOperationException(
                'This version of mod.groovy only supports the "versions" part of Gradle version catalogues')
    }

    Map<String, ?> getPlugins() {
        throw new UnsupportedOperationException(
                'This version of mod.groovy only supports the "versions" part of Gradle version catalogues')
    }

    VersionCatalogue(final Map<String, Map<String, ?>> map) {
        versions = map.versions as Map<String, String>
    }
}
