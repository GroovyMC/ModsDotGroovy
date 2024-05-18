package org.groovymc.modsdotgroovy.core

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.transform.TupleConstructor
import groovy.util.logging.Log4j2
import org.codehaus.groovy.runtime.StringGroovyMethods
import org.groovymc.modsdotgroovy.plugin.ModsDotGroovyPlugin
import org.groovymc.modsdotgroovy.plugin.NestKey
import org.groovymc.modsdotgroovy.plugin.PluginRegistry
import org.groovymc.modsdotgroovy.plugin.PluginResult
import org.groovymc.modsdotgroovy.types.core.Platform

import java.lang.reflect.Modifier

@CompileStatic
@Log4j2(category = 'MDG - Core')
final class ModsDotGroovyCore {
    private final PluginRegistry plugins = new PluginRegistry()

    final Platform platform

    final LayeredMap layeredMap = new LayeredMap()

    final LayeredMap.Listener listener

    ModsDotGroovyCore(final Map<String, ?> environment) {
        plugins*.init(environment.asImmutable())
        platform = environment.containsKey('platform')
                ? Platform.of(environment['platform'].invokeMethod('name', null) as String)
                : Platform.UNKNOWN

        // Setup transforms
        plugins.each {
            layeredMap.transforms.addAll(it.mapTransforms())
        }

        LayeredMap.Listener listener = { }
        for (final ModsDotGroovyPlugin plugin in plugins.collect().reverse()) {
            def oldListener = listener
            listener = new PluginListener(plugin, oldListener)
            plugin.initializeStackPut({ stack, value -> layeredMap.putStackedWatched(stack, value, listener) }, this)
        }
        this.listener = listener
    }

    void put(final String key, final Object value) {
        layeredMap.putWatched(key, value, listener)
    }

    void putStacked(final List<String> stack, final Object value) {
        layeredMap.putStackedWatched(stack, value, listener)
    }

    void push(final String key) {
        layeredMap.push(key)
    }

    void pop() {
        layeredMap.popWatched(listener)
    }

    Map build() {
        Map result = layeredMap.main
        for (final ModsDotGroovyPlugin plugin in plugins) {
            result = MapUtils.recursivelyMerge(result, plugin.build(result))
            MapUtils.sanitizeMap(result)
        }
        return result
    }

    @TupleConstructor
    class PluginListener implements LayeredMap.Listener {
        final ModsDotGroovyPlugin plugin
        final LayeredMap.Listener rest

        @Override
        void call(LayeredMap.MapEvent event) {
            if (event.onPop) {
                runEvent(event, PluginAction.ON_NEST_LEAVE)
            } else {
                runEvent(event, PluginAction.SET)
            }
        }

        void runEvent(LayeredMap.MapEvent event, PluginAction action) {
            String propertyName = event.key
            def mapValue = event.value
            List<String> originalStack = new ArrayList<>(event.stack)

            PluginResult result = getPluginResult(originalStack, plugin, action, propertyName, mapValue)

            if (result instanceof PluginResult.Validate) {
                log.debug "Plugin \"${plugin.name}\" validated property \"$propertyName\""
                rest.call(event)
            } else if (result instanceof PluginResult.Change) {
                LayeredMap.Listener listener = rest
                if (result.reentrant) listener = this
                mapValue = result.newValue ?: mapValue

                if (result.newValue == null) {
                    log.debug "Plugin \"${plugin.name}\" removed property \"$propertyName\""
                    layeredMap.remove(propertyName)
                } else if (result instanceof PluginResult.Rename && result.newPropertyName !== null && result.newPropertyName != propertyName) {
                    log.debug "Plugin \"${plugin.name}\" renamed property \"$propertyName\" to \"${result.newPropertyName}\""
                    if (result.newPropertyName === null) throw new IllegalStateException("Rename result must have a new property name")
                    layeredMap.remove(propertyName)
                    layeredMap.putWatched(result.newPropertyName, mapValue, listener)
                } else if (result instanceof PluginResult.Move && result.newLocation !== null && result.newLocation != (originalStack + [propertyName])) {
                    log.debug "Plugin \"${plugin.name}\" moved \"${originalStack.join('.')}.$propertyName\" to \"${result.newLocation.join('.')}\""
                    if (result.newLocation === null) throw new IllegalStateException("Move result must have a new location")
                    layeredMap.moveWatched(propertyName, result.newLocation, mapValue, listener)
                } else {
                    log.debug "Plugin \"${plugin.name}\" changed property \"$propertyName\""
                    layeredMap.putWatched(propertyName, mapValue, listener)
                }
            } else if (result instanceof PluginResult.Unhandled) {
                rest.call(event)
            } else {
                throw new IllegalStateException("Unknown PluginResult type: ${result.class.name}")
            }
        }
    }

    private static final enum PluginAction {
        SET, ON_NEST_LEAVE

        PluginAction() {}

