package org.groovymc.modsdotgroovy.plugin.multiplatform

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.core.Logger
import org.groovymc.modsdotgroovy.core.ConversionSettings
import org.groovymc.modsdotgroovy.plugin.ModsDotGroovyPlugin
import org.groovymc.modsdotgroovy.plugin.PluginResult
import org.groovymc.modsdotgroovy.types.core.Platform
import org.groovymc.modsdotgroovy.core.versioning.VersionRange

@CompileStatic
@SuppressWarnings('GroovyUnusedDeclaration') // All these methods are dynamically called by ModsDotGroovyCore
@Log4j2(category = 'MDG - MultiplatformPlugin')
final class MultiplatformPlugin extends ModsDotGroovyPlugin {
    private Platform currentPlatform = Platform.UNKNOWN

    @Override
    void init(final Map<String, ?> environment, ConversionSettings conversionSettings) {
        this.currentPlatform = Platform.of(environment['platform'].invokeMethod('name', null) as String)
        if (currentPlatform !in [Platform.FORGE, Platform.FABRIC, Platform.NEOFORGE, Platform.QUILT])
            throw new PluginResult.MDGPluginException('Unknown platform: ' + currentPlatform)
    }

    @Override
    Logger getLog() {
        return log
    }

    @Override
    byte getPriority() {
        // The multiplatform plugin needs to be called before other plugins so that it can translate the calls
        return 10
    }

    def setModLoader(final String modLoader) {
        if (isFabricLike(currentPlatform))
            return PluginResult.remove()
    }

    def setLoaderVersion(final String loaderVersion) {
        if (isFabricLike(currentPlatform))
            return PluginResult.remove()
    }

    def setSourcesUrl(final String sourcesUrl) {
        return switch (currentPlatform) {
            case Platform.FORGE, Platform.NEOFORGE -> PluginResult.remove()
            case Platform.FABRIC -> PluginResult.move(['contact', 'sources'], sourcesUrl)
            case Platform.QUILT -> PluginResult.move(['metadata', 'contact', 'sources'], sourcesUrl)
            default -> null
        }
    }

    def setLicence(final String licence) {
        if (currentPlatform == Platform.FABRIC)
        // ForgePlugin supports the "licence" alias, FabricPlugin does not
            return PluginResult.rename('license', licence)
        else if (currentPlatform == Platform.QUILT)
            return PluginResult.move(['quiltLoader', 'metadata', 'license'], licence)
    }

    def setLicense(final String licence) {
        if (currentPlatform == Platform.QUILT)
            return PluginResult.move(['quiltLoader', 'metadata', 'license'], licence)
    }

    def setIssueTrackerUrl(final String issueTrackerUrl) {
        if (currentPlatform == Platform.FABRIC)
            return PluginResult.move(['contact', 'issues'], issueTrackerUrl)
        else if (currentPlatform == Platform.QUILT)
            return PluginResult.move(['quiltLoader', 'metadata', 'contact', 'issues'], issueTrackerUrl)
    }

    def setEnvironment(def environment) {
        environment = platformedSide(environment)
        if (isForgeLike(currentPlatform))
            return PluginResult.remove()
        else if (currentPlatform == Platform.QUILT)
            return PluginResult.move(['minecraft', 'environment'], environment)
    }

    def setAccessWidener(final String accessWidener) {
        if (isForgeLike(currentPlatform))
            return PluginResult.remove()
        if (currentPlatform == Platform.QUILT)
            return PluginResult.move(['quiltLoader', 'accessWidener'], accessWidener)
    }

    def setIcon(final String icon) {
        if (isForgeLike(currentPlatform))
            return PluginResult.remove()
        if (currentPlatform == Platform.QUILT)
            return PluginResult.move(['quiltLoader', 'icon'], icon)
    }

    def setShowAsDataPack(final value) {
        if (isFabricLike(currentPlatform))
            return PluginResult.remove()
    }

    def setShowAsResourcePack(final value) {
        if (isFabricLike(currentPlatform))
            return PluginResult.remove()
    }

