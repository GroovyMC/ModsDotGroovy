package org.groovymc.modsdotgroovy.gradle

import com.moandjiezana.toml.TomlWriter
import groovy.transform.CompileStatic
import org.groovymc.modsdotgroovy.core.Platform
import org.gradle.api.tasks.CacheableTask

@CacheableTask
@CompileStatic
abstract class ConvertToTomlTask extends AbstractConvertTask {
    @Override
    protected String getOutputName() {
        return 'mods.toml'
    }

    @Override
    protected void setupPlatformSpecificArguments() {
        final mcDependency = project.configurations.findByName('minecraft')?.getDependencies()?.find()
        if (mcDependency !== null) {
            final version = mcDependency.version.split('-')
            arg('minecraftVersion', version[0])
            arg('forgeVersion', version[1].split('_mapped_')[0])

            final mcSplit = version[0].split('\\.')
            if (mcSplit.length > 1) {
                try {
                    final int currentVersion = Integer.parseInt(mcSplit[1])
                    arg('minecraftVersionRange', "[${version[0]},1.${currentVersion + 1})")
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    protected String writeData(Map data) {
        final tomlWriter = new TomlWriter.Builder()
//                .indentValuesBy(2)
//                .indentTablesBy(4)
                .build()
        return tomlWriter.write(data)
    }

    @Override
    protected String getOutputDir() {
        return 'META-INF'
    }

    @Override
    protected Platform getPlatform() {
        return Platform.FORGE
    }
}
