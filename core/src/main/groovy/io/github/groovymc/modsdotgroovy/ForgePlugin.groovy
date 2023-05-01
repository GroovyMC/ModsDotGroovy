package io.github.groovymc.modsdotgroovy

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import io.github.groovymc.modsdotgroovy.plugin.ModsDotGroovyPlugin
import io.github.groovymc.modsdotgroovy.plugin.PluginResult
import io.github.groovymc.modsdotgroovy.plugin.PluginUtils
import org.apache.logging.log4j.core.Logger
import org.jetbrains.annotations.Nullable

import java.util.regex.Matcher
import java.util.regex.Pattern

@CompileStatic
@SuppressWarnings('GroovyUnusedDeclaration') // All these methods are dynamically called by ModsDotGroovyCore
@Log4j2(category = 'MDG - ForgePlugin')
class ForgePlugin extends ModsDotGroovyPlugin {

    // note: void methods are executed and treated as PluginResult.VALIDATE
    void setModLoader(final String modLoader) {
        log.debug "modLoader: ${modLoader}"
        if (PluginUtils.startsWithNumber(modLoader))
            throw new PluginResult.MDGPluginException('modLoader must not start with a number.')
    }

    void setLicense(final String license) {
        log.debug "license: ${license}"
        if (license.isBlank())
            throw new PluginResult.MDGPluginException('license cannot be set to a blank string. Omit the setter altogether if you don\'t want to specify a license.')
    }

    /** Support British spelling as an alias */
    PluginResult setLicence(final String licence) {
        log.debug "licence: ${licence}"
        return PluginResult.rename('license', licence)
    }

    void setIssueTrackerUrl(final String issueTrackerUrl) {
        log.debug "issueTrackerUrl: ${issueTrackerUrl}"
        if (!PluginUtils.isValidHttpUrl(issueTrackerUrl))
            throw new PluginResult.MDGPluginException('issueTrackerUrl must start with http:// or https://')
    }

    class Mods {
        private final List modInfos = []

        def onNestLeave(final Deque<String> stack, final Map value) {
            log.debug "mods.onNestLeave: ${value}"
            return modInfos
        }

        def onNestEnter(final Deque<String> stack, final Map value) {
            log.debug "mods.onNestEnter: ${value}"
            modInfos.clear()
            return new PluginResult.Validate()
        }

        class ModInfo {
            String modId

            PluginResult onNestLeave(final Deque<String> stack, final Map value) {
                log.debug "mods.modInfo.onNestLeave"
                modInfos.add(value)
                return PluginResult.remove()
            }

            def setModId(final String modId) {
                log.debug "mods.modInfo.modId: ${modId}"

                // validate the modId string
                // https://github.com/MinecraftForge/MinecraftForge/blob/4b813e4319fbd4e7f1ea2a7edaedc82ba617f797/fmlloader/src/main/java/net/minecraftforge/fml/loading/moddiscovery/ModInfo.java#L32
                if (!modId.matches(/^[a-z][a-z0-9_]{3,63}\u0024/)) {
                    // if the modId is invalid, do a bunch of checks to generate a more helpful error message
                    final StringBuilder errorMsg = new StringBuilder('modId must match the regex /^[a-z][a-z0-9_]{3,63}$/.')

                    if (modId.contains('-') || modId.contains(' '))
                        errorMsg.append('\nDashes and spaces are not allowed in modId as per the JPMS spec. Use underscores instead.')
                    if (PluginUtils.startsWithNumber(modId))
                        errorMsg.append('\nmodId cannot start with a number.')
                    if (modId != modId.toLowerCase(Locale.ROOT))
                        errorMsg.append('\nmodId must be lowercase.')

                    if (modId.length() < 4)
                        errorMsg.append('\nmodId must be at least 4 characters long to avoid conflicts.')
                    else if (modId.length() > 64)
                        errorMsg.append('\nmodId cannot be longer than 64 characters.')

                    throw new PluginResult.MDGPluginException(errorMsg.toString())
                }
                this.modId = modId
                return PluginResult.rename('modIdNew', modId)
            }

            PluginResult setAuthor(final String author) {
                log.debug "mods.modInfo.author: ${author}"
                return PluginResult.rename('authors', [author])
            }

            void setLogoFile(final String logoFile) {
                log.debug "mods.modInfo.logoFile: ${logoFile}"
                if (!logoFile.contains('.'))
                    throw new PluginResult.MDGPluginException('logoFile is missing a file extension. Did you forget to put ".png" at the end?')
            }

            void setUpdateJsonUrl(final String updateJsonUrl) {
                log.debug "mods.modInfo.updateJsonUrl: ${updateJsonUrl}"
                if (!PluginUtils.isValidHttpUrl(updateJsonUrl))
                    throw new PluginResult.MDGPluginException('updateJsonUrl must start with http:// or https://')
            }

            class Dependencies {
                private final List dependencies = []

                def onNestEnter(final Deque<String> stack, final Map value) {
                    log.debug "mods.modInfo.dependencies.onNestEnter: ${value}"
                    if (ModInfo.this.modId === null)
                        throw new PluginResult.MDGPluginException('modId must be set before dependencies can be set.')

                    dependencies.clear()
                    return new PluginResult.Validate()
                }

                PluginResult onNestLeave(final Deque<String> stack, final Map value) {
                    log.debug "mods.modInfo.dependencies.onNestLeave"
                    return PluginResult.move(['dependencies'], ModInfo.this.modId, dependencies)
                }

                @Nullable
                @CompileDynamic // todo: figure out why this is never called
                def set(final Deque<String> stack, final String name, def value) {
                    log.debug "mods.modInfo.dependencies.set(name: $name, value: $value)"

                    // support `modId = "versionRange"` syntax
                    final newStack = new ArrayDeque<String>(stack)
                    newStack.push('dependency')
                    return new PluginResult.Change(newLocation: newStack, newValue: [modId: name, versionRange: value])

                    // todo: support `modId { versionRange = '...'; side = DependencySide.CLIENT; ... }` syntax
                }

                class Dependency {
                    @Nullable String modId = null
                    @Nullable String versionRange = null

                    PluginResult onNestLeave(final Deque<String> stack, final Map value) {
                        log.debug "mods.modInfo.dependencies.dependency.onNestLeave"
                        if (this.modId === null)
                            throw new PluginResult.MDGPluginException('dependency is missing a modId')

                        if (this.versionRange === null)
                            throw new PluginResult.MDGPluginException("dependency \"${this.modId}\" is missing a versionRange")

                        dependencies.add(value)
                        return PluginResult.remove()
                    }
                }
            }
        }
    }

    @Override
    Logger getLog() {
        return log
    }

    @Override
    @Nullable
    @CompileDynamic
    def set(final Deque<String> stack, final String name, def value) {
        log.debug "set(name: $name, value: $value)"

        if (!stack.isEmpty() && name == 'modLoader') {
            log.warn "modLoader should be set at the root but it was found in ${stack.join '->'}"

            // move the modLoader to the root by returning an empty stack
            return PluginResult.move([], value as String)
        }

        return new PluginResult.Unhandled()
    }

    @Override
    PluginResult onNestEnter(final Deque<String> stack, final String name, final Map value) {
        log.debug "onNestEnter(name: $name, value: $value)"
        return new PluginResult.Unhandled()
    }

    @Override
    @CompileDynamic
    def onNestLeave(final Deque<String> stack, final String name, Map value) {
        log.debug "onNestLeave(name: $name, value: $value)"
        return new PluginResult.Unhandled()
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
