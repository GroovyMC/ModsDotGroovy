package ga.ozli.projects.flexiblemodsdotgroovy.plugins

import ga.ozli.projects.flexiblemodsdotgroovy.ModsDotGroovyPlugin
import ga.ozli.projects.flexiblemodsdotgroovy.PluginResult
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovyjarjarantlr4.v4.runtime.misc.Nullable

@CompileStatic
@SuppressWarnings('GroovyUnusedDeclaration') // All these methods are dynamically called by ModsDotGroovyCore
class ForgePlugin implements ModsDotGroovyPlugin {
    // note: void methods are executed and treated as PluginResult.VALIDATE
    static void setModLoader(final String modLoader) {
        println "[Forge] modLoader: ${modLoader}"
        if (modLoader ==~ /^\d/)
            throw new RuntimeException('modLoader must not start with a number.')
    }

    static class Mods {
        static void setInsideModsBuilder(final boolean insideModsBuilder) {
            println "[Forge] mods.insideModsBuilder: ${insideModsBuilder}"
        }

        @CompileDynamic
        static def setX(final def x) {
            println "[Forge] mods.x: ${x}"
            return '42'
        }

        static class ModInfo {
            static void setModId(final String modId) {
                println "[Forge] mods.modInfo.modId: ${modId}"

                // validate the modId string
                // https://github.com/MinecraftForge/MinecraftForge/blob/4b813e4319fbd4e7f1ea2a7edaedc82ba617f797/fmlloader/src/main/java/net/minecraftforge/fml/loading/moddiscovery/ModInfo.java#L32
                if (!modId.matches(/^[a-z][a-z0-9_]{3,63}\u0024/)) {
                    // if the modId is invalid, do a bunch of checks to generate a more helpful error message
                    final StringBuilder errorMsg = new StringBuilder('modId must match the regex /^[a-z][a-z0-9_]{3,63}$/.')

                    if (modId.contains('-') || modId.contains(' '))
                        errorMsg.append('\nDashes and spaces are not allowed in modId as per the JPMS spec. Use underscores instead.')
                    if (modId ==~ /^\d/)
                        errorMsg.append('\nmodId cannot start with a number.')
                    if (modId != modId.toLowerCase(Locale.ROOT))
                        errorMsg.append('\nmodId must be lowercase.')

                    if (modId.length() < 4)
                        errorMsg.append('\nmodId must be at least 4 characters long to avoid conflicts.')
                    else if (modId.length() > 64)
                        errorMsg.append('\nmodId cannot be longer than 64 characters.')

                    throw new RuntimeException(errorMsg.toString())
                }
            }
        }
    }

    @Override
    @Nullable
    @CompileDynamic
    def set(final Deque<String> stack, final String name, def value) {
        println "[Forge] set(name: $name, value: $value)"

        if (!stack.isEmpty() && name == 'modLoader') {
            println "[Forge] Warning: modLoader should be set at the root but it was found in ${stack.join '->'}"

            // move the modLoader to the root by returning an empty stack
            return new Tuple2<PluginResult, Tuple2<Deque, String>>(PluginResult.TRANSFORM, new Tuple2<Deque, String>(new ArrayDeque<String>(0), value as String))
        }

        return PluginResult.UNHANDLED
    }
}
