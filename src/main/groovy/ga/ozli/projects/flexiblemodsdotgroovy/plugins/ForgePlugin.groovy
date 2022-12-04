package ga.ozli.projects.flexiblemodsdotgroovy.plugins

import ga.ozli.projects.flexiblemodsdotgroovy.ModInfoBuilder
import ga.ozli.projects.flexiblemodsdotgroovy.ModsBuilder
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
        atRoot = parent === null

        println "parent: $parent, self: $self"

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
        println "key: $key, objectIn: $objectIn"
        if (key == 'modInfo' || key == 'mod') {
            return setModInfo(objectIn)
        }

        return null
    }

    String setModLoader(String modLoader) throws Exception {
        if (atRoot && modLoader.allWhitespace)
            throw new Exception('modLoader cannot be set to all whitespace')

        if (modLoader == '42') {
            println 'Indeed - that modLoader is the answer to life, the universe, and everything'
            modLoader = 'javafml'
        }

        return modLoader
    }

    static boolean setOnForge(@DelegatesTo(value = ModsDotGroovy, strategy = DELEGATE_FIRST) final Closure closure) {
        closure.delegate = ModsDotGroovy
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call()

        return true
    }

    static boolean setOnFabric(final Closure closure) {
        return handleCrossPlatform('Fabric')
    }

    static boolean setOnQuilt(final Closure closure) {
        return handleCrossPlatform('Quilt')
    }

    static boolean handleCrossPlatform(final String platform) {
        final Map pluginsMap = ModsDotGroovyCore.INSTANCE.pluginsMap
        if ('CrossPlatformPlugin' in pluginsMap && "${platform}Plugin" in pluginsMap)
            return false // let the platform plugin handle this
        else
            throw new RuntimeException("on$platform {} is not supported in Forge projects. Did you forget to add the $platform and CrossPlatform plugins?")
    }

    static List<Map<String, ?>> setMods(final Tuple2<PluginAwareMap, Closure> mods) {
        final modsBuilder = new ModsBuilder(mods.v1)
        mods.v2.delegate = modsBuilder
        mods.v2.resolveStrategy = DELEGATE_FIRST
        mods.v2.call(modsBuilder)
        modsBuilder.build()
        println "modsBuilder.mods: ${modsBuilder.mods}"

        if (modsBuilder.mods.isEmpty()) {
            throw new RuntimeException('No mods were specified')
        }

        return modsBuilder.mods
    }

    static Map<String, ?> setModInfo(final Tuple2<PluginAwareMap, Closure> modInfo) {
        println "called setModInfo with parent: ${modInfo.v1}, closure: ${modInfo.v2}"
        final modInfoBuilder = new ModInfoBuilder(modInfo.v1)
        modInfo.v2.delegate = modInfoBuilder
        modInfo.v2.resolveStrategy = DELEGATE_FIRST
        modInfo.v2.call(modInfoBuilder)
        modInfoBuilder.build()

        return modInfoBuilder.toMap()
    }

    static Map<String, ?> setMod(final Tuple2<PluginAwareMap, Closure> modInfo) {
        return setModInfo(modInfo)
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
