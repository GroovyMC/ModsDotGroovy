package io.github.groovymc.modsdotgroovy.core

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import io.github.groovymc.modsdotgroovy.plugin.ModsDotGroovyPlugin
import io.github.groovymc.modsdotgroovy.plugin.PluginRegistry
import io.github.groovymc.modsdotgroovy.plugin.PluginResult

import java.beans.PropertyChangeEvent

@CompileStatic
final class ModsDotGroovyCore {
    private final PluginRegistry plugins = new PluginRegistry()

    @Delegate
    final StackAwareObservableMap backingData = new StackAwareObservableMap()

    ModsDotGroovyCore() {
        plugins*.init()
        println "[Core] Initialised plugins: ${plugins*.name}"

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
                // this is being called for stack pushes because a new property is being added to the observable map
                onSinglePropertyEvent((ObservableMap.PropertyEvent) event)
                break
            default:
                if (event.propertyName == 'size' && event.newValue != event.oldValue) return // ignore size changes
                else throw new IllegalArgumentException("Unknown event type: ${event.class.name}")
        }
    }

    @CompileDynamic
    private void onSinglePropertyEvent(final ObservableMap.PropertyEvent event) {
        String propertyName = event.propertyName
        def mapValue = event.newValue

        // Notify each of the plugins in the PriorityQueue
        for (final ModsDotGroovyPlugin plugin in plugins) {
            PluginResult result = getPluginResult(getStack(), plugin, PluginAction.SET, propertyName, mapValue)
            switch (result) {
                case PluginResult.Validate:
                    println "[Core] Plugin \"${plugin.name}\" validated property \"$propertyName\""
                    break
                case PluginResult.Change:
                    result = (PluginResult.Change) result // Groovy 3 workaround - usually this cast is unnecessary

                    // change the stack to the new location if different
                    if (result.newLocation !== null && result.newLocation != getStack()) {
                        println "[Core] Plugin \"${plugin.name}\" moved property from \"${getStack().join '->'}\" to \"${result.newLocation.join '->'}\""
                        // first, remove the property from the old location
                        setIgnoreNextEvent(true)
                        remove(propertyName)

                        // then set the new location
                        getStack().clear()
                        getStack().addAll(result.newLocation)
                    }

                    // set the new name and value
                    if (result.newPropertyName !== null && result.newPropertyName != propertyName) {
                        println "[Core] Plugin \"${plugin.name}\" renamed property \"${propertyName}\" to \"${result.newPropertyName}\""
                        propertyName = result.newPropertyName
                    }

                    if (result.newValue === null) {
                        println "[Core] Plugin \"${plugin.name}\" removed property \"$propertyName\""
                        setIgnoreNextEvent(true)
                        remove(propertyName)
                        break
                    } else if (result.newValue != mapValue) {
                        println "[Core] Plugin \"${plugin.name}\" changed property \"${propertyName}\" value from \"${mapValue}\" to \"${result.newValue}\""
                        mapValue = result.newValue
                    }

                    // and finally, put it in the map
                    setIgnoreNextEvent(true)
                    put(propertyName, mapValue)
                    break
                case PluginResult.Unhandled:
                    //println "[Core] Plugin \"${plugin.name}\" didn't handle property \"$propertyName\""
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
            println "plugin: ${plugin.name}"
            println "action: ${action}"
            println "newStack: ${event.newStack}"
            println "oldStack: ${event.oldStack}"
            println "nestName: ${stack.last}"
            println "value: ${mapValue}"
            PluginResult result = getPluginResult(stack, plugin, action, stack.last, mapValue)
            println "[Core] Plugin \"${plugin.name}\" returned result: ${result}"
            switch (result) {
                case PluginResult.Validate:
                    println "[Core] Plugin \"${plugin.name}\" validated nest \"${propertyName}\""
                    break
                case PluginResult.Change:
                    var change = (PluginResult.Change) result
                    // todo: support moving the nest to a new location
                    if (change.newPropertyName !== null) {
                        println "[Core] Plugin \"${plugin.name}\" renamed nest \"${propertyName}\" to \"${change.newPropertyName}\""
                        setIgnoreNextEvent(true)
                        var old = remove(propertyName)
                        setIgnoreNextEvent(true)
                        put(change.newPropertyName, old)
                    }
                    if (change.newValue === null) {
                        println "[Core] Plugin \"${plugin.name}\" removed nest \"${propertyName}\""
                        setIgnoreNextEvent(true)
                        remove(propertyName)
                        break
                    } else if (change.newValue != event.newValue) {
                        println "[Core] Plugin \"${plugin.name}\" changed nest \"${propertyName}\" value from \"${mapValue}\" to \"${change.newValue}\""
                        setIgnoreNextEvent(true)
                        put(propertyName, change.newValue)
                    }
                    break
                case PluginResult.Error:
                    ((PluginResult.Error) result).throwException()
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
            final String str = name().toLowerCase(Locale.ROOT).split('_').collect {it.capitalize()}.join('')
            final String firstChar = str.take(1)
            return str.replaceFirst(firstChar, firstChar.toLowerCase(Locale.ROOT))
        }
    }

    private PluginResult getPluginResult(final Deque<String> eventStack, final ModsDotGroovyPlugin plugin, final PluginAction action = PluginAction.SET, final String propertyName, final def propertyValue) {
        final String capitalizedPropertyName = propertyName.capitalize()
        boolean useGenericMethod = false

        // Todo: request support for tuple destructuring in CompileStatic
        // final def (Class<?> classObject, boolean foundSubclass) = traverseClassTree(getStack(), plugin.getClass())
        final Tuple2<Class<?>, Boolean> result = traverseClassTree(eventStack, plugin.getClass())
        final Class<?> classObject = result.v1
        final boolean foundSubclass = result.v2
        //if (!foundSubclass || plugin.getClass() != classObject) useGenericMethod = true

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
                    if (classObject.metaClass.respondsTo(classObject, methodName, propertyValue))
                        return PluginResult.of(classObject.metaClass.invokeMethod(classObject, methodName, propertyValue))
                    else
                        return PluginResult.of(plugin.set(eventStack, propertyName, propertyValue))
                case PluginAction.ON_NEST_ENTER:
                    if (classObject.metaClass.respondsTo(classObject, methodName, eventStack, (Map) propertyValue))
                        return PluginResult.of(classObject.metaClass.invokeMethod(classObject, methodName, eventStack, (Map) propertyValue))
                    else
                        return PluginResult.of(plugin.onNestEnter(eventStack, propertyName, (Map) propertyValue))
                case PluginAction.ON_NEST_LEAVE:
                    if (classObject.metaClass.respondsTo(classObject, methodName, eventStack, (Map) propertyValue))
                        return PluginResult.of(classObject.metaClass.invokeMethod(classObject, methodName, eventStack, (Map) propertyValue))
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
     * @param classObject The class to start from
     * @return A tuple containing either the traversed class or original class, and whether or not a subclass was found
     */
    private static Tuple2<Class<?>, Boolean> traverseClassTree(final Deque<String> stack, Class<?> classObject) {
        boolean foundSubclass = false
        if (!stack.isEmpty()) {
            final Deque<String> stackCopy = new ArrayDeque<>(stack)
            var name = classObject.name + '$' + stackCopy.collect {it.capitalize()}.join('$')
            try {
                classObject = classObject.forName(name)
                foundSubclass = true
            } catch (final ClassNotFoundException ignored) {
                // if the class doesn't exist, then we'll just use the generic method
                foundSubclass = false
            }
        }

        return new Tuple2<>(classObject, foundSubclass)
    }
}
