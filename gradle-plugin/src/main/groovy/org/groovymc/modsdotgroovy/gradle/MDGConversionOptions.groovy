package org.groovymc.modsdotgroovy.gradle

import groovy.transform.CompileStatic
import org.gradle.api.provider.SetProperty

@CompileStatic
abstract class MDGConversionOptions {
    abstract SetProperty<String> getEnvironmentBlacklist()

    MDGConversionOptions() {
        this.environmentBlacklist.convention(['pass', 'password', 'token', 'key', 'secret'])
    }
}
