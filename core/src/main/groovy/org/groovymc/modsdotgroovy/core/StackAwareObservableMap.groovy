package org.groovymc.modsdotgroovy.core

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.plugin.NestKey
import org.jetbrains.annotations.ApiStatus

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeSupport
import java.lang.reflect.Field

@CompileStatic
@Log4j2(category = 'MDG - StackAwareObservableMap')
final class StackAwareObservableMap extends ObservableMap {
    private static final Field pcsField = ObservableMap.getDeclaredField('pcs')

    private final ObservableMap rootMap = makeObservableMap()
    private final Deque<String> stack = new ArrayDeque<>()

    private final Map<NestKey, Deque<NestKey>> exitCues = [:]

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
    void relocate(final Deque<String> newStack) {
        final NestKey oldKey = new NestKey(stack.toList())
        final NestKey newKey = new NestKey(newStack.toList())

        log.debug "relocate: ${oldKey} -> ${newKey}"

        exitCues.computeIfAbsent(newKey, { new ArrayDeque<>() }).add(oldKey)

        stack.clear()
        stack.addAll(newStack)
        traverseAndCreate()
    }

    void push(final String key) {
        // update the stack
        final Deque<String> oldStack = new ArrayDeque<>(stack)
        stack.addLast(key)

        // make the nested map and attach its parent map's PropertyChangeListeners to it
        this.setIgnoreNextEvent(true)
        log.debug 'before nestedData = makeObservableMap()'
        final ObservableMap nestedData = makeObservableMap()
        log.debug 'after nestedData = makeObservableMap()'
        this.setIgnoreNextEvent(true)
        log.debug 'before traverse(oldStack).getPropertyChangeListeners().each(nestedData.&addPropertyChangeListener)'
        traverse(oldStack).getPropertyChangeListeners().each(nestedData.&addPropertyChangeListener)
        log.debug 'after traverse(oldStack).getPropertyChangeListeners().each(nestedData.&addPropertyChangeListener)'

        this.setIgnoreNextEvent(false)
        log.debug 'before fireStackChangedEvent(oldStack, null, nestedData)'
        fireStackChangedEvent(oldStack, null, nestedData)
        log.debug 'after fireStackChangedEvent(oldStack, null, nestedData)'

        // add the nestedMap
        this.setIgnoreNextEvent(true) // we already fired a StackChangedEvent earlier
        putAt(oldStack, key, nestedData)
    }

    void move(final String property, final Deque<String> newLocation, final String newProperty, final Object newValue) {
        var newMap = traverseAndCreate(newLocation)

        this.setIgnoreNextEvent(true)
        newMap.put(newProperty ?: property, newValue ?: get(property))
        this.setIgnoreNextEvent(true)
        remove(property)
    }

    void pop() {
        // update the stack
        final Deque<String> oldStack = new ArrayDeque<>(stack)
        fireStackChangedEvent(oldStack, get(stack.pollLast()), null)
        NestKey key = new NestKey(oldStack.toList())
        if (exitCues.containsKey(key) && !exitCues[key].isEmpty()) {
            stack.clear()
            stack.addAll(exitCues[key].pollLast().stack)
            stack.removeLast()
            exitCues.remove(new NestKey(oldStack.toList()))
        }
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
        log.debug "fireStackChangedEvent: $oldStack -> ${this.getStack()}"
        ((PropertyChangeSupport) pcsField.get(this.getRootMap())).firePropertyChange(new StackChangedEvent(this, oldStack, this.getStack(), oldValue, newValue))
    }
    //endregion Stack

    @ApiStatus.Internal
    private ObservableMap makeObservableMap() {
        return new ObservableMap({ propertyName, newValue ->
            log.debug "event closure. ignoreNextEvent: ${this.isIgnoreNextEvent()}"
            if (this.isIgnoreNextEvent()) {
                this.setIgnoreNextEvent(false)
                return false // deny the change when asked to ignoreNextEvent
            }
            return true
        })
    }

    @ApiStatus.Internal
    private ObservableMap traverse(final Deque<String> stack = stack) {
        ObservableMap traversedMap = this.getRootMap()
        for (final String stackKey in stack) {
            traversedMap = traversedMap[stackKey] as ObservableMap
        }
        return traversedMap
    }

    @ApiStatus.Internal
    private ObservableMap traverseAndCreate(final Deque<String> stack = stack) {
        boolean originalIgnore = this.isIgnoreNextEvent()
        ObservableMap traversedMap = this.getRootMap()
        for (final String stackKey in stack) {
            ObservableMap newTraversedMap = traversedMap[stackKey] as ObservableMap
            if (newTraversedMap == null) {
                newTraversedMap = makeObservableMap()
                this.setIgnoreNextEvent(true)
                traversedMap[stackKey] = newTraversedMap
            }
            traversedMap = newTraversedMap
        }
        this.setIgnoreNextEvent(originalIgnore)
        return traversedMap
    }
}
