package org.groovymc.modsdotgroovy.gradle

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurablePublishArtifact
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.jvm.tasks.ProcessResources
import org.groovymc.modsdotgroovy.types.core.Platform
import org.groovymc.modsdotgroovy.types.internal.FlexVerComparator
import org.groovymc.modsdotgroovy.gradle.tasks.*

import javax.inject.Inject

@CompileStatic
abstract class MDGExtension {
    private static final String EXPOSE_SOURCE_SET = 'shareModsDotGroovy'
    private static final String EXPOSE_GATHERED = 'shareModsDotGroovyGather'
    private static final String CONSUME_SOURCE_SET = 'consumeModsDotGroovy'
    private static final String CONSUME_GATHERED = 'consumeModsDotGroovyGather'
    private static final String DEFAULT_MDG = 'mods.groovy'
    private static final String TASK_GROUP = 'modsdotgroovy'

    private static final String CONFIGURATION_NAME_ROOT = 'mdgRuntime'
    private static final String CONFIGURATION_NAME_PLUGIN = 'mdgPlugin'
    private static final String CONFIGURATION_NAME_FRONTEND = 'mdgFrontend'

    private static final String MDG_MAVEN_GROUP = 'org.groovymc.modsdotgroovy'
    private static final String MDG_FRONTEND_GROUP = MDG_MAVEN_GROUP + '.frontend-dsl'
    private static final String MDG_PLUGIN_GROUP = MDG_MAVEN_GROUP + '.stock-plugins'

    private static final Map<CharSequence, CharSequence> CATALOG_ID_REPLACEMENTS = Map.<CharSequence, CharSequence>of('-', '_', '.', '_')

    final Property<Boolean> setupDsl
    final Property<Boolean> setupPlugins
    final Property<Boolean> setupTasks
    final Property<Boolean> setupGatherTask
    final Property<Boolean> inferGather
    final ListProperty<Platform> platforms
    final Property<FileCollection> modsDotGroovyFile
    final Multiplatform multiplatform
    final ListProperty<String> catalogs
    final ListProperty<Action<AbstractGatherPlatformDetailsTask>> gatherActions
    final ListProperty<Action<AbstractMDGConvertTask>> convertActions

    private final Property<Boolean> multiplatformFlag
    private final SourceSet sourceSet

    private final Project project

    private boolean applied

    @Inject
    MDGExtension(SourceSet sourceSet, Project project) {
        this.project = project
        this.sourceSet = sourceSet

        this.setupDsl = project.objects.property(Boolean)
        this.setupPlugins = project.objects.property(Boolean)
        this.setupTasks = project.objects.property(Boolean)
        this.setupGatherTask = project.objects.property(Boolean)
        this.inferGather = project.objects.property(Boolean)
        this.platforms = project.objects.listProperty(Platform)
        this.modsDotGroovyFile = project.objects.property(FileCollection)
        this.catalogs = project.objects.listProperty(String)
        this.gatherActions = project.objects.listProperty(Action.class as Class<Action<AbstractGatherPlatformDetailsTask>>)
        this.convertActions = project.objects.listProperty(Action.class as Class<Action<AbstractMDGConvertTask>>)

        this.platforms.convention(inferPlatforms(project))

        this.modsDotGroovyFile.convention(sourceSet.resources.matching {
            include DEFAULT_MDG
        })

        this.multiplatformFlag = project.objects.property(Boolean)
        this.multiplatformFlag.convention(false)

        this.multiplatform = new Multiplatform()

        this.setupPlugins.convention(false)
        this.setupTasks.convention(false)
        this.setupGatherTask.convention(false)
        this.setupDsl.convention(false)
        this.inferGather.convention(true)
        this.catalogs.convention(['libs'])

        this.setupDsl.finalizeValueOnRead()
        this.setupPlugins.finalizeValueOnRead()
        this.setupTasks.finalizeValueOnRead()
        this.setupGatherTask.finalizeValueOnRead()
        this.inferGather.finalizeValueOnRead()
        this.platforms.finalizeValueOnRead()
        this.modsDotGroovyFile.finalizeValueOnRead()
        this.multiplatformFlag.finalizeValueOnRead()
        this.catalogs.finalizeValueOnRead()
        this.gatherActions.finalizeValueOnRead()
        this.convertActions.finalizeValueOnRead()
    }

