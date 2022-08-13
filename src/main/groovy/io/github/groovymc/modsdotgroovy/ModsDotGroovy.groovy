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

import com.moandjiezana.toml.Toml
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTreeElement
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.language.jvm.tasks.ProcessResources

import java.nio.file.Path

class ModsDotGroovy implements Plugin<Project> {
    public static final String CONFIGURATION_NAME = 'modsDotGroovy'

    @Override
    void apply(Project project) {
        final ext = project.extensions.create(MDTExtension.NAME, MDTExtension)
        final configuration = project.configurations.create(CONFIGURATION_NAME)
        project.getPlugins().apply('java')
        project.afterEvaluate {
            configuration.dependencies.add(project.dependencies.create(ext.mdtDsl()))
            project.configurations.getByName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME)
                .extendsFrom(configuration)

            final mainSrcSet = project.extensions.getByType(JavaPluginExtension)
                    .sourceSets.named('main').get()
            final mainSrcSets = mainSrcSet.getResources().srcDirs
            if (mainSrcSets.empty) {
                project.getTasks().create('modsDotGroovyToToml', DefaultTask) // dummy task
            } else {
                final modsToml = mainSrcSets
                    .stream().map { new File(it, 'META-INF/mods.groovy') }
                    .filter { it.exists() }.findFirst()
                    .orElseGet(() -> new File(mainSrcSets.find(), 'META-INF/mods.groovy'))
                final convertTask = project.getTasks().create('modsDotGroovyToToml', ConvertToTomlTask) {
                    it.getInput().set(modsToml)
                }
                project.tasks.named(JavaPlugin.PROCESS_RESOURCES_TASK_NAME, ProcessResources).configure {
                    exclude((FileTreeElement el) -> el.file == convertTask.input.get().asFile)
                    dependsOn(convertTask)
                    from(convertTask.output.get().asFile) {
                        into 'META-INF'
                    }
                }
            }
        }
    }

    static void main(String[] args) {
        final path = Path.of('C:\\Users\\rober\\Documents\\GitHub\\Groovylicious\\src\\main\\resources\\META-INF\\mods.toml')
        Toml toml = new Toml().read(path.toFile())
        final map = toml.toMap()
        println map
    }
}
