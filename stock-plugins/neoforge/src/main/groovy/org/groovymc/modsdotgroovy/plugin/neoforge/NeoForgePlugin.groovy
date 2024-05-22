package org.groovymc.modsdotgroovy.plugin.neoforge

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.core.Logger
import org.groovymc.modsdotgroovy.plugin.ModsDotGroovyPlugin
import org.groovymc.modsdotgroovy.plugin.PluginResult
import org.jetbrains.annotations.Nullable

@CompileStatic
@SuppressWarnings('GroovyUnusedDeclaration') // All these methods are dynamically called by ModsDotGroovyCore
@Log4j2(category = 'MDG - NeoForgePlugin')
final class NeoForgePlugin extends ModsDotGroovyPlugin {

    @Override
    byte getPriority() {
        return 5 // we want to run before ForgeLikePlugin
    }

    @Override
    Logger getLog() {
        return log
    }

    class Mixins {
        private final List mixins = []

        PluginResult onNestLeave(final Map value) {
            log.debug "mixins.onNestLeave"
            return PluginResult.move(['mixins'], mixins)
        }

        class Mixin {
            PluginResult onNestLeave(final Map value) {
                log.debug "mixins.mixin.onNestLeave"
                mixins.add(value)
                return PluginResult.remove()
            }
        }
    }

    class AccessTransformers {
        private final List accessTransformers = []

        PluginResult onNestLeave(final Map value) {
            log.debug "accessTransformers.onNestLeave"
            return PluginResult.move(['accessTransformers'], accessTransformers)
        }

        class AccessTransformer {
            PluginResult onNestLeave(final Map value) {
                log.debug "accessTransformers.accessTransformer.onNestLeave"
                accessTransformers.add(value)
                return PluginResult.remove()
            }
        }
    }

    class Mods {
        class ModInfo {
            @Nullable String modId = null

            class Dependencies {
                class Dependency {
                    def setType(final type) {
                        if (type instanceof Enum)
                            return PluginResult.of(type.name().toLowerCase(Locale.ROOT))
                    }

                    void onNestLeave(final Map value) {
                        if (value['type'] === null)
                            value['type'] = 'required'
                    }
                }
            }
        }
    }
}
