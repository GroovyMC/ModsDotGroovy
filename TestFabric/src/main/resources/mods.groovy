final mdg = ModsDotGroovyFrontendFabric.make {
    schemaVersion = 1
    id = "examplemod"
    name = "Example Mod"
    description = "This is an example description!"
    version = "1.0.0"
    provides = ["example_mod"]
    environment = Environment.ANY
    license = ["MIT", "CC0-1.0"]

    entrypoints {
        main "net.fabricmc.example.ExampleMod"
        main {
            adapter = "kotlin"
            value = "hello.kotlin.World"
        }
        client "net.fabricmc.example.ExampleModClient"
        rei = "net.fabricmc.example.ReiPlugin"
        entrypoint 'fabric:datagen', {
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

    mixins {
        mixin "modid.mixins.json"
        mixin "modid-client.mixins.json", Environment.CLIENT
        mixin {
            config = "modid-server.mixins.json"
            environment = Environment.SERVER
        }
    }

    depends {
        fabricloader = ">=0.14.21"
        minecraft = "~1.20.1"
        java = ">=17"
        it.'fabric-api' = "*"
    }

    recommends {
        example_foss_mod = "*"
    }

    suggests {
        nice_mod = "*"
        another_nice_mod = [">=0.5.0", '<0.7.8']
    }

    breaks {
        example_arr_mod = "*"
    }

    conflicts {
        naughty_mod = "*"
    }

    contact {
        email = "contact@example.com"
        irc = "irc://irc.esper.net:6667/example"
        homepage = "https://fabricmc.net/"
        issues = "https://github.com/FabricMC/fabric-example-mod/issues"
        sources = "https://github.com/FabricMC/fabric-example-mod"
        // Non-standard values
        discord = "https://discord.gg/example"
    }

    authors {
        person "Me!"
        person "You!", {
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
        person "Maty"
    }

    custom {
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