    class Mixins {
        class Mixin {
            def setEnvironment(final environment) {
                return platformedSide(environment)
            }
        }
    }

    class Icon {
        def onNestLeave(final Deque<String> stack, final Map value) {
            if (isForgeLike(currentPlatform))
                return PluginResult.remove()
            if (currentPlatform == Platform.QUILT)
                return PluginResult.move(['quiltLoader', 'icon'], value)
        }
    }

    class Mods {
        def onNestLeave(final Map value) {
            if (isFabricLike(currentPlatform)) return PluginResult.remove()
        }

        class ModInfo {
            def onNestLeave(final List<String> stack, final Map value) {
                if (currentPlatform == Platform.FABRIC) {
                    // remove the "modInfo" and "mods" keys
                    List<String> newStack = new ArrayList()
                    for (int i = 2; i < stack.size(); i++) {
                        newStack.add(stack[i])
                    }
                    return PluginResult.move(newStack, value)
                } else if (currentPlatform == Platform.QUILT) {
                    // remove the "modInfo" and "mods" keys, add a "quiltLoader" key
                    List<String> newStack = new ArrayList()
                    for (int i = 2; i < stack.size(); i++) {
                        newStack.add(stack[i])
                    }
                    newStack.add('quiltLoader')
                    return PluginResult.move(newStack, value)
                }
            }

            def set(final List<String> stack, final String propertyName, final Map value) {
                if (currentPlatform == Platform.FABRIC) {
                    // remove the "modInfo" and "mods" keys
                    List<String> newStack = new ArrayList()
                    for (int i = 2; i < stack.size(); i++) {
                        newStack.add(stack[i])
                    }
                    newStack.add(propertyName)
                    return PluginResult.move(newStack, value)
                } else if (currentPlatform == Platform.QUILT) {
                    // remove the "modInfo" and "mods" keys, add a "quiltLoader" key
                    List<String> newStack = new ArrayList()
                    for (int i = 2; i < stack.size(); i++) {
                        newStack.add(stack[i])
                    }
                    newStack.add('quiltLoader')
                    newStack.add(propertyName)
                    return PluginResult.move(newStack, value)
                }
            }

            def setRepositories(final value) {
                if (currentPlatform != Platform.QUILT)
                    return PluginResult.remove()
            }

            def setLoadType(final value) {
                if (currentPlatform != Platform.QUILT)
                    return PluginResult.remove()
            }

            def setIntermediateMappings(final value) {
                if (currentPlatform != Platform.QUILT)
                    return PluginResult.remove()
            }

            def setAuthors(final authors) {
                if (currentPlatform == Platform.FABRIC) {
                    if (authors instanceof List) {
                        authors.each {
                            put(['authors', 'author'], it instanceof Map ? it : ['name': it], false)
                        }
                        return PluginResult.remove()
                    } else {
                        return PluginResult.move(['authors'], authors)
                    }
                } else if (currentPlatform == Platform.QUILT) {
                    def roles = [[(authors): 'Author']]
                    if (authors instanceof List) {
                        roles = authors.collect { it instanceof Map ? it : [(it): 'Author'] }
                    }
                    roles.each {
                        put(['quiltLoader', 'metadata', 'contributors', 'contributor'], it, false)
                    }
                    return PluginResult.remove()
                }
            }

            def setContributors(final contributors) {
                if (currentPlatform == Platform.FABRIC) {
                    if (contributors instanceof List) {
                        return PluginResult.move([], contributors.collect { it instanceof Map ? it : ['name': it] })
                    } else {
                        return PluginResult.move([], contributors)
                    }
                } else if (currentPlatform == Platform.QUILT) {
                    def roles = [[(contributors): 'Contributor']]
                    if (contributors instanceof List) {
                        roles = contributors.collect { it instanceof Map ? it : [(it): 'Contributor'] }
                    }
                    roles.each {
                        put(['quiltLoader', 'metadata', 'contributors', 'contributor'], it, false)
                    }
                    return PluginResult.remove()
                }
            }

