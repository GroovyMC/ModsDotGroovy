package org.groovymc.modsdotgroovy.frontend.multiplatform

import groovy.transform.CompileStatic
import org.groovymc.rootpackagetransformer.RootPackage

@CompileStatic
@RootPackage
enum DependencyOrdering {
    BEFORE, AFTER, NONE

    DependencyOrdering() {}
}
