package io.github.groovymc.modsdotgroovy

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovyjarjarantlr4.v4.runtime.misc.Nullable
import io.github.groovymc.modsdotgroovy.plugin.ModsDotGroovyPlugin
import io.github.groovymc.modsdotgroovy.plugin.PluginResult

@CompileStatic
@SuppressWarnings('GroovyUnusedDeclaration') // All these methods are dynamically called by ModsDotGroovyCore
class ForgePlugin implements ModsDotGroovyPlugin {

    // note: void methods are executed and treated as PluginResult.VALIDATE
    void setModLoader(final String modLoader) {
        println "[Forge] modLoader: ${modLoader}"
        if (modLoader ==~ /^\d/)
            throw new RuntimeException('modLoader must not start with a number.')
    }

    final mods = new Object() {
        private final List modInfos = []

        def onNestLeave(final Deque<String> stack, final Map value) {
            println "[Forge] mods.onNestLeave: ${value}"
            return modInfos
        }

        def onNestEnter(final Deque<String> stack, final Map value) {
            println "[Forge] mods.onNestEnter: ${value}"
            modInfos.clear()
            return new PluginResult.Validate()
        }

        final modInfo = new Object() {
            PluginResult onNestLeave(final Deque<String> stack, final Map value) {
                println "[Forge] mods.modInfo.onNestLeave"
                modInfos.add(value)
                return PluginResult.remove()
            }

            PluginResult setModId(final String modId) {
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

                    return new PluginResult.Error(errorMsg.toString())
                }

                this.modId = modId
                return new PluginResult.Validate()
            }

            static class Dependencies {
                static PluginResult onNestLeave(final Deque<String> stack, final Map value) {
                    println "[Forge] mods.modInfo.dependencies.onNestLeave"
                    stack.addLast(modId) // redirect to mods.modInfo.dependencies.modId
                    return PluginResult.move(stack, value)
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
            return PluginResult.move([], value as String)
        }

        return new PluginResult.Unhandled()
    }

    @Override
    PluginResult onNestEnter(final Deque<String> stack, final String name, final Map value) {
        println '---'
        println "[Forge] onNestEnter(name: $name, value: $value)"
        println '---'
        return new PluginResult.Validate()
    }

    @Override
    @CompileDynamic
    def onNestLeave(final Deque<String> stack, final String name, Map value) {
        println '---'
        println "[Forge] onNestLeave(name: $name, value: $value)"
        println '---'
        return new PluginResult.Validate()
    }

    @Override
    @Nullable
    Map build(Map buildingMap) {
        return [
            modLoader: 'javafml',
            loaderVersion: '[1,)',
            license: 'All Rights Reserved'
        ]
    }
}
