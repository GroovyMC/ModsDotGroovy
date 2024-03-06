final mdg = MultiplatformModsDotGroovy.make {
    modLoader = 'gml'
    loaderVersion = '[1,)'

    license = 'All Rights Reserved'
    issueTrackerUrl = 'https://github.com/zlepper/itlt'

    mod {
        modId = 'examplemod'
        displayName = 'Example Mod'
        version = environmentInfo.version
        description = 'This is an example mod.'
        //authors = ['Example Mod Author']
        authors {
            person('Example Mod Author') {
                email = 'example@example.com'
            }
            person('Another Author')
        }
        logoFile = 'logo.png'

        // for testing the inferred updateJsonUrl feature - issueTrackerUrl and links to GitHub repos are also supported by this feature
        displayUrl = 'https://curseforge.com/minecraft/mc-mods/spammycombat'
        // updateJsonUrl = 'https://forge.curseupdate.com/623297/spammycombat'

        credits = buildProperties['credits']
        displayTest = DisplayTest.MATCH_VERSION

        entrypoints {
            main "net.fabricmc.example.ExampleMod"
            main {
                adapter = "kotlin"
                value = "hello.kotlin.World"
            }
            client "net.fabricmc.example.ExampleModClient"
            rei = "net.fabricmc.example.ReiPlugin"
            entrypoint('fabric:datagen') {
                value = "net.fabricmc.example.ExampleModData"
            }
        }

        dependencies {
            minecraft = '[1.19.3]'

            onForge {
                forge = "[44,)"
                mod {
                    modId = 'patchouli'
                    versionRange = '[1.1,)'
                    ordering = DependencyOrdering.AFTER
                    side = DependencySide.BOTH
                }
            }

            mod('exampledep') {
                versionRange = '[1,)'
                side = DependencySide.CLIENT
                type = 'optional'
            }

            exampledep2 = '[1,)'
        }

        features {
            openGLVersion = '[3.2,)'
            javaVersion = '[17,)' // for testing features that mods.groovy doesn't recognise yet
        }
    }

    unrecognisedByFrontend {
        hello = 'world'
        x = 25

        nest {
            asDeepAsYouWant {
                one {
                    two {
                        three {
                            four = 4
                        }
                    }
                }
            }
        }
    }
}
Map builtMap = mdg.core.build() // for viewing the built map that consumers will receive
println builtMap
return mdg
