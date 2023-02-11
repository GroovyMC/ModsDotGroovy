# ModsDotGroovy
[![Plugin Version](https://img.shields.io/badge/dynamic/xml?style=for-the-badge&color=blue&label=Latest%20Plugin%20Version&prefix=v&query=metadata%2F%2Flatest&url=https%3A%2F%2Fmaven.moddinginquisition.org%2Freleases%2Fio%2Fgithub%2Fgroovymc%2Fmodsdotgroovy%2FModsDotGroovy%2Fmaven-metadata.xml)](https://maven.moddinginquisition.org/#/releases/io/github/groovymc/modsdotgroovy/ModsDotGroovy)
[![DSL Version](https://img.shields.io/badge/dynamic/xml?style=for-the-badge&color=blue&label=Latest%20DSL%20Version&prefix=v&query=metadata%2F%2Flatest&url=https%3A%2F%2Fmaven.moddinginquisition.org%2Freleases%2Fio%2Fgithub%2Fgroovymc%2Fmodsdotgroovy%2Fdsl%2Fmaven-metadata.xml)](https://maven.moddinginquisition.org/#/releases/io/github/groovymc/modsdotgroovy/dsl)

ModsDotGroovy is a Gradle plugin which allows writing the Forge `mods.toml` in a Groovy script, which will be compiled down to a `mods.toml` when the mod is built.
## Installation
In order to install the plugin, we first add the Modding Inquisition repository to the `settings.gradle` file:
```gradle
pluginManagement {
    repositories {
        // Other repositories here
        maven { url = 'https://maven.moddinginquisition.org/releases' }
    }
}
```
Now, we apply the plugin:
```gradle
plugins {
    // Other plugins here
    id 'io.github.groovymc.modsdotgroovy' version '1.1.2' // Version can be replaced with any existing plugin version
}
```
Then, you need to decide on a ModsDotGroovy DSL version which you want to use. You can browse all available versions [here](https://maven.moddinginquisition.org/#/releases/io/github/groovymc/modsdotgroovy/dsl).
Add the following line in your `build.gradle`, to do so:
```gradle
modsDotGroovy {
    dslVersion = '1.3.0' // Can be replaced with any existing DSL version
    platform 'forge'
}
```
## Usage
The plugin will use the file in `src/main/resources/mods.groovy` for generating the `mods.toml`. The input file can be changed in the `modsDotGroovyToToml` task.  
The `mods.groovy` file must return a `ModsDotGroovy` instance created by the `ModsDotGroovy#make(Closure)` method. Example:
```groovy
ModsDotGroovy.make {
    modLoader = 'javafml' // The mod loader of the mod
    loaderVersion = '[40,)' // The version of the modloader the mod is compatible with
    
    license = 'MIT' // The license of the mod
    // A URL to refer people to when problems occur with this mod
    issueTrackerUrl = 'https://change.me.to.your.issue.tracker.example.invalid/'

    mod {
        modId = 'mymod' // The ID of the mod
        displayName = 'My Mod' // The name of the mod

        version = this.version // The version of the mod. `this.version` refers to the `version` property in your gradle.properties file
        
        description = """
            Some very nice description.
            Groovy is the best!
        """ // A multiline description of the mod
        authors = [
                'Beans', 'Me'
        ] // A list containing the authors of the mod
        
        logoFile = "examplemod.png" // A file name (in the root of the mod JAR) containing a logo for display. Optional
        
        dependencies {
            // The `forgeVersion` and `minecraftVersion` properties are computed from the `minecraft` dependency in the `build.gradle` file
            // Alternatively, versions can be specified in the SemVer style: ">=${this.forgeVersion}"
            forge = "[${this.forgeVersion},)" // The Forge version range the mod is compatible with
            // The automatically generated `minecraftVersionRange` property is computed as: [1.$minecraftMajorVersion,1.${minecraftMajorVersion + 1})
            // Example: for a Minecraft version of 1.19, the computed `minecraftVersionRange` is [1.19,1.20)
            minecraft = this.minecraftVersionRange // The Minecraft version range the mod is compatible with

            // Declare an optional dependency against JEI
            mod('jei') {
                mandatory = false
                // Support any JEI version >= 10.0.0.0
                versionRange = "[10.0.0.0,)"
            }
        }
    }
}
```
The DSL is documented with JavaDocs which should be browsable in your IDE.

## Loader Support
The plugin can additionally be used to configure the `quilt.mod.json` file in a Quilt project, or both files in a multiloader
project. To configure the plugin for Quilt, add the following to your `build.gradle`:
```gradle
modsDotGroovy {
    //...
    platform 'quilt'
}
```
Certain quilt-specific DSL elements exist; the `this.quiltLoaderVersion` property can be used to get the version of quilt loader
present in the project. To configure the plugin for a multiloader project instead, insert the following into your root project's
`build.gradle`:
```gradle
modsDotGroovy {
    //...
    platform 'multiloader'
}
```
The plugin assumes that your subprojects for Quilt, Forge, and common code are called `Quilt`, `Forge`, and `Common` respectively.
If this is not the case, it can be configured as follows:
```gradle
modsDotGroovy {
    //...
    platform 'multiloader'
    multiloader {
        common = project(':common')
        quilt = [project(':quilt')]
        forge = [project(':forge')]
    }
}
```
The common project provides the `mods.groovy` file, which is then used to generate both a `mods.toml` and `quilt.mod.json` file.
