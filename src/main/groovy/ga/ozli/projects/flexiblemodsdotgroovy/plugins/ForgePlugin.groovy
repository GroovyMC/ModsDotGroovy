package ga.ozli.projects.flexiblemodsdotgroovy.plugins

import ga.ozli.projects.flexiblemodsdotgroovy.ModsDotGroovy
import ga.ozli.projects.flexiblemodsdotgroovy.ModsDotGroovyCore
import ga.ozli.projects.flexiblemodsdotgroovy.ModsDotGroovyPlugin
import ga.ozli.projects.flexiblemodsdotgroovy.PluginAwareMap
import ga.ozli.projects.flexiblemodsdotgroovy.PluginMode
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovyjarjarantlr4.v4.runtime.misc.Nullable

import static groovy.lang.Closure.DELEGATE_FIRST

@CompileStatic
class ForgePlugin implements ModsDotGroovyPlugin {
    private boolean atRoot = false

    @Override
    boolean shouldRun(@Nullable PluginAwareMap parent, PluginAwareMap self) {
        if (parent.is null) atRoot = true

        return true
    }

    @Override
    Tuple2<PluginMode, Map> getDefaults(final Map data) {
        if (atRoot) {
            data.platform = 'Forge'
            return new Tuple2<>(PluginMode.MERGE, data)
        }
        return null
    }

    @Override
    @CompileDynamic
    @Nullable
    def getFallbackFor(final Map data, final String key) throws Exception {
        if (key == 'license') {
            if (atRoot) return 'All Rights Reserved'
            else throw new RuntimeException('License must be specified at the root')
        }
        else return null
    }

    @Override
    @CompileDynamic
    @Nullable
    def set(final String key, def objectIn) throws Exception {
        if (atRoot && key == 'modLoader' && objectIn !instanceof String)
            throw new Exception('modLoader must be a String')

        println "key: $key, objectIn: $objectIn"

        return null
    }

    static String setModLoader(String modLoader) {
        if (modLoader == '42') {
            println 'Indeed - that modLoader is the answer to life, the universe, and everything'
            modLoader = 'javafml'
        }

        return modLoader
    }

    static boolean setOnForge(@DelegatesTo(value = ModsDotGroovy, strategy = DELEGATE_FIRST) Closure closure) {
        closure.delegate = ModsDotGroovy
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call()

        return true
    }

    static boolean setOnFabric(Closure closure) {
        final Map pluginsMap = ModsDotGroovyCore.INSTANCE.getPluginsMap()
        if (pluginsMap.containsKey('FabricPlugin') && pluginsMap.containsKey('CrossPlatformPlugin')) {
            return false // let the FabricPlugin handle this
        } else {
            throw new RuntimeException('onFabric {} is not supported in Forge projects. Did you forget to add the Fabric and CrossPlatform plugins?')
        }
    }

    static boolean setOnQuilt(Closure closure) {
        final Map pluginsMap = ModsDotGroovyCore.INSTANCE.getPluginsMap()
        if (pluginsMap.containsKey('QuiltPlugin') && pluginsMap.containsKey('CrossPlatformPlugin')) {
            return false // let the QuiltPlugin handle this
        } else {
            throw new RuntimeException('onQuilt {} is not supported in Forge projects. Did you forget to add the Quilt and CrossPlatform plugins?')
        }
    }

    static def setMods(def mods) {
        println mods
        return mods
    }

    static String setModId(String modId) {
        return modId
    }

    @Override
    @Nullable
    Tuple2<PluginMode, Map> build(final Map data) throws Exception {
        if (atRoot) {
            if (!data.containsKey('modLoader')) throw new Exception('modLoader must be specified')
        }
        return null
    }
}
