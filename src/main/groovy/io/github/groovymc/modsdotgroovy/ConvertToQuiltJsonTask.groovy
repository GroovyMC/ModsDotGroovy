package io.github.groovymc.modsdotgroovy

import com.google.gson.GsonBuilder

abstract class ConvertToQuiltJsonTask extends AbstractConvertTask {
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
        final quiltLoaderDependency = project.configurations.findByName('modImplementation').dependencies
                .find {it.name == "quilt-loader" && it.group == "org.quiltmc"}
        if (quiltLoaderDependency !== null) {
            arg('quiltLoaderVersion', quiltLoaderDependency.version)
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
