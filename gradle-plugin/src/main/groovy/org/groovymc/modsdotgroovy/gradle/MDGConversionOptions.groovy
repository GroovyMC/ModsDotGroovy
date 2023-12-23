package org.groovymc.modsdotgroovy.gradle

import org.gradle.api.provider.SetProperty

abstract class MDGConversionOptions {
    abstract SetProperty<String> getEnvironmentBlacklist()

    MDGConversionOptions() {
        this.environmentBlacklist.convention(['pass', 'password', 'token', 'key', 'secret'])
    }
}
