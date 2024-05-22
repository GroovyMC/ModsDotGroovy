package org.groovymc.modsdotgroovy.plugin.spigot

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.core.Logger
import org.groovymc.modsdotgroovy.core.ConversionSettings
import org.groovymc.modsdotgroovy.plugin.ModsDotGroovyPlugin
import org.groovymc.modsdotgroovy.plugin.PluginResult
import org.groovymc.modsdotgroovy.plugin.PluginUtils

@CompileStatic
@SuppressWarnings('GroovyUnusedDeclaration') // All these methods are dynamically called by ModsDotGroovyCore
@Log4j2(category = 'MDG - SpigotPlugin')
final class SpigotPlugin extends ModsDotGroovyPlugin {

    @Override
    void init(final Map<String, ?> environment, ConversionSettings conversionSettings) {}

    @Override
    Logger getLog() {
        return log
    }

    void setMain(final String main) {
        log.debug "main: ${main}"

        if (!main.contains('.'))
            throw new PluginResult.MDGPluginException('The main attribute must contain the full namespace including the class itself. For example: "org.spigotmc.testplugin.Test"')
    }

    void setName(final String name) {
        log.debug "name: ${name}"

        // validate the name string
        if (!name.matches('[a-zA-Z0-9_]+'))
            throw new PluginResult.MDGPluginException('The name attribute can only contain alphanumeric characters and underscores.')
    }

    void setLoad(final String loadState) {
        log.debug "load: ${loadState}"

        if (loadState !in ['STARTUP', 'POSTWORLD'])
            throw new PluginResult.MDGPluginException('The load attribute must be one of the following: STARTUP or POSTWORLD')
    }

    void setWebsite(final String website) {
        log.debug "website: ${website}"

        // validate the website string
        if (!PluginUtils.isValidHttpUrl(website))
            throw new PluginResult.MDGPluginException('The website attribute must be a valid HTTP(S) URL.')
    }

    void setLibraries(final List<String> libraries) {
        // todo
        throw new PluginResult.MDGPluginException('The libraries attribute is not yet supported.')
    }

    class Commands {
        class Command {
            String name

            PluginResult onNestLeave(final Map value) {
                log.debug 'commands.command.onNestLeave'
                return PluginResult.move(['commands', name], value)
            }
        }
    }

    class Permissions {
        class Permission {
            PluginResult onNestLeave(final Map value) {
                log.debug 'permissions.permission.onNestLeave'
                return PluginResult.move(['permissions', name], value)
            }
        }
    }
}
