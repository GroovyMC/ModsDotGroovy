package org.groovymc.modsdotgroovy.core

import groovy.transform.CompileStatic

@CompileStatic
class LayeredMap {
    final List<MapTransform> transforms = []
    final Map main = [:]
    Deque<String> stack = new ArrayDeque<>()
    Map current = main

    void push(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Null key provided at stack position ${stack}")
        }
        stack.addLast(key)
        def maybeMap = current.computeIfAbsent(key, { [:] })
        if (maybeMap instanceof Map) {
            current = maybeMap
        } else {
            def map = [:]
            current[key] = map
            current = map
        }
    }

    private String pop() {
        def str = stack.removeLast()
        current = main
        stack.each { current = current[it] }
        return str
    }

    void putWatched(String key, Object value, Listener listener) {
        for (def transform : transforms) {
            value = transform.transform(value)
        }
        if (value instanceof Map) {
            push(key)
            value.each { k, v ->
                putWatched(k as String, v, listener)
            }
            popWatched(listener)
        } else {
            current[key] = value
        }
        MapEvent event = new MapEvent()
        event.key = key
        event.stack = stack
        event.value = value
        listener.call(event)
    }

    void popWatched(Listener listener) {
        MapEvent event = new MapEvent()
        event.key = pop()
        event.stack = stack
        event.value = current[event.key]
        event.onPop = true
        listener.call(event)
    }

    void moveWatched(final String key, final List<String> newLocation, final Object newValue, Listener listener) {
        var value = newValue ?: current.get(key)
        current.remove(key)

        var oldStack = new ArrayList<>(stack)
        int shared = 0
        final List<String> unique = new ArrayList(newLocation.size())

        for (int i = 0; i < newLocation.size(); i++) {
            if (i < oldStack.size() && oldStack.get(i) == newLocation.get(i)) {
                shared++
            } else {
                unique.add(newLocation.get(i))
            }
        }
        while (stack.size() > shared) {
            pop()
        }
        if (value instanceof Map) {
            for (String s : newLocation) {
                push(s)
            }
            value.each { k, v ->
                putWatched(k as String, v, listener)
            }
            for (String s : unique) {
                popWatched(listener)
            }
        } else {
            if (unique.empty) {
                throw new IllegalArgumentException("Cannot move a non-map value to a location already occupied by a map.")
            }
            for (int i = 0; i < unique.size() - 1; i++) {
                push((String) unique.get(i))
            }
            putWatched((String) unique[unique.size() - 1], value, listener)
            for (int i = 0; i < unique.size() - 1; i++) {
                popWatched(listener)
            }
        }
        for (int i = shared; i < oldStack.size(); i++) {
            push(oldStack.get(i))
        }
    }

    void remove(final String key) {
        current.remove(key)
    }

    void putStackedWatched(List<String> location, Object value, Listener listener) {
        var oldStack = new ArrayList<>(stack)
        int shared = 0
        final List<String> unique = new ArrayList()

        for (int i = 0; i < location.size(); i++) {
            if (i < oldStack.size() && oldStack.get(i) == location.get(i)) {
                shared++
            } else {
                unique.add(location.get(i))
            }
        }
        while (stack.size() > shared) {
            pop()
        }
        if (value instanceof Map) {
            for (String s : unique) {
                push(s)
            }
            value.each { k, v ->
                putWatched(k as String, v, listener)
            }
            for (String s : unique) {
                popWatched(listener)
            }
        } else {
            if (unique.empty) {
                throw new IllegalArgumentException("Cannot put a non-map value at a location used by an active map.")
            }
            for (int i = 0; i < unique.size() - 1; i++) {
                push((String) unique.get(i))
            }
            putWatched((String) unique[unique.size() - 1], value, listener)
            for (int i = 0; i < unique.size() - 1; i++) {
                popWatched(listener)
            }
        }
        for (int i = shared; i < oldStack.size(); i++) {
            push(oldStack.get(i))
        }
    }

    static interface Listener {
        void call(MapEvent event)
    }

    final static class MapEvent {
        String key
        Deque<String> stack
        Object value
        boolean onPop = false
    }
}
