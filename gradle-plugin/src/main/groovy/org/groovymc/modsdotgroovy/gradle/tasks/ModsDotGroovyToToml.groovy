package org.groovymc.modsdotgroovy.gradle.tasks

import com.moandjiezana.toml.TomlWriter
import groovy.transform.CompileStatic
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.CacheableTask

import javax.inject.Inject

@CacheableTask
@CompileStatic
abstract class ModsDotGroovyToToml extends AbstractMDGConvertTask {
    private static final TomlWriter TOML_WRITER = new TomlWriter()

    ModsDotGroovyToToml() {
        this.outputName.convention('mods.toml')
    }

    @Override
    protected String writeData(Map data) {
        return TOML_WRITER.write(data)
    }
}