            class Authors {
                private final List authors = []

                def onNestLeave(final Map value) {
                    if (isForgeLike(currentPlatform)) {
                        log.debug "authors.onNestLeave: ${value}"
                        return authors
                    }
                    return PluginResult.remove()
                }

                // We are aware of values being set in here
                def set(final List<String> stack, final String property, final value) {}
                def onNestLeave(final List<String> stack, final Map value) {}

                class Author {
                    String name

                    def onNestLeave(final Map value) {
                        if (isForgeLike(currentPlatform)) {
                            log.debug "authors.author.onNestLeave: ${value}"

                            if (!value.containsKey("name")) {
                                throw new PluginResult.MDGPluginException("Author name is required")
                            }
                            authors.add(value["name"])
                            return PluginResult.remove()
                        } else if (currentPlatform == Platform.FABRIC) {
                            return PluginResult.move(['authors', 'author'], value)
                        } else if (currentPlatform == Platform.QUILT) {
                            if (!value.containsKey('role)')) {
                                value['role'] = 'Author'
                            }
                            return PluginResult.move(['quiltLoader', 'metadata', 'contributors', 'contributor'], value)
                        }
                    }
                }
            }

            class Contributors {
                def onNestLeave(final Map value) {
                    return PluginResult.remove()
                }

                // We are aware of values being set in here
                def set(final List<String> stack, final String property, final value) {}
                def onNestLeave(final List<String> stack, final Map value) {}

                class Contributor {
                    String name

                    def onNestLeave(final Map value) {
                        if (isForgeLike(currentPlatform)) {
                            return PluginResult.remove()
                        } else if (currentPlatform == Platform.FABRIC) {
                            return PluginResult.move(['contributors', 'contributor'], value)
                        } else if (currentPlatform == Platform.QUILT) {
                            if (!value.containsKey('role)')) {
                                value['role'] = 'Contributor'
                            }
                            return PluginResult.move(['quiltLoader', 'metadata', 'contributors', 'contributor'], value)
                        }
                    }
                }
            }

            def setAuthor(final value) {
                if (currentPlatform == Platform.FABRIC) {
                    return PluginResult.move(['authors', 'author'], ['name':value], true)
                } else if (currentPlatform == Platform.QUILT) {
                    return PluginResult.move(['quiltLoader', 'metadata', 'contributors', 'contributor'], ['name':value, 'role':'Author'], true)
                }
                return new PluginResult.Validate()
            }

            class Entrypoints {
                def onNestLeave(final Map value) {
                    if (isForgeLike(MultiplatformPlugin.this.currentPlatform))
                        return PluginResult.remove()
                }

                class Entrypoint {
                    def onNestLeave(final Map value) {
                        if (currentPlatform == Platform.FABRIC)
                            return PluginResult.move(['entrypoints', 'entrypoint'], value)
                        else if (currentPlatform == Platform.QUILT)
                            return PluginResult.move(['quiltLoader', 'entrypoints', 'entrypoint'], value)
                    }
                }
            }

            def setModId(final String modId) {
                if (currentPlatform == Platform.FABRIC)
                    return PluginResult.move(['id'], modId)
                if (currentPlatform == Platform.QUILT)
                    return PluginResult.move(['quiltLoader', 'id'], modId)
            }

            def setDisplayName(final String value) {
                if (currentPlatform == Platform.FABRIC)
                    return PluginResult.move(['name'], value)
                else if (currentPlatform == Platform.QUILT)
                    return PluginResult.move(['quiltLoader', 'metadata', 'name'], value)
            }

            def setDescription(final String value) {
                if (currentPlatform == Platform.FABRIC)
                    return PluginResult.move(['description'], value)
                else if (currentPlatform == Platform.QUILT)
                    return PluginResult.move(['quiltLoader', 'metadata', 'description'], value)
            }

            def setDisplayUrl(final String value) {
                if (currentPlatform == Platform.FABRIC)
                    return PluginResult.move(['contact', 'homepage'], value)
                else if (currentPlatform == Platform.QUILT)
                    return PluginResult.move(['quiltLoader', 'metadata', 'contact', 'homepage'], value)
            }

