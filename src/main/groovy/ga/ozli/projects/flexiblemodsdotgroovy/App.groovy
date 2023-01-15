package ga.ozli.projects.flexiblemodsdotgroovy

class App {
    static void main(final String[] args) {
        println "Groovy v${GroovySystem.version}"

        final mdg = ModsDotGroovy.make {
            modLoader = 'aplp'
            mods {
                modInfo {
                    modId = 'examplemod'
//                    modLoader = 'javafml'
                }
                insideModsBuilder = true
            }
        }
        println mdg.core.build()
    }
}
