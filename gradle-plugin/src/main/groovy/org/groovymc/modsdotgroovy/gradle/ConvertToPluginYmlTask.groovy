/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

package org.groovymc.modsdotgroovy.gradle

import groovy.transform.CompileStatic
import org.gradle.api.tasks.CacheableTask
import org.groovymc.modsdotgroovy.core.Platform
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

@CacheableTask
@CompileStatic
abstract class ConvertToPluginYmlTask extends AbstractConvertTask {
    @Override
    protected String getOutputName() {
        return 'plugin.yml'
    }

    @Override
    protected void setupPlatformSpecificArguments() {
        // todo
    }

    @Override
    protected String writeData(Map data) {
        final yamlWriter = new Yaml(new DumperOptions().tap {
            prettyFlow = true
        })
        return yamlWriter.dump(data)
    }

    @Override
    protected String getOutputDir() {
        return ''
    }

    @Override
    protected Platform getPlatform() {
        return Platform.SPIGOT
    }
}