            def setIcon(final value) {
                if (currentPlatform == Platform.FABRIC)
                    return PluginResult.move(['icon'], value)
                else if (currentPlatform == Platform.QUILT)
                    return PluginResult.move(['quiltLoader', 'metadata', 'icon'], value)
                else
                    return PluginResult.remove()
            }

            class Contact {
                def onNestLeave(final Map value) {
                    if (currentPlatform == Platform.FABRIC) {
                        return PluginResult.move(['contact'], value)
                    } else if (currentPlatform == Platform.QUILT) {
                        return PluginResult.move(['quiltLoader', 'metadata', 'contact'], value)
                    } else {
                        if (value.homepage !== null) {
                            put(['mods', 'modInfo', 'displayUrl'], value.homepage, false)
                        }
                        if (value.issues !== null) {
                            put(['issueTrackerUrl'], value.issues, false)
                        }
                        if (value.sources !== null) {
                            put(['sourcesUrl'], value.sources, false)
                        }
                        return PluginResult.remove()
                    }
                }
            }

            class Icon {
                def onNestLeave(final Map value) {
                    if (isForgeLike(currentPlatform))
                        return PluginResult.remove()
                    if (currentPlatform == Platform.FABRIC)
                        return PluginResult.move(['icon'], value)
                    if (currentPlatform == Platform.QUILT)
                        return PluginResult.move(['quiltLoader', 'metadata', 'icon'], value)
                }
            }

            def setCredits(final value) {
                if (isFabricLike(currentPlatform))
                    return PluginResult.remove()
            }

            def setLogoFile(final value) {
                if (isFabricLike(currentPlatform))
                    return setIcon(value)
            }

            def setDisplayTest(final value) {
                if (isFabricLike(currentPlatform))
                    return PluginResult.remove()
            }

            def setUpdateJsonUrl(final value) {
                if (isFabricLike(currentPlatform))
                    return PluginResult.remove()
            }

            def set(final String property, final value) {
                if (currentPlatform == Platform.FABRIC)
                    return PluginResult.move([property], value)
                if (currentPlatform == Platform.QUILT)
                    return PluginResult.move(['quiltLoader', property], value)
            }

            class Features {
                def onNestLeave(final Map value) {
                    if (isFabricLike(currentPlatform))
                        return PluginResult.remove()
                }
            }

            class Dependencies {
                def onNestLeave(final Map value) {
                    if (currentPlatform == Platform.FABRIC)
                        return PluginResult.remove()
                    else if (currentPlatform == Platform.QUILT)
                        return PluginResult.remove()
                }

                def set(final String property, final value) {
                    if (currentPlatform == Platform.FABRIC) {
                        return PluginResult.move(['depends', property], value)
                    } else if (currentPlatform == Platform.QUILT) {
                        return PluginResult.move(['quiltLoader', 'depends', property], value)
                    }
                }

                class Dependency {
                    def setVersionRange(final value) {
                        if (currentPlatform == Platform.QUILT) {
                            return PluginResult.rename('versions', value)
                        }
                    }

                    def setOrder(final value) {
                        if (!isForgeLike(currentPlatform)) {
                            return PluginResult.remove()
                        }
                    }

                    def setSide(final value) {
                        if (!isForgeLike(currentPlatform)) {
                            return PluginResult.rename('environment', platformedSide(value))
                        }
                    }

                    def setModId(final value) {
                        if (currentPlatform == Platform.QUILT) {
                            return PluginResult.rename('id', value)
                        }
                    }

