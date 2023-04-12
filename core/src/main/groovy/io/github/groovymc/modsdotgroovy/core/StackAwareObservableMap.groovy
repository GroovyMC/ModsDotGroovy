package io.github.groovymc.modsdotgroovy.core

import groovy.transform.CompileStatic
import groovy.transform.Internal
import groovy.transform.PackageScope

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeSupport
import java.lang.reflect.Field

@CompileStatic
final class StackAwareObservableMap extends ObservableMap {
    private static final Field pcsField = ObservableMap.getDeclaredField('pcs')

    private final ObservableMap rootMap = makeObservableMap()
    private final Deque<String> stack = new ArrayDeque<>()

    boolean ignoreNextEvent = false // avoid infinite loop when someone changes a property in the map

    StackAwareObservableMap() {
        super()
        pcsField.accessible = true
    }

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
        return traverse(stack)[key] = value
    }
    //endregion Map

    //region Stack
    void push(final String key) {
        // update the stack
        final Deque<String> oldStack = new ArrayDeque<>(stack)
        stack.addLast(key)

        // make the nested map and attach its parent map's PropertyChangeListeners to it
        this.@rootMap.@ignoreNextEvent = true
        println 'before nestedData = makeObservableMap()'
        final ObservableMap nestedData = makeObservableMap()
        println 'after nestedData = makeObservableMap()'
        this.@rootMap.@ignoreNextEvent = true
        println 'before traverse(oldStack).getPropertyChangeListeners().each(nestedData.&addPropertyChangeListener)'
        traverse(oldStack).getPropertyChangeListeners().each(nestedData.&addPropertyChangeListener)
        println 'after traverse(oldStack).getPropertyChangeListeners().each(nestedData.&addPropertyChangeListener)'

        this.@rootMap.@ignoreNextEvent = false
        println 'before fireStackChangedEvent(oldStack, null, nestedData)'
        fireStackChangedEvent(oldStack, null, nestedData)
        println 'after fireStackChangedEvent(oldStack, null, nestedData)'

        // add the nestedMap
        this.@rootMap.@ignoreNextEvent = true // we already fired a StackChangedEvent earlier
        putAt(oldStack, key, nestedData)
    }

    void pop() {
        // update the stack
        final Deque<String> oldStack = new ArrayDeque<>(stack)
        fireStackChangedEvent(oldStack, get(stack.pollLast()), null)
    }

    Deque<String> getStack() {
        return stack
    }

    @PackageScope static final class StackChangedEvent extends PropertyChangeEvent {
        final Deque<String> oldStack
        final Deque<String> newStack

        StackChangedEvent(final Object source, final Deque<String> oldStack, final Deque<String> newStack,
                          final Object oldValue, final Object newValue) {
            super(source, 'stack', oldValue, newValue)
            this.oldStack = oldStack
            this.newStack = newStack
        }
    }

    protected void fireStackChangedEvent(final Deque<String> oldStack, final Object oldValue, final Object newValue) {
        println "fireStackChangedEvent: $oldStack -> ${this.@stack}"
        ((PropertyChangeSupport) this.@pcsField.get(this.@rootMap)).firePropertyChange(new StackChangedEvent(this, oldStack, this.@stack, oldValue, newValue))
    }
    //endregion Stack

    @Internal
    private ObservableMap makeObservableMap() {
        return new ObservableMap({ propertyName, newValue ->
            println "event closure. ignoreNextEvent: ${getRootMap().@ignoreNextEvent}"
            if (getRootMap().@ignoreNextEvent) {
                getRootMap().@ignoreNextEvent = false
                return false // deny the change when asked to ignoreNextEvent
            }
            return true
        })
    }

    @Internal
    private ObservableMap traverse(final Deque<String> stack = stack) {
        ObservableMap traversedMap = this.@rootMap
        for (final String stackKey in stack) {
            traversedMap = traversedMap[stackKey] as ObservableMap
        }
        return traversedMap
    }
}
