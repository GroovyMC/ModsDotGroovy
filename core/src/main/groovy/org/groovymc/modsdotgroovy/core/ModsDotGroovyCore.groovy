package org.groovymc.modsdotgroovy.core

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.plugin.ModsDotGroovyPlugin
import org.groovymc.modsdotgroovy.plugin.NestKey
import org.groovymc.modsdotgroovy.plugin.PluginRegistry
import org.groovymc.modsdotgroovy.plugin.PluginResult
import org.codehaus.groovy.runtime.StringGroovyMethods

import java.beans.PropertyChangeEvent
import java.lang.reflect.Modifier

@CompileStatic
@Log4j2(category = 'MDG - Core')
final class ModsDotGroovyCore {
    private final PluginRegistry plugins

    @Delegate
    final StackAwareObservableMap backingData = new StackAwareObservableMap()

    ModsDotGroovyCore(final Map<String, ?> environment) {
        Platform platform = environment["platform"]?.asType(Platform) ?: null
        plugins = new PluginRegistry(platform)
        plugins*.init(environment.asImmutable())

        // Setup backingData event listeners
        backingData.getRootMap().addPropertyChangeListener(this.&listenPropertyChangeEvent)
    }

    Map build() {
        Map result = backingData.getRootMap()
        for (final ModsDotGroovyPlugin plugin in plugins) {
            result = MapUtils.recursivelyMerge(result, plugin.build(result))
            MapUtils.sanitizeMap(result)
        }
        return result
    }

    private void listenPropertyChangeEvent(final PropertyChangeEvent event) {
        switch (event) {
            case StackAwareObservableMap.StackChangedEvent:
                onStackChangedEvent((StackAwareObservableMap.StackChangedEvent) event)
                break
            case ObservableMap.MultiPropertyEvent:
                onMultiPropertyEvent((ObservableMap.MultiPropertyEvent) event)
                break
            case ObservableMap.PropertyEvent:
                onSinglePropertyEvent((ObservableMap.PropertyEvent) event)
                break
            default:
                if (event.propertyName == 'size' && event.newValue != event.oldValue) return // ignore size changes
                else throw new IllegalArgumentException("Unknown event type: ${event.class.name}")
        }
    }

    @CompileDynamic
    private void onSinglePropertyEvent(final ObservableMap.PropertyEvent event) {
        if (event instanceof ObservableMap.PropertyRemovedEvent) return // ignore removals

        String propertyName = event.propertyName
        def mapValue = event.newValue

        // Notify each of the plugins in the PriorityQueue
        Deque<String> originalStack = new ArrayDeque<>(getStack())
        for (final ModsDotGroovyPlugin plugin in plugins) {
            PluginResult result = getPluginResult(getStack(), plugin, PluginAction.SET, propertyName, mapValue)
            switch (result) {
                case PluginResult.Validate:
                    log.debug "Plugin \"${plugin.name}\" validated property \"$propertyName\""
                    break
                case PluginResult.Change:
                    result = (PluginResult.Change) result

                    if (result.newLocation !== null && result.newLocation != originalStack && result.newValue !== null) {
                        log.debug "Plugin \"${plugin.name}\" moved property from \"${getStack().join '->'}\" to \"${result.newLocation.join '->'}\""
                        move(propertyName, result.newLocation, result.newPropertyName, result.newValue)
                        break
                    }
                    if (result.newPropertyName !== null && result.newValue !== null) {
                        log.debug "Plugin \"${plugin.name}\" renamed property \"${propertyName}\" to \"${result.newPropertyName}\""

                        // first remove the old property
                        setIgnoreNextEvent(true)
                        remove(propertyName)

                        // then add the new property
                        propertyName = result.newPropertyName
                        setIgnoreNextEvent(true)
                        put(propertyName, result.newValue)
                    }
                    if (result.newValue === null) {
                        log.debug "Plugin \"${plugin.name}\" removed property \"${propertyName}\""
                        setIgnoreNextEvent(true)
                        remove(propertyName)
                        break
                    } else if (result.newValue != event.newValue) {
                        log.debug "Plugin \"${plugin.name}\" changed property \"${propertyName}\" value from \"${mapValue}\" to \"${result.newValue}\""
                        setIgnoreNextEvent(true)
                        put(propertyName, result.newValue)
                    }
                    break
                case PluginResult.Unhandled:
                    //log.debug "Plugin \"${plugin.name}\" didn't handle property \"$propertyName\""
                    break
                default:
                    throw new IllegalStateException("Unknown PluginResult type: ${result.class.name}")
            }
        }
    }

