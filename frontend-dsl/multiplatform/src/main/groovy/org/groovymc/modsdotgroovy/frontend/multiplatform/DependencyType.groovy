package org.groovymc.modsdotgroovy.frontend.multiplatform

import groovy.transform.CompileStatic
import org.groovymc.rootpackagetransformer.RootPackage

@CompileStatic
@RootPackage
enum DependencyType {
    REQUIRED,
    OPTIONAL,
    INCOMPATIBLE,
    DISCOURAGED

    @Override
    String toString() {
        return name().toLowerCase(Locale.ROOT)
    }
}