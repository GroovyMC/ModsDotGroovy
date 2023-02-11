package io.github.groovymc.modsdotgroovy.core

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import io.github.groovymc.modsdotgroovy.plugin.ModsDotGroovyPlugin
import io.github.groovymc.modsdotgroovy.plugin.PluginResult

// todo: support notifying plugins of nesting (core.push()) by making a new instance of a plugin's inner class and removing that instance from dynamicInstance.pluginInstances when the stack is popped (core.pop())
@CompileStatic
final class ModsDotGroovyCore {
    private final PriorityQueue<ModsDotGroovyPlugin> plugins = new PriorityQueue<>(
            new Comparator<ModsDotGroovyPlugin>() {
                @Override
                int compare(ModsDotGroovyPlugin o1, ModsDotGroovyPlugin o2) {
                    return -(o1.priority <=> o2.priority)
                }
            }
    )
    private final ObservableMap backingData = new ObservableMap({ propertyName, newValue ->
        if (ignoreNextEvent) {
            ignoreNextEvent = false
            return false
        }
        println "[Core] root observableMapTestClosure(propertyName: $propertyName, newValue: $newValue)"
        return true
    })
    private final ModsDotGroovyCoreDynamic dynamicInstance = new ModsDotGroovyCoreDynamic()
    private final Map defaults

    final Deque<String> stack = new ArrayDeque<>()

    boolean ignoreNextEvent = false // avoid infinite loop when a plugin changes a property in the backingData map

    PriorityQueue<ModsDotGroovyPlugin> getPlugins() {
        return this.@plugins
    }

    Map build() {
        final Map result = MapUtils.recursivelyMerge(this.@backingData, defaults)
        MapUtils.sanitizeMap(result)
        return result
    }

    /**
     * Push a new nested Map onto the stack.
     * @param key The key to use for the new Map.
     */
    void push(final String key) {
        println "[Core] push(key: $key)"
        ignoreNextEvent = true
        final ObservableMap nestedObservableMap = new ObservableMap({ propertyName, newValue ->
            if (ignoreNextEvent) {
                ignoreNextEvent = false
                return false
            }
            println "[Core] nested (${stack.toString()[1..-2].replace(', ', '->')}) observableMapTestClosure(propertyName: ${propertyName}, newValue: ${newValue})"
            return true
        })
        backingData.getPropertyChangeListeners().each { nestedObservableMap.addPropertyChangeListener(it) }
        put(key, nestedObservableMap)
        stack.addLast(key)
    }

    /**
     * Pop the top nested Map off the stack.
     * Pushing the same key as the one just popped off will overwrite the previous Map's contents.
     */
    void pop() {
        println "[Core] pop()"
        stack.pollLast()
    }

    /**
     * Put a value into the current nested Map, or the root Map if the stack is empty.
     * @param key
     * @param value
     */
    void put(final String key, final def value) {
        println "[Core] put(key: $key, value: $value) stack: ${stack.toString()}"

        if (stack.isEmpty()) backingData[key] = value
        else traverseStackAwareMap()[key] = value
    }

    /**
     * Removes a value from the current nested Map, or the root Map if the stack is empty.
     * @param key
     */
    void remove(final String key) {
        println "[Core] remove(key: $key) stack: ${stack.toString()}"

        if (stack.isEmpty()) backingData.remove(key)
        else traverseStackAwareMap().remove(key)
    }

    Map traverseStackAwareMap(final Deque<String> stack = this.stack) {
        def traversedMap = backingData
        for (final String stackKey in stack) {
            traversedMap = traversedMap[stackKey]
        }
        return traversedMap as Map
    }

    ModsDotGroovyCore() {
        // Load plugins
        plugins.addAll(ServiceLoader.load(ModsDotGroovyPlugin).toList())
        println "[Core] Loaded plugins: ${plugins*.name}"
        plugins*.init()
        defaults = MapUtils.recursivelyMerge((plugins*.defaults as List<Map>).findAll { it !== null })
        setupEventListener()
    }

