package org.groovymc.modsdotgroovy.plugin


import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.core.Logger
import org.jetbrains.annotations.Nullable

@CompileStatic
@SuppressWarnings('GroovyUnusedDeclaration') // All these methods are dynamically called by ModsDotGroovyCore
@Log4j2(category = 'MDG - ForgePlugin')
class ForgePlugin extends ModsDotGroovyPlugin {

    @Override
    byte getPriority() {
        return 5 // we want to run before ForgeLikePlugin
    }

    @Override
    Logger getLog() {
        return log
    }

    class Mods {
        class ModInfo {
            @Nullable String modId = null

            class Dependencies {
                class Dependency {
                    void onNestLeave(final Deque<String> stack, final Map value) {
                        if (value['mandatory'] === null)
                            value['mandatory'] = true
                    }
                }
            }
        }
    }
}
