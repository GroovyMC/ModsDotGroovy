# Developer guide
In this guide, you'll learn how mods.groovy works and create your own mods.groovy frontend and plugin.

## Structure
In total, there's five main parts to how mods.groovy works:
1. The user's mods.groovy file
    - Interacts with the user's chosen frontend
2. Frontend
    - Provides IDE support and communicates changes to the core, where it pushes/pops the stack and sets properties
3. Plugins
    - Most functionality and validation is done here. Plugins can redirect data to different places, transform it, rename it, etc...
4. Core
    - The heart of the system. Handles stack management, communication with plugins, firing events and more
5. ModsDotGroovy Gradle plugin
    - Sets up the right plugins and frontend for your platform and handles running the user's mods.groovy file

## Quick rundown
The core starts up the plugins and informs them about the environment they're running in, such as the requested target
platform. It uses the concept of a stack to manage nested maps and fires events from a `StackAwareObservableMap`. 

Users interact with the loaded frontend through their mods.groovy file, which is loaded in by the Gradle plugin or
script mod loader. The frontend sends events to the core which modify the stack and the data on it.

Plugins can listen to these events and return a `PluginResult` to tell the core what to do with the request. This can be
anything from moving the data around, renaming its corresponding key, performing validation on the value or removing it
altogether.

At the very end, plugins are given a final chance to make any further changes, with an immutable view of the map being
built. They can respond fallback data to be merged into the map based on context of the full stack.

## Frontend
The entrypoint of the frontend should extend `ModsDotGroovyFrontend` and have static `make` methods accepting `Closure`,
`Closure, Binding` and `Closure, Map<String, ?>`.

All closure-accepting methods should have proper `@DelegatesTo` and `@ClosureParams` annotations on the closure parameters
for good IDE support.

Use setter methods or properties when setting map entries. Use closures and/or normal methods when adding values or
pushing to the stack.

For example, the following code will produce a final built Map that looks like this, assuming no plugins change or block
it:
```groovy
// mods.groovy
ExampleModsDotGroovy.make {
   url = 'https://groovymc.org'
}

@PackageScope
@CompileStatic
class ExampleModsDotGroovy extends ModsDotGroovyFrontend {
   /**@
    * Set a valid HTTP(S) URL here
    */
   String url
   
   void setUrl(final String url) {
      core.put('url', url)
   }
   
   // ...
}

// the final built Map looks like: [url: 'https://groovymc.org']
```

The above code can be simplified using the `PropertyInterceptor`, which intercepts set requests to properties and does
a core.put() call with the property name as the key – effectively the same code as above with less boilerplate:
```groovy
@PackageScope
@CompileStatic
class ExampleModsDotGroovy extends ModsDotGroovyFrontend implements Propertyinterceptor {
   /**@
    * Set a valid HTTP(S) URL here
    */
   String url
   
   // ...
}

// the final built Map is still: [url: 'https://groovymc.org']
```

### Nesting
To perform map nesting, you first push the stack to create a new nested map, put data in as usual, then pop the stack
once done:
```groovy
// mods.groovy
ExampleModsDotGroovy.make {
   url = 'https://groovymc.org'
   contact {
      username = 'Paint_Ninja'
   }
}

@PackageScope
@CompileStatic
class ExampleModsDotGroovy extends ModsDotGroovyFrontend implements Propertyinterceptor {
   /**@
    * Set a valid HTTP(S) URL here
    */
   String url
   
   void contact(@DelegatesTo(value = ContactBuilder, strategy = Closure.DELEGATE_FIRST)
                @ClosureParams(value = SimpleType, options = 'ContactBuilder') final Closure closure) {
      core.push('contact') // push a new map to the stack with the key name "contact"
      
      final contactBuilder = new ContactBuilder(core)
      closure.resolveStrategy = Closure.DELEGATE_FIRST
      closure.delegate = contactBuilder
      closure.call(contactBuilder)
      
      core.pop() // pop the stack now that we're done
   }
   
   // ...
}

class ContactBuilder extends DslBuilder implements PropertyInterceptor {
   String username

   ContactBuilder(final ModsDotGroovyCore core) {
      super(core)
   }
}

/*
 * the final built Map looks like: [
 *     url: 'https://groovymc.org',
 *     contact: [username: 'Paint_Ninja']
 * ]
 */
```

