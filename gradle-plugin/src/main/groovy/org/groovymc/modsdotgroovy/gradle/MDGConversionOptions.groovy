package org.groovymc.modsdotgroovy.gradle

import groovy.transform.CompileStatic
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input

@CompileStatic
abstract class MDGConversionOptions {
    @Input
    abstract SetProperty<String> getEnvironmentBlacklist()

    MDGConversionOptions() {
        this.environmentBlacklist.convention(['pass', 'password', 'token', 'key', 'secret'])
        this.environmentBlacklist.finalizeValueOnRead()
    }
}
