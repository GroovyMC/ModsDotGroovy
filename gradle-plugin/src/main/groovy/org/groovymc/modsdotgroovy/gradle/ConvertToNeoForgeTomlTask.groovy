package org.groovymc.modsdotgroovy.gradle

import groovy.transform.CompileStatic
import org.gradle.api.tasks.CacheableTask
import org.groovymc.modsdotgroovy.core.Platform

@CacheableTask
@CompileStatic
abstract class ConvertToNeoForgeTomlTask extends ConvertToTomlTask {
    @Override
    protected Platform getPlatform() {
        return Platform.NEOFORGE
    }
}
