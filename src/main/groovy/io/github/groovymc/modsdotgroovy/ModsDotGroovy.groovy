/*
 * MIT License
 *
 * Copyright (c) 2022 GroovyMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.groovymc.modsdotgroovy

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.file.FileTreeElement
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.language.jvm.tasks.ProcessResources

import javax.annotation.Nullable

class ModsDotGroovy implements Plugin<Project> {
    public static final String CONFIGURATION_NAME = 'modsDotGroovy'

    @Override
    void apply(Project project) {
        final ext = project.extensions.create(MDGExtension.NAME, MDGExtension)
        final configuration = project.configurations.create(CONFIGURATION_NAME)

        project.getPlugins().apply('java')

        project.afterEvaluate {
            project.repositories.maven { MavenArtifactRepository repo ->
                repo.name = 'Modding Inquisition Releases'
                repo.url = 'https://maven.moddinginquisition.org/releases'
            }

            configuration.dependencies.add(project.dependencies.create(ext.mdgDsl()))

            if (ext.automaticConfiguration.get()) {
                final List<MDGExtension.Platform> platforms = ext.platforms.get()
                for (MDGExtension.Platform platform : platforms.unique(false)) {
                    if (platform != MDGExtension.Platform.MULTILOADER) {
                        final srcSets = project.extensions.getByType(JavaPluginExtension).sourceSets
                        final srcSet = ext.source.isPresent()?ext.source.get():browse(srcSets) { new File(it, 'mods.groovy')}
                                .map {it.sourceSet}
                                .orElseGet(() -> srcSets.named('main').get())

                        final modsGroovy = new FileWithSourceSet(srcSet, new File(srcSet.resources.srcDirs.find(), 'mods.groovy'))

                        project.configurations.getByName(modsGroovy.sourceSet.compileOnlyConfigurationName)
                                .extendsFrom(configuration)

                        switch (platform) {
                            case MDGExtension.Platform.FORGE:
                                makeAndAppendForgeTask(modsGroovy, project).with {
                                    arguments.set(ext.arguments.get())
                                    catalogs.set(ext.catalogs.get())
                                }
                                break
                            case MDGExtension.Platform.QUILT:
                                makeAndAppendQuiltTask(modsGroovy, project).with {
                                    arguments.set(ext.arguments.get())
                                    catalogs.set(ext.catalogs.get())
                                }
                        }
                    } else {
                        final common = ext.multiloader.getOrNull()?.common ?: project.subprojects.find { it.name.toLowerCase(Locale.ROOT) == 'common' }
                        final quilt = ext.multiloader.isPresent() ? ext.multiloader.get().quilt : [project.subprojects.find { it.name.toLowerCase(Locale.ROOT) == 'quilt' }]
                        final forge = ext.multiloader.isPresent() ? ext.multiloader.get().forge : [project.subprojects.find { it.name.toLowerCase(Locale.ROOT) == 'forge' }]

                        if (common === null)
                            throw new IllegalArgumentException(
                                    "Specified platform 'multiloader' but missing common subproject")
                        final commonSrcSets = common.extensions.getByType(JavaPluginExtension).sourceSets
                        final commonSrcSet = ext.source.isPresent() ? ext.source.get() : browse(commonSrcSets) { new File(it, 'mods.groovy') }
                                .map { it.sourceSet }
                                .orElseGet(() -> commonSrcSets.named('main').get())

                        final modsGroovy = new FileWithSourceSet(commonSrcSet, new File(commonSrcSet.resources.srcDirs.find(), 'mods.groovy'))

                        final commonConfiguration = common.configurations.findByName(CONFIGURATION_NAME)?:common.configurations.create(CONFIGURATION_NAME)
                        commonConfiguration.dependencies.add(common.dependencies.create(ext.mdgDsl()))
                        common.configurations.getByName(modsGroovy.sourceSet.compileOnlyConfigurationName)
                                .extendsFrom(configuration)
                        common.configurations.getByName(modsGroovy.sourceSet.compileOnlyConfigurationName)
                                .extendsFrom(commonConfiguration)


                        forge.each {
                            makeAndAppendForgeTask(modsGroovy, it).with {
                                dslConfiguration.set(commonConfiguration)
                                arguments.set(ext.arguments.get())
                                catalogs.set(ext.catalogs.get())
                            }
                        }
                        quilt.each {
                            makeAndAppendQuiltTask(modsGroovy, it).with{
                                dslConfiguration.set(commonConfiguration)
                                arguments.set(ext.arguments.get())
                                catalogs.set(ext.catalogs.get())
                            }
                        }
                    }
                }
            }
        }
    }

    static ConvertToTomlTask makeAndAppendForgeTask(FileWithSourceSet modsGroovy, Project project) {
        final convertTask = project.getTasks().create('modsDotGroovyToToml', ConvertToTomlTask) {
            it.getInput().set(modsGroovy.file)
        }
        project.tasks.named(modsGroovy.sourceSet.processResourcesTaskName, ProcessResources).configure {
            exclude((FileTreeElement el) -> el.file == convertTask.input.get().asFile)
            dependsOn(convertTask)
            from(convertTask.output.get().asFile) {
                into 'META-INF'
            }
        }
        return convertTask
    }

    static ConvertToQuiltJsonTask makeAndAppendQuiltTask(FileWithSourceSet modsGroovy, Project project) {
        final convertTask = project.getTasks().create('modsDotGroovyToQuiltJson', ConvertToQuiltJsonTask) {
            it.getInput().set(modsGroovy.file)
        }
        project.tasks.named(modsGroovy.sourceSet.processResourcesTaskName, ProcessResources).configure {
            exclude((FileTreeElement el) -> el.file == convertTask.input.get().asFile)
            dependsOn(convertTask)
            from(convertTask.output.get().asFile) {
                into ''
            }
        }
        return convertTask
    }

    static Optional<FileWithSourceSet> browse(final Collection<SourceSet> sourceSet, @ClosureParams(value = SimpleType, options = 'java.io.File') Closure<File> finder) {
        sourceSet.stream()
                .sorted { SourceSet it1, SourceSet it2 ->
                    if (it1.name == 'main') return 1
                    if (it2.name == 'main') return -1
                    return Comparator.<String>naturalOrder().compare(it1.name, it2.name)
                }
                .map {it -> new FileWithSourceSet(it, browse(it, finder)) }
                .filter { it.file !== null }
                .findFirst()
    }

    @Nullable
    static File browse(final SourceSet sourceSet, @ClosureParams(value = SimpleType, options = 'java.io.File') Closure<File> finder) {
        sourceSet.resources.srcDirs.stream()
                .map {finder(it) }
                .filter { it.exists() }
                .findFirst().orElse(null)
    }

    @Canonical
    @CompileStatic
    static class FileWithSourceSet {
        SourceSet sourceSet
        File file
    }
}
