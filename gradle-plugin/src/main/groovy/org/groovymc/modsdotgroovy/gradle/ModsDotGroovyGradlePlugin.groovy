package org.groovymc.modsdotgroovy.gradle

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.services.BuildServiceSpec
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JavaToolchainService
import org.groovymc.modsdotgroovy.gradle.internal.ConvertService

import javax.inject.Inject

@CompileStatic
abstract class ModsDotGroovyGradlePlugin implements Plugin<Project> {
    private static final String EXTENSION_NAME = 'modsDotGroovy'
    public static final String VERSION = ModsDotGroovyGradlePlugin.class.package.implementationVersion

    @Inject
    protected abstract JavaToolchainService getToolchainService()

    @Inject
    ModsDotGroovyGradlePlugin() {}

    @Override
    void apply(Project project) {
        // setup required plugins
        project.plugins.apply('java')

        JavaPluginExtension javaPluginExtension = project.extensions.getByType(JavaPluginExtension)
        SourceSetContainer sourceSets = javaPluginExtension.sourceSets

        Configuration bootstrapClasspath = project.configurations.create('modsDotGroovyBootstrapClasspath')
        project.dependencies.add('modsDotGroovyBootstrapClasspath', project.dependencies.create('org.groovymc.modsdotgroovy:bootstrap'))
        project.getGradle().getSharedServices().registerIfAbsent(ConvertService.name, ConvertService) { BuildServiceSpec<ConvertService.Parameters> it ->
            it.parameters.classpath.from(bootstrapClasspath)
            it.parameters.threads.set(project.providers.systemProperty(ConvertService.THREAD_COUNT_PROPERTY).orElse("4"))
        }

        // set up the core extension for the 'main' source set
        //noinspection ConfigurationAvoidance
        var mainExtension = project.extensions.create(EXTENSION_NAME, MDGExtension, sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME), project)
        mainExtension.setupDsl.convention(true)
        mainExtension.setupPlugins.convention(true)
        mainExtension.setupTasks.convention(true)

        sourceSets.configureEach {
            if (SourceSet.isMain(it)) {
                var extension = mainExtension
                it.extensions.add(MDGExtension, EXTENSION_NAME, extension)
            } else {
                it.extensions.create(EXTENSION_NAME, MDGExtension, it, project)
            }
        }

        project.afterEvaluate {
            sourceSets.each {sourceSet ->
                MDGExtension extension = sourceSet.extensions.getByType(MDGExtension)
                extension.apply()
            }
        }
    }
}
