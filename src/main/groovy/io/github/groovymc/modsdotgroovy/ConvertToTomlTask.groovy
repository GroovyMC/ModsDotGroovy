/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

package io.github.groovymc.modsdotgroovy

import groovy.transform.CompileStatic

@CompileStatic
abstract class ConvertToTomlTask extends AbstractConvertTask {

    @Override
    protected void registerStrategies() {
        register('root', new Strategy(
                'mods.toml', 'META-INF', TOML_WRITER
        ))
        register('packMcMeta', new Strategy(
                'pack.mcmeta', '', JSON_WRITER
        ))
        register('mixinConfig', new Strategy(
                'mixins.json', '', JSON_WRITER
        ))
    }

    @Override
    protected void setupPlatformSpecificArguments() {
        final mcDependency = project.configurations.findByName('minecraft')
                ?.getDependencies()?.find()
        if (mcDependency !== null) {
            final version = mcDependency.version.split('-')
            arg('minecraftVersion', version[0])
            arg('forgeVersion', version[1].split('_mapped_')[0])

            final mcSplit = version[0].split('\\.')
            if (mcSplit.length > 1) {
                try {
                    final currentVersion = Integer.parseInt(mcSplit[1])
                    arg('minecraftVersionRange', "[${version[0]},1.${currentVersion + 1})")
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    protected String getPlatform() {
        return 'forge'
    }
}
