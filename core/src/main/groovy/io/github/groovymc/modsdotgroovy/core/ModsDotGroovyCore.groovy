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
        backingData.getRootMap().addPropertyChangeListener(this.&onPropertyChangeEvent)
    }

    Map build() {
        Map result = backingData.getRootMap()
        for (final ModsDotGroovyPlugin plugin in plugins) {
            result = MapUtils.recursivelyMerge(result, plugin.build(result))
            MapUtils.sanitizeMap(result)
        }
        return result
    }

    private void onPropertyChangeEvent(final PropertyChangeEvent event) {
        if (event instanceof ObservableMap.MultiPropertyEvent) onMultiPropertyEvent(event)
        else if (event instanceof ObservableMap.PropertyEvent) onSinglePropertyEvent(event)
    }

    @CompileDynamic
    private void onSinglePropertyEvent(final ObservableMap.PropertyEvent event) {
        String propertyName = event.propertyName
        def mapValue = event.newValue

        // Notify each of the plugins in the PriorityQueue
        for (final ModsDotGroovyPlugin plugin in plugins) {
            PluginResult result = getPluginResult(plugin, propertyName, mapValue)
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

    private void onMultiPropertyEvent(final ObservableMap.MultiPropertyEvent event) {
        // todo
    }

//    private static enum MethodToCall {
//        SET, ON_NEST_ENTER, ON_NEST_LEAVE
//
//        @Override
//        String toString() {
//            return name().toLowerCase(Locale.ROOT)
//                    .split('_')
//                    .collect { it.capitalize() }
//        }
//    }

    // todo: clean this up some more
    private PluginResult getPluginResult(final ModsDotGroovyPlugin plugin, final String propertyName, final def mapValue) {
        final String capitalizedPropertyName = propertyName.capitalize()
        boolean useGenericMethod = false

        // resolve the class tree of the plugin to find the correct method to call
        // e.g.: if the stack is "mods, modInfo" and the property name is "modId", then we want to call plugin.Mods.ModInfo.setModId()
        Class<?> classObject = plugin.class //traverseClass(stack, plugin.getClass())
        final Deque<String> stack = new ArrayDeque<>(getStack())
        if (!stack.isEmpty()) {
            for (final String className in stack) {
                try {
                    classObject = classObject.forName(classObject.name + '$' + className.capitalize())
                } catch (final ClassNotFoundException ignored) {
                    // if the class doesn't exist, then we'll just use the generic setter method
                    useGenericMethod = true
                    break
                }
            }
        }

        if (mapValue instanceof ObservableMap) {
            final int lastStackSize = getLastStackSize()
            final String methodName
            if (lastStackSize <= stack.size() && mapValue.isEmpty()) { // entering a new nest
                methodName = 'onNestEnter'
            } else if (lastStackSize > stack.size()) { // leaving the current nest
                methodName = 'onNestLeave'
            } else {
                methodName = ''
                throw new IllegalStateException("Unexpected stack size change: $lastStackSize -> ${stack.size()}")
            }

            if (useGenericMethod) {
                switch (methodName) {
                    case 'onNestEnter': return PluginResult.of(plugin.onNestEnter(stack, propertyName, mapValue))
                    case 'onNestLeave': return PluginResult.of(plugin.onNestLeave(stack, propertyName, mapValue))
                }
            }

            if (classObject.metaClass.respondsTo(classObject, methodName, stack, mapValue)) {
                return PluginResult.of(classObject.metaClass.invokeMethod(classObject, methodName, stack, mapValue))
            } else {
                switch (methodName) {
                    case 'onNestEnter': return PluginResult.of(plugin.onNestEnter(stack, propertyName, mapValue))
                    case 'onNestLeave': return PluginResult.of(plugin.onNestLeave(stack, propertyName, mapValue))
                }
            }
        }

        final String methodName = "set${capitalizedPropertyName}"
        if (useGenericMethod)
            return PluginResult.of(plugin.set(stack, propertyName, mapValue))

        if (classObject.metaClass.respondsTo(classObject, methodName, mapValue))
            return PluginResult.of(classObject.metaClass.invokeMethod(classObject, methodName, mapValue))
        else
            return PluginResult.of(plugin.set(stack, propertyName, mapValue))
    }
}
