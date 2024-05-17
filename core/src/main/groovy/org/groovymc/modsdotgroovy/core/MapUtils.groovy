package org.groovymc.modsdotgroovy.core

import groovy.transform.CompileStatic

@CompileStatic
class MapUtils {
    /**
     * Recursively removes null values and evaluates GStrings in-place, returning a serializable type.
     * @param data
     */
    static void sanitizeMap(final Map data) {
        final copy = new LinkedHashMap(data) // cannot use Map.copyOf as we wish to remove null values
        copy.forEach((key, value) -> {
            switch (value) {
                case null -> data.remove(key)
                case List -> sanitizeList(value as List)
                case Map -> sanitizeMap(value as Map)
                case GString -> data[key] = value.toString()
            }
        })
    }

    static void sanitizeList(final List list) {
        final copy = new ArrayList(list)
        copy.removeIf(Objects::isNull)
        for (def i = 0; i < copy.size(); i++) {
            final value = copy[i]
            switch (value) {
                case List -> sanitizeList(value as List)
                case Map -> sanitizeMap(value as Map)
                case GString -> copy[i] = value.toString()
            }
        }
    }

    /**
     * Recursively merges the contents of two Maps, including nested Maps and Lists.<br>
     * The left map has any null or missing values filled in by the right map if available.<br>
     * Example:
     * <code>
     *     final Map a = [a: 1, b: null, c: [1, 2]]
     *     final Map b = [a: 2, b: 2, c: [3, null]]
     *     assert recursivelyMerge(a, b) == [a: 1, b: 2, c: [1, 2, 3]]
     * </code>
     * @param left
     * @param right
     * @return
     */
    static Map recursivelyMerge(final Map left, final Map right) {
        if (left === null && right === null) return [:]
        if (left === null) return right
        if (right === null) return left
        return right.inject(new LinkedHashMap(left)) { map, entry ->
            if (map[entry.key] instanceof Map && entry.value instanceof Map) {
                // if both values are maps, recursively merge them
                map[entry.key] = recursivelyMerge(map[entry.key] as Map, entry.value as Map)
            } else if (map[entry.key] instanceof List && entry.value instanceof List) {
                // if both values are lists, add them together
                map[entry.key] = map[entry.key] as List + entry.value as List
            } else if (map[entry.key] instanceof List && entry.value !== null) {
                // if the left value is a list, add the right value to it
                map[entry.key] = (map[entry.key] as List) + entry.value
            } else if (entry.value instanceof List) {
                List values = [map[entry.key]]
                values.addAll(entry.value as List)
                map[entry.key] = values
            } else if (!map.containsKey(entry.key) || map[entry.key] === null) {
                map[entry.key] = entry.value
            }
            return map
        } as Map
    }

    static Map recursivelyMerge(final List<Map> maps) {
        Map result = null
        for (final Map map in maps) {
            result = recursivelyMerge(result, map)
        }
        return result
    }

    private MapUtils() {}
}
