package io.github.groovymc.modsdotgroovy.core

import groovy.transform.AutoImplement
import groovy.transform.CompileStatic
import groovy.transform.Internal

@CompileStatic
@AutoImplement(message = 'Unimplemented')
final class StackAwareObservableMap implements Map {
    private final ObservableMap rootMap = makeObservableMap()
    private final Deque<String> stack = new ArrayDeque<>()

    private int lastStackSize = stack.size()
    boolean ignoreNextEvent = false // avoid infinite loop when someone changes a property in the map

    StackAwareObservableMap() {}

    //region Map
    @Override
    int size() {
        return traverse().size()
    }

    @Override
    boolean isEmpty() {
        return traverse().isEmpty()
    }

    @Override
    boolean containsKey(Object key) {
        return traverse().containsKey(key)
    }

    @Override
    boolean containsValue(Object value) {
        return traverse().containsValue(value)
    }

    @Override
    Object get(Object key) {
        return traverse().get(key)
    }

    @Override
    Object put(Object key, Object value) {
        return traverse().put(key, value)
    }

    @Override
    Object remove(Object key) {
        return traverse().remove(key)
    }

    @Override
    void clear() {
        traverse().clear()
    }

    @Override
    Set keySet() {
        return traverse().keySet()
    }

    @Override
    Collection values() {
        return traverse().values()
    }

    @Override
    Set<Entry> entrySet() {
        return traverse().entrySet()
    }

    ObservableMap getRootMap() {
        return rootMap
    }

    Object putAt(Deque<String> stack, Object key, Object value) {
        return traverse(stack).put(key, value)
    }
    //endregion Map

    //region Stack
    void push(final String key) {
        // make the nested map and attach its parent map's PropertyChangeListeners to it
        final ObservableMap nestedData = makeObservableMap()
        traverse().getPropertyChangeListeners().each(nestedData.&addPropertyChangeListener)

        // update the stack
        lastStackSize = stack.size()
        final Deque<String> oldStack = new ArrayDeque<>(stack)
        stack.addLast(key)

        // add the nestedMap
        putAt(oldStack, key, nestedData)
    }

    void pop() {
        // update the stack
        lastStackSize = stack.size()
        stack.pollLast()
    }

    Deque<String> getStack() {
        return stack
    }

    int getLastStackSize() {
        return lastStackSize
    }
    //endregion Stack

    @Internal
    private ObservableMap makeObservableMap() {
        return new ObservableMap({ propertyName, newValue ->
            if (ignoreNextEvent) {
                ignoreNextEvent = false
                return false // deny the change when asked to ignoreNextEvent
            }
            return true
        })
    }

    @Internal
    private ObservableMap traverse(final Deque<String> stack = stack) {
        ObservableMap traversedMap = rootMap
        for (final String stackKey in stack) {
            traversedMap = traversedMap[stackKey] as ObservableMap
        }
        return traversedMap
    }
}