Similar to `PropertyInterceptor` there is also a `MapClosureInterceptor` trait, however, this lacks IDE support.

Optional properties should be marked with `@Nullable`, as well as the param for any corresponding setter method.
Plugins handle defaults; however, frontends should show expected defaults in their initialisers to make it clear to users.
If there is no default and the property is optional, it should have a default value of `null` to aid IDEs that don't
recognise or show the nullable annotation:
```groovy
@Nullable
String url = null
```

## Plugin
All plugins must extend the `ModsDotGroovyPlugin` class and have a registered service so that the service loader picks it up.

On `init()`, you'll be given the environment map which you can grab data from and store for later checks. Useful for
multiplatform or translator plugins where you want different output from the same mods.groovy file depending on the
target platform.

Your setter method names should match what's in the frontend, but the param type and return type can be different.
For example, if the frontend has this:
```groovy
void setUrl(final String url) {
   core.put('url', url)
}
```
Then your plugin's setter might look like this to validate it:
```groovy
void setUrl(final String url) {
   if (!PluginUtils.isValidUrl(url))
      throw new PluginResult.MDGPluginException('url must start with http:// or https://')
}
```

In this case, execution will halt and display your error message to the user when they set an invalid URL.

### Plugin results
There are different `PluginResult`s for different actions:
- Unhandled (the default – used when you don't listen for a property change)
- Validate (when returned, indicates that you've validated the property change and are happy with it)
- Change (asks for the property change to be modified in some shape or form)

There are also helper methods in the `PluginResult` class, such as `rename`, `move` and `remove`.

The return type of your plugin's setter method is important and influences how it's interpreted by the core:
- If you return a PluginResult, it'll be treated as-is. 
- If you return an Object, it'll be treated as {@code new PluginResult.Change(newValue: (yourObject))}.
- If you return null or don't return anything (void), it'll be treated as {@code new PluginResult.Validate()}.

Use `def` if you want to return more than one type or are unsure. This'll also allow for scenarios such as returning a
`PluginResult` only on certain conditions, with the default ending up as `void` which'll be treated as `PluginResult.Validate`.

// todo: examples of rename and move PluginResults

### Nesting
Inner classes are used to make it easier for plugins to handle nested properties. Consider the following:
```groovy
ExampleModsDotGroovy.make {
   contact {
      username = 'Paint_Ninja'
   }
}
```
```groovy
core.push('contact') // entering ['contact']
core.put('username', 'Paint_Ninja') // setting username to Paint_Ninja
core.pop() // leaving ['contact']
```
```groovy
class ExamplePlugin extends ModsDotGroovyPlugin {
   // ...
   
   class Contact {
      void onNestEnter(final Deque<String> stack, final String name, Map value) {
         log.debug "entering ${stack}"
      }
      
      void setUsername(final String username) {
         log.debug "setting username to ${username}"
         if (username.isBlank())
            throw new PluginResult.MDGPluginException('username cannot be blank')
      }
      
      void onNestLeave(final Deque<String> stack, final String name, Map value) {
         log.debug "leaving ${stack}"
      }
   }
}
```

Because of the way properties work in Groovy, you can define properties in the nested class and it'll be automatically
set by the generated setter method:
```groovy
class Contact {
   String username
   
   void onNestLeave(final Deque<String> stack, final String name, Map value) {
      log.debug "username is ${username}" // assuming the above core.put() code, will say "username is Paint_Ninja" on core.pop()
   }
}
```
This is useful for storing data for later validation, such as needing multiple properties set before leaving a closure/nest/stack.

You can nest inner classes as much as you like.
You can also call the core.push() method without popping first to nest inside a nested map.

### Generic setter handling
In some cases you may not always know the property or nest name statically. In these cases you can use the generic `set()` method:
```groovy
@CompileDynamic
def set(final Deque<String> stack, final String name, def value) {
    if (value instanceof Number && value == 42)
       log.debug "Meaning of life detected on property ${name} in ${stack}"
}
```

Named setter methods with a matching parameter type are prioritised, then generic setter methods inside an inner class,
then checking outer classes and finally the root generic `set` method.


## A barebones example
In this example, we'll write a basic frontend and plugin that accepts one property in the root of the map and validates
that it has been set with a valid a URL.

First, let's make the test mods.groovy file:
```groovy
BarebonesExampleModsDotGroovy.make {
    url = 'https://groovymc.org'
}
```

Now let's make the frontend for it. It needs to go in the root/unnamed package so that we can reference it from the
test mods.groovy file made earlier.
```groovy
@PackageScope
@CompileStatic
@Log4j2(category = 'MDG - BarebonesExample frontend') // setup a logger to ease debugging
class BarebonesExampleModsDotGroovy extends ModsDotGroovyFrontend {
    /**@
     * Set a URL here
     */
    @Nullable String url = null // mods.groovy convention is to mark a property as nullable when optional and null when no default is set
    
    void setUrl(final String url) {
        log.debug 'setUrl(String)' // log when this method is called
        core.put('url', url) // tell the core (from the ModsDotGroovyFrontend superclass) to put the url in the map with the key "url"
    }
    
    // the superclass needs to be provided the environment so that it can pass it along to the core and plugins
    private ModsDotGroovy(final Map<String, ?> environment) {
        super(environment)
    }

    // boilerplate for the "make" method - we need one for users (just a closure) and one that also includes the environment map param
    // we need all of these for good IDE support. Make sure the delegatesto and closureparam values are correct
    static ModsDotGroovy make(@DelegatesTo(value = BarebonesExampleModsDotGroovy, strategy = Closure.DELEGATE_FIRST)
                              @ClosureParams(value = SimpleType, options = 'BarebonesExampleModsDotGroovy') final Closure closure) {
        return make(closure, [:])
    }

    static ModsDotGroovy make(@DelegatesTo(value = BarebonesExampleModsDotGroovy, strategy = Closure.DELEGATE_FIRST)
                              @ClosureParams(value = SimpleType, options = 'BarebonesExampleModsDotGroovy') final Closure closure,
                              final Binding scriptBinding) {
        return make(closure, scriptBinding.variables)
    }

    static ModsDotGroovy make(@DelegatesTo(value = BarebonesExampleModsDotGroovy, strategy = Closure.DELEGATE_FIRST)
                              @ClosureParams(value = SimpleType, options = 'BarebonesExampleModsDotGroovy') final Closure closure,
                              final Map<String, ?> environment) {
        final ModsDotGroovy val = new ModsDotGroovy(environment)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = val
        closure.call(val)
        return val
    }
}
```

Now let's make a plugin that validates this property. We can put this in a proper package as this isn't meant to be
directly referenced by users.
```groovy
package com.example.modsdotgroovy.plugin

@CompileStatic
@SuppressWarnings('GroovyUnusedDeclaration') // All these methods are dynamically called by ModsDotGroovyCore
@Log4j2(category = 'MDG - BarebonesExamplePlugin')
class BarebonesExamplePlugin extends ModsDotGroovyPlugin {
    @Override
    void init(final Map<String, ?> environment) {
        
    }

    @Override
    Logger getLog() {
        return log
    }

    // let the core know which platforms your plugin supports
    @Override
    EnumSet<Platform> getPlatforms() {
        return EnumSet.of(Platform.FORGE)
    }
    
    // this is called when the frontend calls core.put('url', url)
    PluginResult setUrl(final String url) {
        log.debug "url: ${url}"
        if (PluginUtils.isValidUrl(url))
            return new PluginResult.Validate() // all good, tell the core we've validated this and are happy with it
        else
            throw new PluginResult.MDGPluginException('url must start with http:// or https://') // throw an error if not
    }
}
```

Make sure you also declare your plugin to the service loader:
`META-INF/services/org.groovymc.modsdotgroovy.plugin.ModsDotGroovyPlugin`
```
com.example.modsdotgroovy.plugin.BarebonesExamplePlugin
```

