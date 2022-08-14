ModsDotGroovy.make {
    modLoader = 'gml'
    loaderVersion = '[1,'

    license = 'MIT'

    mod {
        modId = 'no'
        version = '1.190'

        authors = ['Matyrobbrt', 'Paint_Ninja']
        credits = "${buildProperties.someProperty}"

        customProperty = 'hello'

        dependencies {
            minecraft = 1.19..1.20 // equivalent to `minecraft = '[1.19,1.20)'`
            forge = '[43.0.0,)' // equivalent to `forge { versionRange = '[43.0.0,)' }`

            mod {
                modId = 'patchouli'
                versionRange = '[1.1,)'
                ordering = DependencyOrdering.AFTER
                side = DependencySide.BOTH
            }
        }
    }
}
