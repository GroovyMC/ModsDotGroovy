//file:noinspection GrPackage
ModsDotGroovy.make {
    modLoader = 'gml'
    loaderVersion = '[1.0,'

    license = 'MIT'

    mod {
        modId = 'no'
        version = '1.190'
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
                ordering = modsdotgroovy.DependencyOrdering.AFTER
                side = modsdotgroovy.DependencySide.BOTH
            }
        }
    }
}