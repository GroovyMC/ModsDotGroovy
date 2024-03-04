final mdg = NeoForgeModsDotGroovy.make {
    modLoader = 'javafml'
    loaderVersion = rawVersionRange('[1,)')

    license = 'All Rights Reserved'
    issueTrackerUrl = 'https://example.com/issues'

    mod {
        modId = 'examplemod'
        version = '1.0.0'
        displayName = 'Example Mod'
        description = 'This is an example mod.'
        authors = ['Example Mod Author', 'Another Author']
        logoFile = 'logo.png'

        // for testing the inferred updateJsonUrl feature - issueTrackerUrl and links to GitHub repos are also supported by this feature
        displayUrl = 'https://curseforge.com/minecraft/mc-mods/spammycombat'
        // updateJsonUrl = 'https://forge.curseupdate.com/623297/spammycombat'

        credits = buildProperties['credits']
        displayTest = DisplayTest.MATCH_VERSION
        dependencies {
            neoforge = "[47.1.3,)"
            minecraft = '[1.20.1]'

            mod {
                modId = 'patchouli'
                versionRange = v('>=1.1') & v('<2.0')
                ordering = DependencyOrdering.AFTER
                side = DependencySide.BOTH
            }

            optional {
                modId = 'optionaldep'
                versionRange = v('<1.1') | v('>=2.0')
            }

            mod('exampledep') {
                versionRange = ">=${libs.libraries.example_exampledep.version}"
                side = DependencySide.CLIENT
                type = DependencyType.OPTIONAL
            }

            exampledep2 = '[1,)'
            exampledep3 = "[${libs.versions['exampledep3']},)" // to test reading from version catalogues
        }

        features {
            openGLVersion = '[3.2,)'
            javaVersion = '[17,)' // for testing features that mods.groovy doesn't recognise yet
        }
    }

    mixins {
        mixin 'examplemod.mixins.json'
    }

    accessTransformers {
        accessTransformer 'accesstransformer.cfg'
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
