package org.groovymc.modsdotgroovy.gradle.tasks

import com.google.gson.GsonBuilder
import groovy.transform.CompileStatic
import org.gradle.api.tasks.CacheableTask
import org.groovymc.modsdotgroovy.gradle.tasks.AbstractConvertTask

@CacheableTask
@CompileStatic
abstract class ConvertToJson extends AbstractConvertTask {
    @Override
    protected String writeData(Map data) {
        final gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()
        return gson.toJson(data)
    }
}
