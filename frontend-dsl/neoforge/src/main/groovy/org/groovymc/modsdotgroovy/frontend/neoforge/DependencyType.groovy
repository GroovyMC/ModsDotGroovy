package org.groovymc.modsdotgroovy.frontend.neoforge

import groovy.transform.CompileStatic
import org.groovymc.rootpackagetransformer.RootPackage

@CompileStatic
@RootPackage
enum DependencyType {
    REQUIRED,
    OPTIONAL,
    INCOMPATIBLE,
    DISCOURAGED
}