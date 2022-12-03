package io.github.groovymc.modsdotgroovy.compat

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.spongepowered.asm.gradle.plugins.MixinExtension

@CompileStatic
final class MixinGradleSetup {
    static void setup(Project project, Object ext) {
        setupInternal(project, (MixinExtension) ext)
    }

    private static void setupInternal(Project project, MixinExtension extension) {

    }
}
