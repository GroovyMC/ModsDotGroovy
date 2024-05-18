package org.groovymc.modsdotgroovy.gradle.internal

import groovy.transform.CompileStatic

@CompileStatic
class MapUtils {
    static Map recursivelyMergeOnlyMaps(final Map left, final Map right) {
        if (left === null && right === null) return [:]
        if (left === null) return right
        if (right === null) return left
        Map out = new LinkedHashMap(left)
        right.each { key, value ->
            var existing = out[key]
            if (existing instanceof Map && value instanceof Map) {
                out[key] = recursivelyMergeOnlyMaps(existing, value)
            } else {
                out[key] = value
            }
        }
        return out
    }

    private MapUtils() {}
}