                    def onNestLeave(final Map value) {
                        if (currentPlatform == Platform.FORGE) {
                            if (!value.containsKey('mandatory') && value.containsKey('type')) {
                                switch (value.type) {
                                    case 'required':
                                        value.mandatory = true
                                        break
                                    case 'optional':
                                        value.mandatory = false
                                        break
                                    case 'incompatible':
                                        def version = value.versionRange
                                        version = version == null ? null : handleVersionRange(version)
                                        if (version instanceof VersionRange) {
                                            value.versionRange = ~version
                                            value.mandatory = false
                                            break
                                        }
                                        return PluginResult.remove()
                                    default:
                                        return PluginResult.remove()
                                }
                            }
                            return value
                        } else if (currentPlatform == Platform.NEOFORGE) {
                            if (!value.containsKey('type') && value.containsKey('mandatory')) {
                                value.type = value.mandatory ? 'required' : 'optional'
                            }
                            return value
                        } else if (currentPlatform == Platform.FABRIC) {
                            def type = 'required'
                            if (value.containsKey('type')) {
                                type = value.type
                            } else if (value.containsKey('mandatory')) {
                                type = value.mandatory ? 'required' : 'optional'
                            }
                            value.remove('type')
                            value.remove('mandatory')
                            switch (type as String) {
                                case 'required':
                                    return PluginResult.move(['depends', 'mod'], value)
                                    break
                                case 'optional':
                                    def version = value.versionRange
                                    version = version == null ? null : handleVersionRange(version)
                                    if (version instanceof VersionRange) {
                                        value.versionRange = ~version
                                        return PluginResult.move(['breaks', 'mod'], value)
                                    }
                                    return PluginResult.move(['suggests', 'mod'], value)
                                    break
                                case 'incompatible':
                                    return PluginResult.move(['breaks', 'mod'], value)
                                    break
                                case 'discouraged':
                                    return PluginResult.move(['conflicts', 'mod'], value)
                                    break
                                default:
                                    return PluginResult.move([type as String, 'mod'], value)
                            }
                        } else if (currentPlatform == Platform.QUILT) {
                            def type = 'required'
                            if (value.containsKey('type')) {
                                type = value.type
                            } else if (value.containsKey('mandatory')) {
                                type = value.mandatory ? 'required' : 'optional'
                            }
                            value.remove('type')
                            value.remove('mandatory')
                            switch (type as String) {
                                case 'required':
                                    return PluginResult.move(['quiltLoader', 'depends', 'mod'], value)
                                    break
                                case 'optional':
                                    def version = value.versions
                                    version = version == null ? null : handleVersionRange(version)
                                    if (version instanceof VersionRange) {
                                        value.versions = ~version
                                        return PluginResult.move(['quiltLoader', 'breaks', 'mod'], value)
                                    }
                                    break
                                case 'incompatible':
                                    return PluginResult.move(['quiltLoader', 'breaks', 'mod'], value)
                                    break
                                default:
                                    return PluginResult.move(['quiltLoader', type as String, 'mod'], value)
                            }
                        }
                    }
                }
            }
        }
    }

    private static def handleVersionRange(final Object value) {
        if (value instanceof String || value instanceof GString) {
            return VersionRange.of(value.toString())
        } else if (value instanceof List) {
            return new VersionRange.OrVersionRange(value.collect { (it instanceof String || it instanceof GString) ? VersionRange.of(it as String) : it as VersionRange })
        }
        return value
    }

    def platformedSide(final side) {
        if (currentPlatform == Platform.QUILT) {
            def s = (side as String).toLowerCase(Locale.ROOT)
            return switch (s) {
                case 'both' -> '*'
                case 'server' -> 'dedicated_server'
                default -> s
            }
        } else if (currentPlatform == Platform.FABRIC) {
            def s = (side as String).toLowerCase(Locale.ROOT)
            return switch (s) {
                case 'both' -> '*'
                case 'dedicated_server' -> 'server'
                default -> s
            }
        } else {
            def s = (side as String).toUpperCase(Locale.ROOT)
            return switch (s) {
                case '*' -> 'BOTH'
                case 'DEDICATED_SERVER' -> 'SERVER'
                default -> s
            }
        }
    }

    private static boolean isForgeLike(Platform platform) {
        return platform == Platform.FORGE || platform == Platform.NEOFORGE
    }

    private static boolean isFabricLike(Platform platform) {
        return platform == Platform.FABRIC || platform == Platform.QUILT
    }
}
