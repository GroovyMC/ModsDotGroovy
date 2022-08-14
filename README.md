# ModsDotGroovy
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
    id 'io.github.groovymc.modsdotgroovy' version '1.0.1' // Version can be replaced with any existing plugin version
}
```
Then, you need to decide on a ModsDotGroovy DSL version which you want to use. You can browse all available versions [here](https://maven.moddinginquisition.org/#/releases/io/github/groovymc/modsdotgroovy/dsl).
Add the following line in your `build.gradle`, to do so:
```gradle
modsDotGroovy {
    dslVersion = '1.0.1' // Can be replaced with any existing DSL version
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