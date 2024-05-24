package org.groovymc.modsdotgroovy.frontend.forge

import groovy.transform.CompileStatic
import org.groovymc.rootpackagetransformer.RootPackage

@CompileStatic
@RootPackage
enum DependencySide {
    CLIENT, SERVER, BOTH

    DependencySide() {}
}
