package org.groovymc.modsdotgroovy.frontend.multiplatform

import groovy.transform.CompileStatic
import org.groovymc.rootpackagetransformer.RootPackage

@CompileStatic
@RootPackage
enum DependencySide {
    CLIENT, SERVER, BOTH

    DependencySide() {}
}