    private static Provider<List<Platform>> inferPlatforms(Project project) {
        return project.<List<Platform>>provider {

            boolean loomPresent = isLoomProbablyPresent(project)
            String archLoomPlatform = project.findProperty('loom.platform')
            if (loomPresent && archLoomPlatform !== null) {
                return List.of(Platform.fromRegistry(archLoomPlatform))
            }

            if (project.plugins.findPlugin('net.minecraftforge.gradle')) return List.of(Platform.FORGE)
            else if (project.plugins.findPlugin('net.neoforged.gradle.userdev')) return List.of(Platform.NEOFORGE)
            else if (project.plugins.findPlugin('fabric-loom')) return List.of(Platform.FABRIC)
            else if (project.plugins.findPlugin('org.quiltmc.loom')) return List.of(Platform.QUILT)
            else if (loomPresent) return List.of(Platform.FABRIC)

            return List.of()
        }
    }

    private static boolean isLoomProbablyPresent(Project project) {
        var loom = project.extensions.findByName('loom')
        // The loom extension is present and looks sort of like what we expect
        return loom !== null && loom.hasProperty('minecraftVersion')
    }

    void multiplatform(Action<Multiplatform> action) {
        action.execute(multiplatform)
    }

    void enable() {
        setupDsl.set(true)
        setupPlugins.set(true)
        setupTasks.set(true)
        setupGatherTask.set(true)
    }

    void disable() {
        setupDsl.set(false)
        setupPlugins.set(false)
        setupTasks.set(false)
        setupGatherTask.set(false)
    }

    void platform(Platform platform) {
        platforms.add(platform)
    }

    void setPlatform(Platform platform) {
        this.platforms.set(List.of(platform))
    }

    void setPlatform(String platform) {
        this.platforms.set(List.of(Platform.of(platform)))
    }

    void gather(Action<AbstractGatherPlatformDetailsTask> action) {
        gatherActions.add(action)
    }

    void convert(Action<AbstractMDGConvertTask> action) {
        convertActions.add(action)
    }

