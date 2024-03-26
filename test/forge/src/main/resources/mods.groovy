final mdg = ForgeModsDotGroovy.make {
    modLoader = 'gml'
    loaderVersion = rawVersionRange('[1,)')

    license = 'All Rights Reserved'
    issueTrackerUrl = 'https://github.com/zlepper/itlt'

    clientSideOnly = true

    mod {
        modId = 'examplemod'
        version = '1.0.0'
        displayName = 'Example Mod'
        description = 'This is an example mod.'
        //authors = ['Alice', 'Bob']
        author = 'Example Mod Author'
        logoFile = 'logo.png'

        // for testing the inferred updateJsonUrl feature - issueTrackerUrl and links to GitHub repos are also supported by this feature
        displayUrl = 'https://curseforge.com/minecraft/mc-mods/spammycombat'
        // updateJsonUrl = 'https://forge.curseupdate.com/623297/spammycombat'

        credits = buildProperties.credits
        //displayTest = DisplayTest.MATCH_VERSION
        dependencies {
            // TODO: can/should we get IDE support for this? (or expose it only through the environment info?)
            forge = ">=${platformVersion}"
            minecraft = '[1.19.3]'

            mod {
                modId = 'patchouli'
                versionRange = '>=1.1'
                ordering = DependencyOrdering.AFTER
                side = DependencySide.BOTH
            }

            mod('exampledep') {
                versionRange = '1.*'
                side = DependencySide.CLIENT
                mandatory = false
            }

            exampledep2 = '[1,)'
            //exampledep3 = "[${libs.versions['exampledep3']},)" // to test reading from version catalogues
        }

        features {
            openGLVersion = '[3.2,)'
            javaVersion = '[17,)'
            someSpecialFeatureVersion = '[1.2.3,)' // for testing features that mods.groovy doesn't recognise yet
        }
    }

    unrecognisedByFrontend {
        hello = 'world'
        x = 24

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
