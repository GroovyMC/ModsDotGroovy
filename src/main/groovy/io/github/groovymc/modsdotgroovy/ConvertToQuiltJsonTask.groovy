package io.github.groovymc.modsdotgroovy

import com.google.gson.GsonBuilder
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile

abstract class ConvertToQuiltJsonTask extends AbstractConvertTask {
    @InputFile
    abstract RegularFileProperty getInput()
    @Optional
    @OutputFile
    abstract RegularFileProperty getOutput()
    @Optional
    @InputFile
    abstract RegularFileProperty getDslLocation()

    @Input
    @Optional
    abstract MapProperty<String, Object> getArguments()

    @Override
    protected String getOutputName() {
        return 'quilt.mod.json'
    }

    @Override
    protected void setupPlatformSpecificArguments() {
        final mcDependency = project.configurations.findByName('minecraft')
                ?.getDependencies()?.find()
        if (mcDependency !== null) {
            arg('minecraftVersion', mcDependency.version)

            final mcSplit = mcDependency.version.split('\\.')
            if (mcSplit.length > 1) {
                try {
                    final currentVersion = Integer.parseInt(mcSplit[1])
                    arg('minecraftVersionRange', "[${mcDependency.version},1.${currentVersion + 1})")
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    protected String writeData(Map data) {
        final gson = new GsonBuilder()
                .setPrettyPrinting()
                .create()
        return gson.toJson(data)
    }

    @Override
    protected String getOutputDir() {
        return ''
    }

    @Override
    protected String getPlatform() {
        return 'quilt'
    }
}