    void apply() {
        if (applied) {
            return
        }

        applied = true

        this.platforms.get().each { platform ->
            // setup MDG dependency configurations
            final rootConfiguration = project.configurations.register(forSourceSetName(sourceSet.name, CONFIGURATION_NAME_ROOT + platform.name.capitalize())) { Configuration conf -> conf.tap {
                canBeConsumed = false
                attributes.tap {
                    attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category, Category.LIBRARY))
                    attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling, Bundling.EXTERNAL))
                    attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, project.objects.named(TargetJvmEnvironment, TargetJvmEnvironment.STANDARD_JVM))
                    attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage, Usage.JAVA_RUNTIME))
                    attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements, LibraryElements.JAR))
                }
            }}
            rootConfiguration.configure { conf ->
                conf.dependencies.add(project.dependencies.platform("${MDG_MAVEN_GROUP}:modsdotgroovy:${ModsDotGroovyGradlePlugin.VERSION}"))
            }

            final frontendConfiguration = project.configurations.register(forSourceSetName(sourceSet.name, CONFIGURATION_NAME_FRONTEND + platform.name.capitalize())) { Configuration conf ->
                conf.extendsFrom rootConfiguration.get()
            }
            final pluginConfiguration = project.configurations.register(forSourceSetName(sourceSet.name, CONFIGURATION_NAME_PLUGIN + platform.name.capitalize())) { Configuration conf ->
                conf.extendsFrom rootConfiguration.get()
            }

            // setup required MDG repositories and dependencies for better IDE support
            project.repositories.mavenCentral()
            rootConfiguration.configure(conf -> conf.dependencies.add(project.dependencies.create('org.apache.groovy:groovy:4.0.19')))

            // if asked, setup the mods.groovy DSL
            if (this.setupDsl.get())
                setupDsl(frontendConfiguration, platform)

            // the plugins have to be done on a per-platform basis
            // if asked, setup the mods.groovy plugins
            if (this.setupPlugins.get())
                setupPlugins(pluginConfiguration, platform)

            // now the hard part - the tasks
            // if asked, setup the mods.groovy Gradle tasks
            if (this.setupTasks.get())
                setupTasks(sourceSet, platform, rootConfiguration, pluginConfiguration, frontendConfiguration)
            else if (this.setupGatherTask.get())
                setupGatherTask(platform, sourceSet)


            // setup IDE support by adding the mdgFrontend configuration to the compileOnly configuration
            project.configurations.named(sourceSet.compileOnlyConfigurationName).configure(conf -> conf.extendsFrom(frontendConfiguration.get()))
        }
    }

    @CompileStatic
    class Multiplatform {
        private boolean exposed = false

        void from(String projectPath) {
            from(projectPath, sourceSet.name)
        }

        void from(String projectPath, String sourceSetName) {
            var configurationName = forSourceSetName(sourceSetName, EXPOSE_SOURCE_SET)
            var consumingConfiguration = project.configurations.create(
                    forSourceSetName(sourceSet.name, CONSUME_SOURCE_SET)
            )
            consumingConfiguration.dependencies.add project.dependencies.project(path: projectPath, configuration: configurationName)
            consumingConfiguration.canBeResolved = true
            consumingConfiguration.canBeConsumed = false
            consumingConfiguration.visible = false
            modsDotGroovyFile.set(consumingConfiguration)

            var gatherConfigurationName = forSourceSetName(sourceSetName, EXPOSE_GATHERED)
            var gatherConsumingConfiguration = project.configurations.create(
                    forSourceSetName(sourceSet.name, CONSUME_GATHERED)
            )
            gatherConsumingConfiguration.dependencies.add project.dependencies.project(path: projectPath, configuration: gatherConfigurationName)
            gatherConsumingConfiguration.canBeResolved = true
            gatherConsumingConfiguration.canBeConsumed = false
            gatherConsumingConfiguration.visible = false
            gather {
                it.parents.from(gatherConsumingConfiguration)
                it.dependsOn(gatherConsumingConfiguration)
            }

            multiplatformFlag.set(true)
        }

        void enable() {
            multiplatformFlag.set(true)
        }

        void expose(Object file, Action<? super ConfigurablePublishArtifact> configureAction) {
            exposed = true
            setupPlugins.set(false)
            setupTasks.set(false)
            setupGatherTask.set(true)
            multiplatformFlag.set(true)
            platforms.set([Platform.UNKNOWN])
            var configurationName = forSourceSetName(sourceSet.name, EXPOSE_SOURCE_SET)
            var exposingConfiguration = project.configurations.maybeCreate(configurationName)
            exposingConfiguration.canBeResolved = false
            exposingConfiguration.canBeConsumed = true

            var gatherConfigurationName = forSourceSetName(sourceSet.name, EXPOSE_GATHERED)
            var gatherExposingConfiguration = project.configurations.maybeCreate(gatherConfigurationName)
            gatherExposingConfiguration.canBeResolved = false
            gatherExposingConfiguration.canBeConsumed = true

            project.artifacts {
                add(configurationName, file, configureAction)
            }
        }

        void expose(Object file) {
            expose(file, {})
        }

        void expose() {
            var file = sourceSet.resources.matching {
                include DEFAULT_MDG
            }
            expose(project.provider { file.singleFile })
        }
    }

    private static String forSourceSetName(String sourceSetName, String name) {
        return sourceSetName == SourceSet.MAIN_SOURCE_SET_NAME ? name : "${sourceSetName}${name.capitalize()}"
    }

    private void setupDsl(NamedDomainObjectProvider<Configuration> frontendConfiguration, Platform platform) {
        if (!multiplatformFlag.get() && platform !in Platform.STOCK_PLATFORMS) {
            throw new UnsupportedOperationException("""
                There is no stock frontend DSL available for $platform on this version of ModsDotGroovy.
                Possible solutions:
                - Check for updates to ModsDotGroovy
                - Use a custom frontend by setting the 'setupDsl' property to false and adding an mdgFrontend dependency
            """.stripIndent().strip())
        }

        // mdgFrontend "org.groovymc.modsdotgroovy.frontend-dsl:<platform>"
        final String platformName = multiplatformFlag.get() ? 'multiplatform' : platform.name.toLowerCase(Locale.ROOT)
        frontendConfiguration.configure(conf -> conf.dependencies.add(project.dependencies.create(MDG_FRONTEND_GROUP + ':' + platformName)))
    }

    private void setupPlugins(NamedDomainObjectProvider<Configuration> pluginConfiguration, Platform platform) {
        if (platform !in Platform.STOCK_PLATFORMS)
            return // no stock plugins available for this platform

        // mdgPlugin "org.groovymc.modsdotgroovy.stock-plugins:<platform>"
        pluginConfiguration.configure(conf -> {
            if (multiplatformFlag.get()) conf.dependencies.add(project.dependencies.create(MDG_PLUGIN_GROUP + ':multiplatform'))

            conf.dependencies.add(project.dependencies.create(MDG_PLUGIN_GROUP + ':' + platform.name.toLowerCase(Locale.ROOT)))
        })
    }

    private <T extends AbstractGatherPlatformDetailsTask> TaskProvider<T> makeGatherTask(Platform platform, Class<T> gatherType, Object... args) {
        return project.tasks.register(forSourceSetName(sourceSet.name, "gather${platform.name.capitalize()}PlatformDetails"), gatherType, args).tap {
            configure {
                it.group = MDGExtension.TASK_GROUP
            }
        }
    }

    private static VersionCatalog getLibsExtension(Project project, String name) {
        Optional<? extends VersionCatalog> catalogView = project.extensions.findByType(VersionCatalogsExtension)?.find(name)
        return catalogView?.orElse(null)
    }

    private static Map versionCatalogToMap(Map out, VersionCatalog catalog) {
        Map versions = out.computeIfAbsent('versions', { [:] }) as Map
        Map libraries = out.computeIfAbsent('libraries', { [:] }) as Map
        Map plugins = out.computeIfAbsent('plugins', { [:] }) as Map
        Map bundles = out.computeIfAbsent('bundles', { [:] }) as Map
        if (catalog === null)
            return [:]
        catalog.versionAliases.each {
            var val = catalog.findVersion(it)
            if (val.isPresent())
                versions[it.replace(CATALOG_ID_REPLACEMENTS)] = val.get() as String
        }
        catalog.pluginAliases.each {
            var val = catalog.findPlugin(it)
            if (val.isPresent()) {
                def plugin = val.get().get()
                plugins[it.replace(CATALOG_ID_REPLACEMENTS)] = [
                        id: plugin.pluginId as String,
                        version: plugin.version as String
                ]
            }
        }
        catalog.bundleAliases.each {
            var val = catalog.findBundle(it)
            if (val.isPresent()) {
                var modules = val.get().get()
                bundles[it.replace(CATALOG_ID_REPLACEMENTS)] = modules.collect {
                    moduleToMap(it)
                }
            }
        }
        catalog.libraryAliases.each {
            var val = catalog.findLibrary(it)
            if (val.isPresent()) {
                var module = val.get().get()
                libraries[it.replace(CATALOG_ID_REPLACEMENTS)] = moduleToMap(module)
            }
        }
        return out
    }

    private static Map moduleToMap(MinimalExternalModuleDependency module) {
        return [
                group  : module.group as String,
                name   : module.name as String,
                version: module.version as String
        ]
    }

    void setupCatalogs(TaskProvider<? extends AbstractGatherPlatformDetailsTask> gatherTask, List<String> versionCatalogs) {
        Map catalogData = [:]
        versionCatalogs.each { catalog ->
            def libs = getLibsExtension(project, catalog)
            if (libs !== null) {
                versionCatalogToMap(catalogData, libs)
            }
        }

        if (!catalogData.isEmpty()) {
            gatherTask.configure {
                it.extraProperties.put('libs', catalogData)
            }
        }
    }

    private void setupTasks(SourceSet sourceSet, Platform platform, Provider<Configuration> root, Provider<Configuration> plugin, Provider<Configuration> frontend) {
        // three steps:
        // 1. register a task to gather platform's details
        // 2. register a task to convert the mods.groovy file to a platform-specific file
        // 3. configure those tasks appropriately

        final TaskProvider<ProcessResources> processResourcesTask = project.tasks.named(sourceSet.processResourcesTaskName, ProcessResources)

        TaskProvider<? extends AbstractGatherPlatformDetailsTask> gatherTask = setupGatherTask(platform, sourceSet)

        TaskProvider<? extends AbstractMDGConvertTask> convertTask
        String processResourcesDestPath
        switch (platform) {
            case Platform.FORGE:
                convertTask = project.tasks.register(forSourceSetName(sourceSet.name, 'modsDotGroovyToTomlForge'), ModsDotGroovyToToml)
                processResourcesDestPath = 'META-INF'
                break
            case Platform.NEOFORGE:
                // Handle change of metadata location in 20.5
                Provider<String> conventionalLocation = gatherTask.flatMap { it.platformVersion.map {
                    FlexVerComparator.compare(it, '20.5.0-alpha') <= 0 ? 'mods.toml' : 'neoforge.mods.toml'
                }.orElse('mods.toml') }
                convertTask = project.tasks.register(forSourceSetName(sourceSet.name, 'modsDotGroovyToTomlNeoForge'), ModsDotGroovyToToml)
                convertTask.configure {
                    it.outputName.convention(conventionalLocation)
                }
                processResourcesDestPath = 'META-INF'
                break
            case Platform.FABRIC:
                convertTask = project.tasks.register(forSourceSetName(sourceSet.name, 'modsDotGroovyToJsonFabric'), ModsDotGroovyToJson) { ModsDotGroovyToJson task ->
                    task.outputName.set('fabric.mod.json')
                }
                processResourcesDestPath = '.'
                break
            case Platform.QUILT:
                convertTask = project.tasks.register(forSourceSetName(sourceSet.name, 'modsDotGroovyToJsonQuilt'), ModsDotGroovyToJson) { ModsDotGroovyToJson task ->
                    task.outputName.set('quilt.mod.json')
                }
                processResourcesDestPath = '.'
                break
            case Platform.SPIGOT:
                convertTask = project.tasks.register(forSourceSetName(sourceSet.name, 'modsDotGroovyToYmlSpigot'), ModsDotGroovyToYml) { ModsDotGroovyToYml task ->
                    task.outputName.set('spigot.yml')
                }
                processResourcesDestPath = '.'
                break
            default:
                convertTask = null
        }

        if (convertTask != null) {
            processResourcesTask.configure { task ->
                task.dependsOn convertTask
                task.from(convertTask.get().output) { CopySpec spec ->
                    spec.into processResourcesDestPath
                }
            }
            convertTask.configure { task ->
                task.dependsOn gatherTask
                task.platformDetailsFile.set(gatherTask.get().outputFile)
                task.platform.set(platform)
                task.isMultiplatform.set(multiplatformFlag)
                task.input.fileProvider(modsDotGroovyFile.map(FileCollection::getSingleFile))
                task.mdgRuntimeFiles.from(
                        root,
                        plugin,
                        frontend
                )

                task.group = MDGExtension.TASK_GROUP
                this.convertActions.get().each { action ->
                    action.execute(task)
                }
            }
        }
    }

    private TaskProvider<? extends AbstractGatherPlatformDetailsTask> setupGatherTask(Platform platform, SourceSet sourceSet) {
        TaskProvider<? extends AbstractGatherPlatformDetailsTask> gatherTask

        boolean loomPresent = isLoomProbablyPresent(project)

        if (inferGather.get()) {
            switch (platform) {
                case Platform.FORGE:
                    if (loomPresent) {
                        gatherTask = makeGatherTask(platform, GatherLoomPlatformDetails)
                        gatherTask.configure { task ->
                            Configuration forgeConfig = project.configurations.getByName('forgeUserdev')
                            Provider<Set<ResolvedArtifactResult>> artifacts = forgeConfig.incoming.artifacts.resolvedArtifacts
                            task.artifactIds.set(artifacts.map(artifact -> artifact*.id))
                            task.targetModule.set('minecraftforge')
                            task.targetGroup.set('net.minecraftforge')
                        }
                    } else {
                        gatherTask = makeGatherTask(platform, GatherForgePlatformDetails)
                        gatherTask.configure { task ->
                            Configuration minecraftConfig = project.configurations.getByName('minecraft')
                            Provider<Set<ResolvedArtifactResult>> artifacts = minecraftConfig.incoming.artifacts.resolvedArtifacts
                            task.artifactIds.set(artifacts.map(artifact -> artifact*.id))
                        }
                    }
                    break
                case Platform.NEOFORGE:
                    if (loomPresent) {
                        gatherTask = makeGatherTask(platform, GatherLoomPlatformDetails)
                        gatherTask.configure { task ->
                            Configuration neoForgeConfig = project.configurations.getByName('forgeUserdev')
                            Provider<Set<ResolvedArtifactResult>> artifacts = neoForgeConfig.incoming.artifacts.resolvedArtifacts
                            task.artifactIds.set(artifacts.map(artifact -> artifact*.id))
                            task.targetModule.set('neoforge')
                            task.targetGroup.set('net.neoforged')
                        }
                    } else {
                        gatherTask = makeGatherTask(platform, GatherNeoForgePlatformDetails, sourceSet.compileClasspathConfigurationName)
                    }
                    break
                case Platform.FABRIC:
                    gatherTask = makeGatherTask(platform, GatherLoomPlatformDetails)
                    gatherTask.configure { task ->
                        Configuration modCompileClasspath = project.configurations.getByName('modCompileClasspath')
                        Provider<Set<ResolvedArtifactResult>> artifacts = modCompileClasspath.incoming.artifacts.resolvedArtifacts
                        task.artifactIds.set(artifacts.map(artifact -> artifact*.id))
                        task.targetModule.set('fabric-loader')
                        task.targetGroup.set('net.fabricmc')
                    }
                    break
                case Platform.QUILT:
                    gatherTask = makeGatherTask(platform, GatherLoomPlatformDetails)
                    gatherTask.configure { task ->
                        Configuration modCompileClasspath = project.configurations.getByName('modCompileClasspath')
                        Provider<Set<ResolvedArtifactResult>> artifacts = modCompileClasspath.incoming.artifacts.resolvedArtifacts
                        task.artifactIds.set(artifacts.map(artifact -> artifact*.id))
                        task.targetModule.set('quilt-loader')
                        task.targetGroup.set('org.quiltmc')
                    }
                    break
                default:
                    gatherTask = makeGatherTask(platform, AbstractGatherPlatformDetailsTask)
            }
        } else {
            gatherTask = makeGatherTask(platform, AbstractGatherPlatformDetailsTask)
        }

        // If we have any version catalogs specified, we should set them up
        setupCatalogs(gatherTask, getCatalogs().get())

        gatherActions.get().each { action ->
            gatherTask.configure(action)
        }

        if (multiplatform.exposed) {
            project.artifacts {
                add(forSourceSetName(sourceSet.name, EXPOSE_GATHERED), gatherTask.flatMap { it.outputFile }) {
                    builtBy(gatherTask)
                }
            }
        }

        return gatherTask
    }
}
