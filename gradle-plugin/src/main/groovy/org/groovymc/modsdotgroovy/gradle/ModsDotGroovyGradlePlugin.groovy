package org.groovymc.modsdotgroovy.gradle

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.services.BuildServiceSpec
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.groovymc.modsdotgroovy.gradle.internal.ConvertService

import javax.inject.Inject

@CompileStatic
abstract class ModsDotGroovyGradlePlugin implements Plugin<Project> {
    private static final String EXTENSION_NAME = 'modsDotGroovy'
    public static final String VERSION = ModsDotGroovyGradlePlugin.class.package.implementationVersion

    @Inject
    ModsDotGroovyGradlePlugin() {}

    @Override
    void apply(Project project) {
        // setup required plugins
        project.plugins.apply('java')

        JavaPluginExtension javaPluginExtension = project.extensions.getByType(JavaPluginExtension)
        SourceSetContainer sourceSets = javaPluginExtension.sourceSets

        project.configurations.register('modsDotGroovyRunnerClasspath')
        project.dependencies.add('modsDotGroovyRunnerClasspath', project.dependencies.create('org.groovymc.modsdotgroovy:runner'))
        project.getGradle().getSharedServices().registerIfAbsent(ConvertService.name, ConvertService) { BuildServiceSpec<ConvertService.Parameters> it ->
            it.parameters.threads.set(project.providers.systemProperty(ConvertService.THREAD_COUNT_PROPERTY).orElse("4"))
            it.parameters.logLevel.set(closestLogLevel(project.gradle.startParameter.logLevel))
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

    static String closestLogLevel(LogLevel logLevel) {
        switch (logLevel) {
            case LogLevel.DEBUG:
                return 'DEBUG'
            case LogLevel.INFO:
                return 'INFO'
            case LogLevel.LIFECYCLE:
                return 'WARN'
            case LogLevel.WARN:
                return 'WARN'
            case LogLevel.QUIET:
                return 'ERROR'
            case LogLevel.ERROR:
                return 'ERROR'
        }
    }
}