        @Override
        String toString() {
            // converts to camelCase
            final String str = name().toLowerCase(Locale.ROOT).split('_').collect(StringGroovyMethods.&capitalize).join('')
            final String firstChar = str.take(1)
            return str.replaceFirst(firstChar, firstChar.toLowerCase(Locale.ROOT))
        }
    }

    private static PluginResult getPluginResult(final List<String> eventStack, final ModsDotGroovyPlugin plugin, final PluginAction action = PluginAction.SET, final String propertyName, final def propertyValue) {
        final List<String> fullEventStack = new ArrayList<>(eventStack)
        if (action == PluginAction.ON_NEST_LEAVE) fullEventStack.add(propertyName)

        final delegateObject = traverseClassTree(fullEventStack, plugin)

        final String methodName = action === PluginAction.SET
                ? action.toString() + propertyName.capitalize()
                : action.toString()

        switch (action) {
            case PluginAction.SET:
                if (delegateObject.metaClass.respondsTo(delegateObject, methodName, propertyValue)) {
                    // explicit setter
                    return PluginResult.of(delegateObject.metaClass.invokeMethod(delegateObject, methodName, propertyValue))
                } else if (delegateObject.metaClass.respondsTo(delegateObject, 'set', propertyName, propertyValue)) {
                    // inner generic setter
                    return PluginResult.of(delegateObject.metaClass.invokeMethod(delegateObject, 'set', propertyName, propertyValue))
                } else {
                    List<String> stack = new ArrayList<>(fullEventStack)
                    while (stack.size() > 0) {
                        final nestedObject = plugin.getNest(new NestKey(stack))
                        if (nestedObject !== null) {
                            if (nestedObject.metaClass.respondsTo(nestedObject, 'set', eventStack, propertyName, propertyValue)) {
                                return PluginResult.of(nestedObject.metaClass.invokeMethod(nestedObject, 'set', eventStack, propertyName, propertyValue))
                            }
                        }
                        stack.remove(stack.size() - 1)
                    }
                    return PluginResult.of(plugin.set(eventStack, propertyName, propertyValue))
                }
            case PluginAction.ON_NEST_LEAVE:
                if (delegateObject.metaClass.respondsTo(delegateObject, methodName, (Map) propertyValue)) {
                    return PluginResult.of(delegateObject.metaClass.invokeMethod(delegateObject, methodName, (Map) propertyValue))
                } else {
                    List<String> stack = new ArrayList<>(fullEventStack)
                    while (stack.size() > 0) {
                        final nestedObject = plugin.getNest(new NestKey(stack))
                        if (nestedObject !== null) {
                            if (nestedObject.metaClass.respondsTo(nestedObject, methodName, fullEventStack, (Map) propertyValue)) {
                                return PluginResult.of(nestedObject.metaClass.invokeMethod(nestedObject, methodName, fullEventStack, (Map) propertyValue))
                            }
                        }
                        stack.remove(stack.size() - 1)
                    }
                    return PluginResult.of(plugin.onNestLeave(fullEventStack, (Map) propertyValue))
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
     * @return the traversed class or original class
     */
    private static Object traverseClassTree(final List<String> stack, ModsDotGroovyPlugin pluginObject) {
        Object delegateObject = (Object) pluginObject

        if (stack.isEmpty())
            return delegateObject

        final Deque<String> stackCopy = new ArrayDeque<>(stack)
        List<String> stackList = []
        while (!stackCopy.empty) {
            String s = stackCopy.pollFirst()
            stackList.add(s)
            NestKey key = new NestKey(stackList)
            if (pluginObject.getNest(key) !== null) {
                delegateObject = pluginObject.getNest(key)
            } else {
                boolean found = true
                Object oldObject = (Object) delegateObject
                try {
                    delegateObject = delegateObject[s]
                    if (delegateObject === null) {
                        found = false
                    }
                    pluginObject.initializeNest(key, delegateObject)
                } catch (MissingPropertyException ignored) {
                    found = false
                    delegateObject = oldObject
                    var classSearchName = s.capitalize()
                    Class<?> innerClass = null
                    //noinspection GroovyUnusedCatchParameter
                    try {
                        innerClass = findFirstInnerClass(delegateObject.class, classSearchName)
                    } catch (IllegalStateException ignored2) {
                        // ignore
                    }
                    if (innerClass !== null) {
                        boolean has1 = false
                        if (innerClass.constructors.any {
                            if ((it.modifiers & Modifier.PUBLIC) == 0) return false
                            if (it.parameterCount === 1) {
                                has1 = true
                                return true
                            }
                            return it.parameterCount === 0
                        }) {
                            delegateObject = innerClass.metaClass.invokeConstructor(has1 ? new Object[]{delegateObject} : new Object[0])
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
                    delegateObject = pluginObject
                    break
                }
            }
        }

        return delegateObject
    }

    @Memoized
    private static Class<?> findFirstInnerClass(Class<?> clazz, String name) {
        var innerClass = clazz.classes.find {it.simpleName == name}
        if (innerClass !== null) return innerClass

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
