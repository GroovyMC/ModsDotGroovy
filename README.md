# ModsDotGroovy
[![Version](https://img.shields.io/maven-central/v/org.groovymc.modsdotgroovy/modsdotgroovy?style=for-the-badge&color=blue&label=Latest%20Version&prefix=v)](https://central.sonatype.com/artifact/org.groovymc.modsdotgroovy/modsdotgroovy/)

ModsDotGroovy v2 is a tool that allows writing Minecraft mod metadata files in Groovy which is then compiled down to
a `mods.toml`, `fabric.mod.json`, `quilt.mod.json` and/or `plugin.yml` when the mod is built.

It features a swappable frontend for adjusting IDE suggestions based on your target mod loader and a plugin system for
adding support for new platforms and functionality.

## Getting started
To start using mods.groovy, simply add the plugin to your `build.gradle`:
```groovy
plugins {
    id 'org.groovymc.modsdotgroovy' version '2.0.0'
}
```

Then add a `mods.groovy` file to your resources folder:
```
└──📂 src
   └──📂 main
      └──📂 resources
         └──📄 mods.groovy
```

You can find examples of mods.groovy files in the Test directory of this repo.

By default, the Gradle plugin detects your platform and sets up the right mods.groovy DSL frontend and plugins for you.
The built toml/json files are put in your built jar in the place your chosen mod loader expects.

## Usage
This varies a bit depending on your chosen platform, as we try to maintain familiarity with your existing platform's
format where possible. Some plugins may provide smart defaults, such as generating a functional `updateJsonUrl` on Forge
when it detects that you've set a working CurseForge link as your `displayUrl`.

Refer to the documentation for your specific platform for more details:
- [Forge]() (todo)
- [Fabric]() (todo)
- [Quilt]() (todo)
- [Spigot]() (todo)
- [Multiplatform]() (todo)

However, the following is a general guide that should work in most cases:
- Your mods.groovy file should start with `ModsDotGroovy.make {` or `(platform name)ModsDotGroovy.make {` (e.g.: `FabricModsDotGroovy.make {`)
- When inside a closure (the `{}`), you can type `it.` to get IDE suggestions based on your current context
- You can access build properties with `buildProperties['name']`
    - You may need to cast these to booleans or ints where appropriate (e.g. `buildProperties['enableSpecialThing'] as boolean`)
- Avoid using the `this` keyword as it refers to the script context rather than your mods.groovy closure context, which may cause unexpected behaviour.

## Customising mods.groovy
### Adding plugins
You can add additional mods.groovy plugins for supporting more platforms, adding additional validation,
new functionality, and more.

To do so, add an `mdgPlugin` dependency to your project and the Gradle plugin should pick it up. Here's an example
of adding the stock plugins:
```groovy
dependencies {
    mdgPlugin 'org.groovymc.modsdotgroovy:stock-plugins'
}
```
Note! The stock plugins are automatically added for you by the Gradle plugin, unless you explicitly tell it not to
(see [automatic configuration and setup](#automatic-configuration-and-setup)).

### Changing DSL frontends
The frontend DSL is what determines IDE support and routes your code to the plugins. These can be swapped out to
improve IDE support, as you'll only get suggestions for things that apply to your specific platform(s).

To do so, add an `mdgFrontend` dependency to your project and the Gradle plugin should pick it up. Here's an example
of using the stock multiplatform frontend:
```groovy
dependencies {
    mdgFrontend 'org.groovymc.modsdotgroovy:frontend-dsl-multiplatform'
}
```
Note! Stock frontends are automatically swapped out for you by the Gradle plugin, unless you explicitly tell it not to
(see [automatic configuration and setup](#automatic-configuration-and-setup)).

### Changing platforms
On Gradle, you can explicitly define which platform(s) you want. This is auto-detected if omitted.
```groovy
import org.groovymc.modsdotgroovy.types.core.Platform

modsDotGroovy {
    platform = Platform.FORGE
}

```
You can also specify a list for multiplatform:
```groovy
import org.groovymc.modsdotgroovy.types.core.Platform

modsDotGroovy {
    platform = [Platform.FORGE, Platform.FABRIC]
}
```

This determines the output format (for example, mods.toml for `Platform.FORGE`) as well as which plugins and frontend
to use (unless you explicitly tell the Gradle plugin not to set up the DSL and plugins for you).

### Providing build properties
In mods.groovy, you can access build properties you expose with `buildProperties['propertyName']`. To do so, you must
first explicitly tell mods.groovy to include them:
```groovy
modsDotGroovy.gather {
    projectProperty 'propertyName'
}
```

### Automatic configuration and setup
Most of the time you don't need to turn this off, but for the edge-cases where you do, you can do so with:
```groovy
modsDotGroovy {
    // creates the modsDotGroovyToX Gradle tasks, using the mods.groovy file in your main sourceset as the input
    automaticConfiguration = false
    
    // adds the right frontend DSL to your project based on the detected (or explicitly defined) platform
    setupDsl = false
    
    // adds the stock plugins to your project
    setupPlugins = false
  
    // attempts to automatically infer details such as platform or minecraft version for you project
    inferGether = false
}
```

## Extending mods.groovy
If you're a developer interested in writing your own plugins or frontend DSLs, refer to the developer guide for more
information.

