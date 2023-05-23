/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

package io.github.groovymc.modsdotgroovy

import com.google.gson.GsonBuilder

abstract class ConvertToFabricJsonTask extends AbstractConvertTask {
    @Override
    protected String getOutputName() {
        return 'fabric.mod.json'
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
        final fabricLoaderDependency = project.configurations.findByName('modImplementation')?.dependencies
                ?.find {it.name == 'fabric-loader' && it.group == 'net.fabricmc'}
        if (fabricLoaderDependency !== null) {
            arg('fabricLoaderVersion', fabricLoaderDependency.version)
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
    protected String getPlatform() {
        return 'fabric'
    }
}