    // todo
    private static void onMultiPropertyEvent(final ObservableMap.MultiPropertyEvent event) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("MultiPropertyEvent not yet implemented")
    }

    private void onStackChangedEvent(final StackAwareObservableMap.StackChangedEvent event) {
        final int oldStackSize = event.oldStack.size()
        final int newStackSize = event.newStack.size()
        final PluginAction action = newStackSize > oldStackSize
                ? PluginAction.ON_NEST_ENTER
                : PluginAction.ON_NEST_LEAVE

        final Deque<String> stack = newStackSize > oldStackSize
                ? event.newStack
                : event.oldStack

        def mapValue = event.newValue ?: event.oldValue

        String propertyName = stack.last

        for (final ModsDotGroovyPlugin plugin in plugins) {
            log.debug "plugin: ${plugin.name}"
            log.debug "action: ${action}"
            log.debug "newStack: ${event.newStack}"
            log.debug "oldStack: ${event.oldStack}"
            log.debug "nestName: ${stack.last}"
            log.debug "value: ${mapValue}"
            PluginResult result = getPluginResult(stack, plugin, action, stack.last, mapValue)
            log.debug "Plugin \"${plugin.name}\" returned result: ${result}"
            switch (result) {
                case PluginResult.Validate:
                    log.debug "Plugin \"${plugin.name}\" validated nest \"${propertyName}\""
                    break
                case PluginResult.Change:
                    var change = (PluginResult.Change) result
                    if (change.newLocation !== null && (change.newValue != null || action == PluginAction.ON_NEST_ENTER)) {
                        log.debug "Plugin \"${plugin.name}\" moved nest \"${propertyName}\" from \"${event.oldStack.join '->'}\" to \"${change.newLocation.join '->'}\""
                        switch (action) {
                            case PluginAction.ON_NEST_ENTER:
                                relocate(change.newLocation)
                                break
                            case PluginAction.ON_NEST_LEAVE:
                                move(propertyName, change.newLocation, change.newPropertyName, change.newValue)
                                break
                            default:
                                throw new IllegalStateException("Unknown PluginAction type: ${action.class.name}")
                        }
                        break
                    }
                    if (change.newPropertyName !== null && change.newValue !== null) {
                        log.debug "Plugin \"${plugin.name}\" renamed nest \"${propertyName}\" to \"${change.newPropertyName}\""

                        // first remove the old property
                        setIgnoreNextEvent(true)
                        remove(propertyName)

                        // then add the new property
                        propertyName = change.newPropertyName
                        setIgnoreNextEvent(true)
                        put(propertyName, change.newValue)
                    }
                    if (change.newValue === null) {
                        log.debug "Plugin \"${plugin.name}\" removed nest \"${propertyName}\""
                        setIgnoreNextEvent(true)
                        put(propertyName, null)
                        break
                    } else if (change.newValue != event.newValue) {
                        log.debug "Plugin \"${plugin.name}\" changed nest \"${propertyName}\" value from \"${mapValue}\" to \"${change.newValue}\""
                        setIgnoreNextEvent(true)
                        put(propertyName, change.newValue)
                    }
                    break
                case PluginResult.Unhandled:
                    break
                default:
                    throw new IllegalStateException("Unknown PluginResult type: ${result.class.name}")
            }
        }
    }

    private static final enum PluginAction {
        SET, ON_NEST_ENTER, ON_NEST_LEAVE

        PluginAction() {}

        @Override
        String toString() {
            // converts to camelCase
            final String str = name().toLowerCase(Locale.ROOT).split('_').collect(StringGroovyMethods.&capitalize).join('')
            final String firstChar = str.take(1)
            return str.replaceFirst(firstChar, firstChar.toLowerCase(Locale.ROOT))
        }
    }

    private PluginResult getPluginResult(final Deque<String> eventStack, final ModsDotGroovyPlugin plugin, final PluginAction action = PluginAction.SET, final String propertyName, final def propertyValue) {
        final String capitalizedPropertyName = propertyName.capitalize()
        boolean useGenericMethod = false

        // Todo: request support for tuple destructuring in CompileStatic
        // final def (Class<?> delegateObject, boolean foundSubclass) = traverseClassTree(getStack(), plugin.getClass())
        final Tuple2<Object, Boolean> result = traverseClassTree(eventStack, plugin)
        final Object delegateObject = result.v1

        if (useGenericMethod) {
            switch (action) {
                case PluginAction.SET:
                    return PluginResult.of(plugin.set(eventStack, propertyName, propertyValue))
                case PluginAction.ON_NEST_ENTER:
                    return PluginResult.of(plugin.onNestEnter(eventStack, propertyName, (Map) propertyValue))
                case PluginAction.ON_NEST_LEAVE:
                    return PluginResult.of(plugin.onNestLeave(eventStack, propertyName, (Map) propertyValue))
            }
        } else {
            final String methodName = action === PluginAction.SET
                    ? action.toString() + capitalizedPropertyName
                    : action.toString()

            switch (action) {
                case PluginAction.SET:
                    if (delegateObject.metaClass.respondsTo(delegateObject, methodName, propertyValue)) // explicit setter
                        return PluginResult.of(delegateObject.metaClass.invokeMethod(delegateObject, methodName, propertyValue))
                    else if (delegateObject.metaClass.respondsTo(delegateObject, 'set', eventStack, propertyName, propertyValue)) // inner generic setter
                        return PluginResult.of(delegateObject.metaClass.invokeMethod(delegateObject, 'set', eventStack, propertyName, propertyValue))
                    else // outer generic setter
                        return PluginResult.of(plugin.set(eventStack, propertyName, propertyValue))
                case PluginAction.ON_NEST_ENTER:
                    if (delegateObject.metaClass.respondsTo(delegateObject, methodName, eventStack, (Map) propertyValue))
                        return PluginResult.of(delegateObject.metaClass.invokeMethod(delegateObject, methodName, eventStack, (Map) propertyValue))
                    else
                        return PluginResult.of(plugin.onNestEnter(eventStack, propertyName, (Map) propertyValue))
                case PluginAction.ON_NEST_LEAVE:
                    if (delegateObject.metaClass.respondsTo(delegateObject, methodName, eventStack, (Map) propertyValue))
                        return PluginResult.of(delegateObject.metaClass.invokeMethod(delegateObject, methodName, eventStack, (Map) propertyValue))
                    else
                        return PluginResult.of(plugin.onNestLeave(eventStack, propertyName, (Map) propertyValue))
            }
        }
    }

    /**
     * Attempts to traverse the class tree of the plugin to find the correct method to call.<br>
     * For example, if the stack is "mods, modInfo" and the provided class object has those subclasses, this method will
     * return the class object for the "Mods.ModInfo" class and true. If the class object doesn't have those subclasses,
     * then it will return the original class object and false.
     * @param stack The subclasses to traverse
     * @param delegateObject The plugin object to start from
     * @return A tuple containing either the traversed class or original class, and whether or not a subclass was found
     */
    private static Tuple2<Object, Boolean> traverseClassTree(final Deque<String> stack, ModsDotGroovyPlugin pluginObject) {
        boolean foundSubclass = false
        Object delegateObject = (Object) pluginObject

        if (stack.isEmpty())
            return new Tuple2<>(delegateObject, foundSubclass)

        final Deque<String> stackCopy = new ArrayDeque<>(stack)
        List<String> stackList = []
        while (!stackCopy.empty) {
            String s = stackCopy.pollFirst()
            stackList.add(s)
            NestKey key = new NestKey(stackList)
            if (pluginObject.getNest(key) !== null) {
                delegateObject = pluginObject.getNest(key)
                foundSubclass = true
            } else {
                boolean found = true
                Object oldObject = (Object) delegateObject
                try {
                    delegateObject = delegateObject[s]
                    if (delegateObject == null) {
                        found = false
                    }
                    pluginObject.initializeNest(key, delegateObject)
                } catch (MissingPropertyException ignored) {
                    found = false
                    delegateObject = oldObject
                    var classSearchName = s.capitalize()
                    Class<?> innerClass
                    try {
                        innerClass = findFirstInnerClass(delegateObject.class, classSearchName)
                    } catch (IllegalStateException ignored2) {
                        // ignore
                    }
                    if (innerClass != null) {
                        boolean has1 = false
                        if (innerClass.constructors.any {
                            if ((it.modifiers & Modifier.PUBLIC) == 0) return false
                            if (it.parameterCount === 1) {
                                has1 = true
                                return true
                            }
                            return it.parameterCount === 0
                        }) {
                            delegateObject = innerClass.metaClass.invokeConstructor(has1 ? new Object[]{delegateObject} : new Object[]{})
                            pluginObject.initializeNest(key, delegateObject)
                            found = true
                        }
                        var fields = innerClass.fields.findAll { it.type == innerClass && (it.modifiers & Modifier.STATIC) != 0 }
                        if (fields.size() === 1) {
                            delegateObject = fields[0].get(delegateObject)
                            pluginObject.initializeNest(key, delegateObject)
                            found = true
                        }
                    }
                }
                if (!found) {
                    foundSubclass = false
                    delegateObject = pluginObject
                    break
                }
                foundSubclass = true
            }
        }

        return new Tuple2<>(delegateObject, foundSubclass)
    }

    @Memoized
    private static Class<?> findFirstInnerClass(Class<?> clazz, String name) {
        var innerClass = clazz.classes.find {it.simpleName == name}
        if (innerClass != null) return innerClass

        var matchesFromInterfaces = clazz.interfaces.collect {
            findFirstInnerClass(it, name)
        }

        if (matchesFromInterfaces.size() === 1) {
            return matchesFromInterfaces[0]
        } else if (matchesFromInterfaces.size() > 1) {
            throw new IllegalStateException("Multiple inner classes with the name \"${name}\" found in the class tree of \"${clazz.name}\"")
        }

        if (clazz.superclass !== null && ModsDotGroovyPlugin.class.isAssignableFrom(clazz.superclass)) {
            return findFirstInnerClass(clazz.superclass, name)
        }

        return null
    }
}
