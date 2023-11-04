package org.groovymc.modsdotgroovy.gradle.tasks

import groovy.transform.CompileStatic
import org.gradle.api.tasks.CacheableTask
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

@CacheableTask
@CompileStatic
abstract class ConvertToYml extends AbstractConvertTask {
    @Override
    protected String writeData(Map data) {
        final yamlWriter = new Yaml(new DumperOptions().tap {
            prettyFlow = true
        })
        return yamlWriter.dump(data)
    }
}
