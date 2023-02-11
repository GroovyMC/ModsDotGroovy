package io.github.groovymc.modsdotgroovy.core

import groovy.transform.CompileDynamic
import groovy.transform.PackageScope

/**
 * For ModsDotGroovyCore properties that need CompileDynamic.
 * Kept in a separate class to avoid the performance penalty of dynamic compilation for the rest of the properties
 * that don't need it.
 */
@PackageScope
@CompileDynamic
class ModsDotGroovyCoreDynamic {
    final Map<String, Map> pluginInstances = [:]
}
