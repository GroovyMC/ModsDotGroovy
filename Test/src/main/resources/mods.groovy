ModsDotGroovy.make {
    modLoader = 'gml'
    loaderVersion = '[1,'

    license = 'MIT'

    mod {
        modId = 'no'
        version = '1.190'

        credits = "${buildProperties.someProperty}"

        customProperty = 'hello'

        dependencies {
            minecraft {
                versionRange = '[1.19,1.20)'
            }
            forge {
                versionRange = '[43.0.0,)'
            }

            mod {
                modId = 'patchouli'
                versionRange = '[1.1,)'
                ordering = DependencyOrdering.AFTER
                side = DependencySide.BOTH
            }
        }
    }
}