    @CompileDynamic
    private void setupEventListener() {
        plugins.each { plugin -> dynamicInstance.pluginInstances[plugin.name] = [instance: plugin] }

        backingData.addPropertyChangeListener { event ->
            if (ignoreNextEvent) {
                ignoreNextEvent = false
                return
            }
            if (event instanceof ObservableMap.PropertyEvent) {
                final propertyName = event.propertyName
                final mapValue = event.newValue
                def result = mapValue
                println "[Core] propertyEvent(propName: $propertyName, mapValue: $mapValue)"
                if (event instanceof ObservableMap.PropertyAddedEvent || event instanceof ObservableMap.PropertyUpdatedEvent) {
                    final capitalizedPropertyName = propertyName.capitalize()
                    for (final ModsDotGroovyPlugin plugin in plugins) {
                        def classObject = dynamicInstance.pluginInstances[plugin.name]['instance']
                        if (!stack.isEmpty()) {
                            // resolve the class tree of the plugin to find the correct method to call
                            // e.g.: if the stack is "mods, modInfo" and the property name is "modId", then we want to call plugin.Mods.ModInfo.setModId()
                            def previousClass = classObject
                            Map currentClassesMap = dynamicInstance.pluginInstances[plugin.name]
                            for (final className in stack) {
                                final capitalizedClassName = className.capitalize()
                                if (currentClassesMap.containsKey(capitalizedClassName)) {
                                    // a class instance already exists, so let's use it
                                    classObject = currentClassesMap[capitalizedClassName]['instance']
                                } else {
                                    // a class instance doesn't exist, so let's create it
                                    // first, let's find the declared subclass
                                    final Class<?> tmp = classObject.getClass().declaredClasses.find {
                                        it.simpleName == className.capitalize()
                                    }
                                    if (tmp === null) {
                                        // fail fast when we can't find the subclass
                                        break
                                    } else {
                                        // create a new instance of the subclass
                                        final constructors = tmp.getDeclaredConstructors()
                                        if (constructors.length === 0) {
                                            // static inner class
                                            classObject = tmp
                                        } else {
                                            // instance inner class
                                            final constructor = constructors[0]
                                            if (constructor.parameterCount === 0) classObject = constructor.newInstance()
                                            else classObject = constructor.newInstance(previousClass)
                                        }

                                        // store the new instance in the pluginInstances map and set the new values for the next iteration
                                        dynamicInstance.pluginInstances[plugin.name][capitalizedClassName] = [instance: classObject]
                                        previousClass = classObject
                                        currentClassesMap = dynamicInstance.pluginInstances[plugin.name][capitalizedClassName]
                                    }
                                }
                            }
                        }

                        // first try a dedicated, typed setter method
                        if (classObject.metaClass.respondsTo(classObject, "set$capitalizedPropertyName", mapValue)) {
                            result = classObject.metaClass.invokeMethod(classObject, "set$capitalizedPropertyName", mapValue)
                        } else {
                            // fallback to the generic set method
                            result = plugin.set(stack, propertyName, mapValue)
                        }

                        if (result === null) {
                            result = new Tuple2<PluginResult, ?>(PluginResult.VALIDATE, null)
                        } else if (result instanceof PluginResult) {
                            result = new Tuple2<PluginResult, ?>(result, null)
                        } else if (result !instanceof Tuple2) {
                            result = new Tuple2<PluginResult, ?>(PluginResult.TRANSFORM, result)
                        }
                        result = result as Tuple2<PluginResult, ?>

                        if (result.v1 === PluginResult.UNHANDLED) {
                            println "[Core] Plugin \"${plugin.name}\" didn't handle property \"$propertyName\""
                        } else if (result.v1 === PluginResult.VALIDATE) {
                            println "[Core] Plugin \"${plugin.name}\" validated property \"$propertyName\""
                        } else if (result.v1 === PluginResult.TRANSFORM) {
                            def resultV2 = result.v2
                            // handle move requests, which are done by returning a Tuple2<Deque<String>, ?> from a transform plugin result
                            if (resultV2 instanceof Tuple2 && resultV2.v1 instanceof Deque) {
                                final Deque<String> newStack = resultV2.v1 as Deque<String>
                                final Deque<String> oldStack = stack.clone()
                                resultV2 = resultV2.v2
                                stack.clear()
                                stack.addAll(newStack)
                                ignoreNextEvent = true
                                put(propertyName, resultV2)
                                stack.clear()
                                stack.addAll(oldStack)
                                ignoreNextEvent = true
                                remove(propertyName)
                                if (mapValue == resultV2) {
                                    println "[Core] Plugin \"${plugin.name}\" moved property \"$propertyName\" from stack \"${oldStack.join '->'}\" to \"${newStack.join '->'}\""
                                } else {
                                    println "[Core] Plugin \"${plugin.name}\" transformed property \"$propertyName\" from \"$mapValue\" to \"${resultV2}\" and moved it from stack \"${oldStack.join '->'}\" to \"${newStack.join '->'}\""
                                }
                                continue
                            }
                            println "[Core] Plugin \"${plugin.name}\" transformed property \"$propertyName\" from \"$mapValue\" to \"${result.v2}\""
                            ignoreNextEvent = true
                            put(propertyName, resultV2)
                        } else if (result.v1 === PluginResult.BREAK) {
                            println "[Core] Plugin \"${plugin.name}\" transformed property \"$propertyName\" from \"$mapValue\" to \"${result.v2}\" and broke the propagation chain"
                            ignoreNextEvent = true
                            put(propertyName, result.v2)
                            break
                        } else if (result.v1 === PluginResult.IGNORE) {
                            println "[Core] Plugin \"${plugin.name}\" handled property \"$propertyName\" and wants it to be ignored"
                            ignoreNextEvent = true
                            remove(propertyName)
                            break
                        }
                    }
                } else if (event instanceof ObservableMap.PropertyRemovedEvent) {
                    // todo: figure out how deleting a property should work, if allowed at all (plugins setting properties to null?)
                    println "[Core] Property $propertyName was removed. Event: $event"
                }
            } else if (!(event.propertyName == 'size' && (event.newValue === event.oldValue + 1 || event.newValue === event.oldValue - 1))) { // Ignore Map.size() change event
                println "[Core] Unknown event: $event"
            }
        }
    }
}
