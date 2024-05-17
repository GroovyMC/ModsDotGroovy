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

    static Map recursivelyConvertToPrimitives(final Map map) {
        return map.inject([:]) { result, key, value ->
            result[key] = convertEntryToPrimitive(value)
            return result
        }
    }

    private static Object convertEntryToPrimitive(final value) {
        if (value instanceof Map) {
            return recursivelyConvertToPrimitives(value as Map)
        } else if (value instanceof List) {
            return recursivelyConvertToPrimitives(value as List)
        } else if (value instanceof Number || value instanceof Boolean || value instanceof Character || value instanceof String) {
            return value
        } else {
            return value.toString()
        }
    }

    static List recursivelyConvertToPrimitives(final List list) {
        return list.collect { listItem ->
            convertEntryToPrimitive(listItem)
        }
    }

    private MapUtils() {}
}
