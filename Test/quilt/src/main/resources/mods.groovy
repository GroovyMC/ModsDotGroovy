final mdg = QuiltModsDotGroovy.make {
    // todo: finish adapting this Fabric test file to Quilt
    schemaVersion = 1
    id = "examplemod"
    name = "Example Mod"
    version = "1.0.0"
    provides = ["example_mod"]

    accessWidener = "examplemod.accesswidener"

    metadata {
        license = ["MIT", "CC0-1.0"]

        description = "This is an example description!"

        contact {
            email = "contact@example.com"
            homepage = "https://fabricmc.net/"
            issues = "https://github.com/FabricMC/fabric-example-mod/issues"
            sources = "https://github.com/FabricMC/fabric-example-mod"
            // Non-standard values
            discord = "https://discord.gg/example"
        }

        icon {
            x16 = "small.png"
            x32 = "medium.png"
        }
        icon 64, "large.png"
    }

    minecraft {
        environment = Environment.ANY
    }

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

    jars {
        jar {
            file = "nested/vendor/dependency.jar"
        }
        jar "nested/vendor/another-dependency.jar"
    }

    languageAdapters {
        kotlin = "net.fabricmc.language.kotlin.KotlinAdapter"
    }

    depends {
        fabricloader = ">=0.14.21"
        minecraft = "~1.20.1"
        java = ">=17"
        it.'fabric-api' = "*"

        mod "another-mod", ">=1.5.0"
        mod "something-else", [">0.5", '<1.0']
        mod {
            modId = "rats"
            versionRange = "*"
        }
    }

    breaks {
        example_arr_mod = "*"
    }

    authors {
        person "Me!"
        person("You!") {
            email = "you@example.com"
        }
        person {
            name = "Someone Else"
            contact {
                email = "someone-else@example.com"
            }
        }
    }

    contributors {
        person "Su5eD"
    }

    custom {
        property "fabricMod:foo", "bar"
        it.'fabricMod:hello' = "world"
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
