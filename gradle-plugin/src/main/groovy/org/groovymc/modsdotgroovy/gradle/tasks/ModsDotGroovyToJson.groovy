package org.groovymc.modsdotgroovy.gradle.tasks

import com.google.gson.GsonBuilder
import groovy.transform.CompileStatic
import org.gradle.api.tasks.CacheableTask

@CacheableTask
@CompileStatic
abstract class ModsDotGroovyToJson extends AbstractMDGConvertTask {
    @Override
    protected String writeData(Map data) {
        final gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()
        return gson.toJson(data)
    }
}
