final mdg = QuiltModsDotGroovy.make {
    schemaVersion = 1

    quiltLoader {
        group = 'org.quiltmc'
        id = 'examplemod'

        provides {
            api {
                id = 'super_awesome_lib'
                version = '1.0.0'
            }
            mod 'flamingo'
        }

        version = '1.0.0'

        entrypoints {
            main 'org.quiltmc.examplemod.impl.ExampleMod'
            main 'org.quiltmc.examplemod.impl.ExampleModNetworking'
            main {
                adapter = "kotlin"
                value = "hello.kotlin.World"
            }
            client 'org.quiltmc.examplemod.impl.client.ExampleModClient'
            rei = 'org.quiltmc.examplemod.impl.ReiPlugin'
            entrypoint('fabric:datagen') {
                value = "net.fabricmc.example.ExampleModData"
            }
        }

        depends {
            quilt_networking_api = v('*')
            quilt_rendering_api = '*'

            mod('modmenu') {
                environment = 'client'
            }

            mod 'another-mod', '>=1.5.0'
            mod('something-else', v('>0.5') & v('<1.0'))

            mod {
                id = 'rats'
                version = '*'
            }
        }

        breaks {
            example_arr_mod = '*'

            mod {
                id = 'sodium'
                reason = 'Sodium does not implement the Quilt Rendering API.'
                unless = 'indium'
            }

            mod {
                id = 'some_random_library'
                versions = '1.23.456'
            }

            mod {
                id = 'some_random_library'
                reason = 'Stable API required'
                version = '<1.0.0'
            }

            mod {
                id = 'some_random_library'
                reason = 'Contain game-breaking bugs'
                versions = v('1.5.3') | v('1.2.7') | v('1.8.3')
            }
        }

        metadata {
            name = 'Quilt Example Mod'
            description = 'An example mod for the Quilt ecosystem.'

            contributors {
                contributor {
                    name = 'Haven King'
                    role = 'Developer'
                }
            }

            contact {
                email = 'contact@example.com'
                homepage = 'https://quiltmc.org/'
                issues = 'https://github.com/QuiltMC/quilt-template-mod/issues'
                sources = 'https://github.com/QuiltMC/quilt-template-mod'
                // Non-standard values
                discord = 'https://discord.gg/example'
            }

            license = ['MIT', 'CC0-1.0']

            icon {
                x16 = 'small.png'
                x32 = 'medium.png'
            }
            icon 64, 'large.png'
        }

        jars {
            jar {
                file = "nested/vendor/dependency.jar"
            }
            jar "nested/vendor/another-dependency.jar"
        }

        languageAdapters {
            kotlin = "net.fabricmc.language.kotlin.KotlinAdapter"
        }
    }

    accessWidener = 'examplemod.accesswidener'

    minecraft {
        environment = Environment.ANY
    }

    unrecognisedByFrontend {
        it.'fabricMod:hello' = 'world'
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
