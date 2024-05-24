package org.groovymc.modsdotgroovy.frontend.neoforge

import groovy.transform.CompileStatic
import org.groovymc.rootpackagetransformer.RootPackage

@CompileStatic
@RootPackage
enum DependencyOrdering {
    BEFORE, AFTER, NONE

    DependencyOrdering() {}
}
