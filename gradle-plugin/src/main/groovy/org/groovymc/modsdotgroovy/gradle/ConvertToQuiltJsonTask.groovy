package org.groovymc.modsdotgroovy.gradle

import com.google.gson.GsonBuilder
import groovy.transform.CompileStatic
import org.groovymc.modsdotgroovy.core.Platform
import org.gradle.api.tasks.CacheableTask

@CacheableTask
@CompileStatic
abstract class ConvertToQuiltJsonTask extends AbstractConvertTask {
    @Override
    protected String getOutputName() {
        return 'quilt.mod.json'
    }

    @Override
    protected void setupPlatformSpecificArguments() {
        final mcDependency = project.configurations.findByName('minecraft')?.getDependencies()?.find()
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
        final quiltLoaderDependency = project.configurations.findByName('modImplementation')?.dependencies
                ?.find {it.name == 'quilt-loader' && it.group == 'org.quiltmc'}
        if (quiltLoaderDependency !== null) {
            arg('quiltLoaderVersion', quiltLoaderDependency.version)
        }
    }

    @Override
    protected String writeData(Map data) {
        final gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()
        return gson.toJson(data)
    }

    @Override
    protected String getOutputDir() {
        return ''
    }

    @Override
    protected Platform getPlatform() {
        return Platform.QUILT
    }
}
