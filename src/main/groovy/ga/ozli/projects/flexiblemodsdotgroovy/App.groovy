package ga.ozli.projects.flexiblemodsdotgroovy

class App {
    static void main(final String[] args) {
        println "Groovy v${GroovySystem.version}"

        final mdg = ModsDotGroovy.make {
//            modLoader = 42
//
//            onForge {
//                println 'This is a Forge project'
//            }
            modLoader = 'javafml'

            mods {
                mod {
                    license = 'MIT'

                    modId = 'examplemod'
                    displayName = 'Example Mod'
                    version = '1.0.0'
                }
            }
        }
        println mdg.data
    }
}
