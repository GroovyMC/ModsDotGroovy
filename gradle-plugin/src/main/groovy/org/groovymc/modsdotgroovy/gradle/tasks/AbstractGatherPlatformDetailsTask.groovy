package org.groovymc.modsdotgroovy.gradle.tasks

import groovy.json.JsonBuilder
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.jetbrains.annotations.Nullable

import javax.inject.Inject
import java.util.regex.Pattern

@CacheableTask
@CompileStatic
abstract class AbstractGatherPlatformDetailsTask extends DefaultTask {
    private static final Pattern DOT_PATTERN = Pattern.compile('\\.')

    private final Property<@Nullable String> minecraftVersion = objectFactory.property(String)
    private final Property<@Nullable String> platformVersion = objectFactory.property(String)
    private final RegularFileProperty outputFile = objectFactory.fileProperty()

    @Input
    abstract MapProperty<String, Object> getExtraProperties()

    @Input
    abstract MapProperty<String, Object> getBuildProperties()

    @Optional @Input
    Property<@Nullable String> getMinecraftVersion() {
        return minecraftVersion
    }

    @Optional @Input
    Property<@Nullable String> getPlatformVersion() {
        return platformVersion
    }

    @OutputFile
    RegularFileProperty getOutputFile() {
        return outputFile
    }

    @Inject
    protected ProjectLayout getProjectLayout() {
        throw new IllegalStateException('ProjectLayout not injected')
    }

    @Inject
    protected ObjectFactory getObjectFactory() {
        throw new IllegalStateException('ObjectFactory not injected')
    }

    void projectProperty(String name) {
        buildProperties.put(name, project.providers.gradleProperty(name))
    }

    void projectProperty(Provider<String> name) {
        buildProperties.putAll(project.providers.gradleProperty(name).<Map<String, Object>>map { it -> [(name): it] })
    }

    AbstractGatherPlatformDetailsTask() {
        outputFile.convention(projectLayout.buildDirectory.dir("generated/modsDotGroovy/${name.uncapitalize()}").map((Directory dir) -> dir.file('mdgPlatform.json')))
        extraProperties.convention([:])
    }

    void setMinecraftVersion(String version) {
        minecraftVersion.set(version)
    }

    void setPlatformVersion(String version) {
        platformVersion.set(version)
    }

    void setOutputFile(File file) {
        outputFile.set(file)
    }

    protected void writePlatformDetails(String minecraftVersion, @Nullable String platformVersion) {
        outputFile.get().asFile.withWriter { writer ->
            Map map = [:]
            if (minecraftVersion !== null) {
                map['minecraftVersion'] = minecraftVersion
                try {
                    map['minecraftVersionRange'] = "[${minecraftVersion},1.${(DOT_PATTERN.split(minecraftVersion, 3)[1] as int) + 1})".toString()
                } catch (RuntimeException ignored) {
                    // It wasn't the sort of version we were expecting, so we can't do any sort of range stuff to it
                }
            }
            if (platformVersion !== null) {
                map['platformVersion'] = platformVersion
            }
            map['buildProperties'] = getBuildProperties().get()
            map.putAll(getExtraProperties().get())
            writer.write(new JsonBuilder(map).toPrettyString())
        }
    }

    @TaskAction
    void run() throws IllegalStateException {
        this.writePlatformDetails(minecraftVersion.getOrNull(), platformVersion.getOrNull())
    }
}
