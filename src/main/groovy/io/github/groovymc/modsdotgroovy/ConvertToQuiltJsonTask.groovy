/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

package io.github.groovymc.modsdotgroovy

import groovy.transform.CompileStatic

@CompileStatic
abstract class ConvertToQuiltJsonTask extends AbstractConvertTask {

    @Override
    protected void registerStrategies() {
        register('root', new Strategy(
                'quilt.mod.json', '', JSON_WRITER
        ))

        mixinConfigs.get().forEach { String config, String refMap ->
            register("mixinConfig_$config", new Strategy(
                    config, '', JSON_WRITER
            ))
        }
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
        final quiltLoaderDependency = project.configurations.findByName('modImplementation')?.dependencies
                ?.find {it.name == 'quilt-loader' && it.group == 'org.quiltmc'}
        if (quiltLoaderDependency !== null) {
            arg('quiltLoaderVersion', quiltLoaderDependency.version)
        }
    }

    @Override
    protected String getPlatform() {
        return 'quilt'
    }
}
