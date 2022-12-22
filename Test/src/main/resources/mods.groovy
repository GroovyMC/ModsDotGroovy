ModsDotGroovy.make {
    modLoader = 'gml'
    loaderVersion = '[1,'

    license = 'MIT'

    mod {
        modId = 'examplemod'
        version = '1.0.0'

        contributors = [
                Owner: ['GroovyMC'],
                Author: ['Matyrobbrt', 'Paint_Ninja', 'lukebemish']
        ]

        contact.source = 'https://github.com/GroovyMC/ModsDotGroovy'
        credits = "${buildProperties.credits}"

        // for testing the inferred updateJsonUrl feature - issueTrackerUrl and links to GitHub repos are also supported by this feature
        displayUrl = 'https://curseforge.com/minecraft/mc-mods/spammycombat?projectId=623297'

        onForge {
            customProperty = 'hello'
        }

        dependencies {
            minecraft = 1.19..1.20 // equivalent to `minecraft = '[1.19,1.20)'`
            forge = '[43.0.0,)' // equivalent to `forge { versionRange = '[43.0.0,)' }`
            quiltLoader = '>=0.17.3'

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

        // Quilt entrypoints
        entrypoints {
            init = adapted {
                value = 'test.no.No'
            }
            client_init = 'test.no.NoClient'
        }
    }

    onQuilt {
        mixin = "no.mixin.json"
    }
}
