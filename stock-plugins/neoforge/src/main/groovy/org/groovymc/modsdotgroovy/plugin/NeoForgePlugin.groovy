package org.groovymc.modsdotgroovy.plugin

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.core.Logger
import org.groovymc.modsdotgroovy.core.versioning.VersionRange
import org.jetbrains.annotations.Nullable

@CompileStatic
@SuppressWarnings('GroovyUnusedDeclaration') // All these methods are dynamically called by ModsDotGroovyCore
@Log4j2(category = 'MDG - NeoForgePlugin')
class NeoForgePlugin extends ModsDotGroovyPlugin {

    @Override
    byte getPriority() {
        return -10 // we want to run after ForgePlugin
    }

    @Override
    Logger getLog() {
        return log
    }

    class Mods {
        class ModInfo {
            @Nullable String modId = null

            class Aliases {
                private final List aliases = []

                PluginResult onNestEnter(final Deque<String> stack, final Map value) {
                    log.debug "mods.modInfo.aliases.onNestEnter: ${value}"
                    if (ModInfo.this.modId === null)
                        throw new PluginResult.MDGPluginException('modId must be set before aliases can be set.')

                    aliases.clear()
                    return new PluginResult.Validate()
                }

                PluginResult onNestLeave(final Deque<String> stack, final Map value) {
                    log.debug "mods.modInfo.aliases.onNestLeave"
                    return PluginResult.move(['aliases'], ModInfo.this.modId, aliases)
                }

                class Alias {
                    @Nullable String modId = null
                    @Nullable VersionRange versionRange = null

                    PluginResult setVersionRange(final VersionRange versionRange) {
                        log.debug "mods.modInfo.aliases.alias.versionRange: ${versionRange}"
                        this.versionRange = versionRange
                        return new PluginResult.Change(newValue: versionRange.toMaven())
                    }

                    PluginResult onNestLeave(final Deque<String> stack, final Map value) {
                        log.debug "mods.modInfo.aliases.alias.onNestLeave"
                        if (modId === null)
                            throw new PluginResult.MDGPluginException('alias is missing a modId')

                        if (versionRange === null)
                            throw new PluginResult.MDGPluginException("alias \"${this.modId}\" is missing a versionRange")

                        aliases.add(value)
                        return PluginResult.remove()
                    }
                }
            }
        }
    }
}
