package org.groovymc.modsdotgroovy.plugin

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.core.Logger
import org.groovymc.modsdotgroovy.core.Platform
import org.groovymc.modsdotgroovy.core.versioning.VersionRange

@CompileStatic
@SuppressWarnings('GroovyUnusedDeclaration') // All these methods are dynamically called by ModsDotGroovyCore
@Log4j2(category = 'MDG - MultiplatformPlugin')
class MultiplatformPlugin extends ModsDotGroovyPlugin {
    private Platform currentPlatform = Platform.UNKNOWN

    @Override
    void init(final Map<String, ?> environment) {
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
            case Platform.FABRIC -> PluginResult.move(['contact'], 'sources', sourcesUrl)
            case Platform.QUILT -> PluginResult.move(['metadata', 'contact'], 'sources', sourcesUrl)
            default -> null
        }
    }

    def setLicence(final String licence) {
        if (currentPlatform == Platform.FABRIC)
            // ForgePlugin supports the "licence" alias, FabricPlugin does not
            return PluginResult.rename('license', licence)
        else if (currentPlatform == Platform.QUILT)
            return PluginResult.move(['metadata'], 'license', licence)
    }

    def setIssueTrackerUrl(final String issueTrackerUrl) {
        if (currentPlatform == Platform.FABRIC)
            return PluginResult.move(['contact'], 'issues', issueTrackerUrl)
        else if (currentPlatform == Platform.QUILT)
            return PluginResult.move(['metadata', 'contact'], 'issues', issueTrackerUrl)
    }

    def setEnvironment(final def environment) {
        if (isForgeLike(currentPlatform))
            return PluginResult.remove()
    }

    def setAccessWidener(final String accessWidener) {
        if (isForgeLike(currentPlatform))
            return PluginResult.remove()
    }

    def setIcon(final String icon) {
        if (isForgeLike(currentPlatform))
            return PluginResult.remove()
    }

    def setShowAsDataPack(final value) {
        if (isFabricLike(currentPlatform))
            return PluginResult.remove()
    }

    def setShowAsResourcePack(final value) {
        if (isFabricLike(currentPlatform))
            return PluginResult.remove()
    }

    class Icon {
        def onNestLeave(final Deque<String> stack, final Map value) {
            if (isForgeLike(currentPlatform))
                return PluginResult.remove()
        }
    }

    class Mods {
        def onNestLeave(final Deque<String> stack, final Map value) {
            if (isFabricLike(currentPlatform)) return PluginResult.remove()
        }

        class ModInfo {
            def setAuthors(final authors) {
                if (isFabricLike(currentPlatform)) {
                    if (authors instanceof List) {
                        return PluginResult.move([], authors.collect { ['name': it] })
                    } else {
                        return PluginResult.move([], authors)
                    }
                }
            }

            class Authors {
                private final List authors = []

                def onNestLeave(final Deque<String> stack, final Map value) {
                    if (isForgeLike(currentPlatform)) {
                        log.debug "authors.onNestLeave: ${value}"
                        return authors
                    }
                }

                def onNestEnter(final Deque<String> stack, final Map value) {
                    if (isForgeLike(currentPlatform)) {
                        log.debug "authors.onNestEnter: ${value}"
                        authors.clear()
                        return new PluginResult.Validate()
                    } else if (currentPlatform == Platform.FABRIC) {
                        return PluginResult.move(['authors'], value)
                    }
                }

                class Author {
                    String name

                    def onNestLeave(final Deque<String> stack, final Map value) {
                        if (isForgeLike(currentPlatform)) {
                            log.debug "authors.author.onNestLeave: ${value}"

                            if (!value.containsKey("name")) {
                                throw new PluginResult.MDGPluginException("Author name is required")
                            }
                            authors.add(value["name"])
                            return PluginResult.remove()
                        }
                    }
                }
            }

            def setAuthor(final value) {
                if (isFabricLike(currentPlatform)) {
                    return PluginResult.move([], 'authors', ['name':value])
                }
            }

            class Entrypoints {
                def onNestEnter(final Deque<String> stack, final Map value) {
                    if (isFabricLike(currentPlatform))
                        return PluginResult.move(['entrypoints'], value)
                }

                def onNestLeave(final Deque<String> stack, final Map value) {
                    if (isForgeLike(MultiplatformPlugin.this.currentPlatform))
                        return PluginResult.remove()
                }
            }

            def setModId(final String modId) {
                if (isFabricLike(currentPlatform))
                    return PluginResult.move([], 'id', modId)
            }

            def setDisplayName(final String value) {
                if (currentPlatform == Platform.FABRIC)
                    return PluginResult.move([], 'name', value)
                else if (currentPlatform == Platform.QUILT)
                    return PluginResult.move(['metadata'], 'name', value)
            }

            def setDescription(final String value) {
                if (currentPlatform == Platform.FABRIC)
                    return PluginResult.move([], 'description', value)
                else if (currentPlatform == Platform.QUILT)
                    return PluginResult.move(['metadata'], 'description', value)
            }

            def setDisplayUrl(final String value) {
                if (currentPlatform == Platform.FABRIC)
                    return PluginResult.move(['contact'], 'homepage', value)
                else if (currentPlatform == Platform.QUILT)
                    return PluginResult.move(['metadata', 'contact'], 'homepage', value)
            }

            def setIcon(final value) {
                if (currentPlatform == Platform.FABRIC)
                    return PluginResult.move([], 'icon', value)
                else if (currentPlatform == Platform.QUILT)
                    return PluginResult.move(['metadata'], 'icon', value)
                else
                    return PluginResult.remove()
            }

            class Icon {
                def onNestEnter(final Deque<String> stack, final Map value) {
                    if (currentPlatform == Platform.FABRIC)
                        return PluginResult.move(['icon'], value)
                    if (currentPlatform == Platform.QUILT)
                        return PluginResult.move(['metadata', 'icon'], value)
                }

                def onNestLeave(final Deque<String> stack, final Map value) {
                    if (isForgeLike(currentPlatform))
                        return PluginResult.remove()
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

            def set(final Deque<String> stack, final String property, final value) {
                if (isFabricLike(currentPlatform))
                    return PluginResult.move([], property, value)
                MultiplatformPlugin.this.set(stack, property, value)
            }

            class Features {
                def onNestLeave(final Deque<String> stack, final Map value) {
                    if (isFabricLike(currentPlatform))
                        return PluginResult.remove()
                }
            }
        }
    }

    private static boolean isForgeLike(Platform platform) {
        return platform == Platform.FORGE || platform == Platform.NEOFORGE
    }

    private static boolean isFabricLike(Platform platform) {
        return platform == Platform.FABRIC || platform == Platform.QUILT
    }

    @Override
    def onNestEnter(Deque<String> stack, String name, Map value) {
        if (currentPlatform == Platform.FABRIC) {
            if (stack.size() == 3 && stack.getAt(0) == "mods" && stack.getAt(1) == "modInfo") {
                return PluginResult.move([name], value)
            }
        }
        return super.onNestEnter(stack, name, value)
    }
}
