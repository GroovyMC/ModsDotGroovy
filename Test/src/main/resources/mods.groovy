ModsDotGroovy.make {
    modLoader = 'gml'
    loaderVersion = '[1,'

    license = 'MIT'
    sourcesUrl = 'https://github.com/GroovyMC/ModsDotGroovy'

    mod {
        modId = 'examplemod'
        version = '1.0.0'

        contributors = [
                Owner: ['GroovyMC'],
                Author: ['Matyrobbrt', 'Paint_Ninja', 'lukebemish']
        ]
        description = 'A very nice example mod'

        credits = "${buildProperties.credits}"

        // for testing the inferred updateJsonUrl feature - issueTrackerUrl and links to GitHub repos are also supported by this feature
        displayUrl = 'https://curseforge.com/minecraft/mc-mods/spammycombat'

        displayTest = DisplayTest.IGNORE_SERVER_VERSION

        onForge {
            customProperty = 'hello_forge'
        }

        onNeoForge {
            customProperty = 'hello_neoforge'
        }

        onFabricAndQuilt {
            customProperty = 'hello_quabric'
        }

        dependencies {
            //minecraft = 1.19..1.20 // equivalent to `minecraft = '[1.19,1.20)'`
            minecraft = 19.4..20.0 // equivalent to `minecraft = '[1.19.4,1.20)'`
            forge = '[43.0.0,)' // equivalent to `forge { versionRange = '[43.0.0,)' }`
            neoforge = '[20.2,)' // equivalent to `neoforge { versionRange = '[20.2,)' }`
            quiltLoader = '>=0.17.3'
            fabricLoader = '[0.14.19,)'

            onForge {
                mod {
                    modId = 'dynamic_asset_generator'
                    versionRange = versionRange {
                        lower '1.0.1'
                    }
                    side = DependencySide.CLIENT
                }
            }

            onQuilt {
                mod {
                    modId = 'roughlyenoughitems'
                    versionRange = '9.0.0 - 9.1'
                    mandatory = false
                }
            }

            onFabric {
                mod {
                    modId = 'fabric-api'
                    versionRange = '>0.81'
                    mandatory = false
                }
            }

            mod {
                modId = 'patchouli'
                versionRange = '[1.1,)'
                ordering = DependencyOrdering.AFTER
                side = DependencySide.BOTH
            }
        }

        // Configure quilt-specific data
        onQuilt {
            intermediate_mappings = "net.fabricmc:intermediary"
        }

        // Quabric entrypoints
        entrypoints {
            onQuilt {
                init = adapted {
                    value = 'test.no.No'
                }
            }
            onFabric {
                main = 'mymod.Hi'
            }
            client = 'test.no.NoClient'
        }
    }

    mixin = 'no.mixin.json'
    mixin = 'test.mixin.json'
}
