package org.groovymc.modsdotgroovy.plugin.quilt


import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.core.Logger
import org.groovymc.modsdotgroovy.core.MapTransform
import org.groovymc.modsdotgroovy.core.versioning.VersionRange
import org.groovymc.modsdotgroovy.plugin.ModsDotGroovyPlugin
import org.groovymc.modsdotgroovy.plugin.PluginResult
import org.groovymc.modsdotgroovy.plugin.PluginUtils
import org.jetbrains.annotations.Nullable

@CompileStatic
@SuppressWarnings('GroovyUnusedDeclaration') // All these methods are dynamically called by ModsDotGroovyCore
@Log4j2(category = 'MDG - QuiltPlugin')
final class QuiltPlugin extends ModsDotGroovyPlugin {
    // From https://emailregex.com/
    private static final String EMAIL_REGEX = "(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])"

    @Override
    void init(final Map<String, ?> environment) {
        log.info "Environment: ${environment}"
    }

    @Override
    Logger getLog() {
        return log
    }

    class QuiltLoader {
        PluginResult onNestLeave(final Map value) {
            log.debug "quiltLoader.onNestLeave: ${value}"
            return PluginResult.rename('quilt_loader', value)
        }

        PluginResult setGroup(final String group) {
            log.debug "group: ${group}"
            if (!group.matches(/^[a-zA-Z0-9-_.]+$/) || group.startsWith('loader.plugin'))
                throw new PluginResult.MDGPluginException('group must match the regex /^[a-zA-Z0-9-_.]+$/ and cannot start with "loader.plugin"')

            return new PluginResult.Validate()
        }

        PluginResult setId(final String id) {
            log.debug "id: ${id}"
            if (!id.matches(/^[a-z][a-z0-9-_]{3,63}$/)) {
                // if the ID is invalid, do a bunch of checks to generate a more helpful error message
                final StringBuilder errorMsg = new StringBuilder('id must match the regex /^[a-z][a-z0-9-_]{3,63}$/.').with {
                    if (id[0] < 'a' || id[0] > 'z')
                        append "\nid starts with an invalid character '${id[0]}' (it must be a lowercase a-z - uppercase isn't allowed anywhere in the ID)"
                    else if (id.length() < 2)
                        append '\nid must be at least 2 characters long.'
                    else if (id.length() > 64)
                        append '\nid cannot be longer than 64 characters.'

                    return it
                }

                throw new PluginResult.MDGPluginException(errorMsg.toString())
            }

            return new PluginResult.Validate()
        }

        class Entrypoints {
            private final List<Map> entrypoints = []

            def onNestLeave(final Map value) {
                log.debug "entrypoints.onNestLeave: ${value}"
                Map<String, List> entrypointsByType = [:]
                entrypoints.each { Map val ->
                    List list = entrypointsByType.computeIfAbsent(val["type"] as String, { [] })
                    if (val["adapter"] == null) {
                        list.add(val["value"])
                    } else {
                        list.add([
                                adapter: val["adapter"],
                                value: val["value"]
                        ])
                    }
                }
                return PluginResult.rename('entrypoints', entrypointsByType)
            }

            class Entrypoint {
                String type
                String adapter
                String value

                def onNestLeave(final Map value) {
                    log.debug "entrypoints.entrypoint.onNestLeave: ${value}"
                    if (value["replace"] === true) {
                        entrypoints.removeIf { it["type"] == type }
                    }
                    entrypoints.add(value)
                    return PluginResult.remove()
                }
            }
        }

        class Plugins {
            PluginResult onNestLeave(final Map value) {
                log.debug "plugins.onNestLeave: ${value}"
                return new PluginResult.Validate()
            }
        }

        class Jars {
            private final List jars = []

            def onNestLeave(final Map value) {
                log.debug "jars.onNestLeave: ${value}"
                return jars
            }

            class Jar {
                String file

                PluginResult onNestLeave(final Map value) {
                    log.debug "jars.jar.onNestLeave: ${value}"
                    if (!file.contains('.'))
                        throw new PluginResult.MDGPluginException("jar ${file} is missing a file extension. Did you forget to put \".jar\" at the end?")
                    jars.add(value)
                    return PluginResult.remove()
                }
            }
        }

        class LanguageAdapters {
            PluginResult onNestLeave(final Map value) {
                log.debug "languageAdapters.onNestLeave: ${value}"
                return PluginResult.rename('language_adapters', value)
            }
        }

        class Depends {
            Mod mod = new Mod()

            def onNestLeave(final Map value) {
                log.info "depends.onNestLeave: ${value}"
                return mod.mods
            }

            def set(final String property, final value) {
                Map map = [:]
                map.put("id", property)
                def versions = handleVersionRange(value)
                map.put("versions", versions)
                mod.mods.add(map)
                return PluginResult.remove()
            }
        }

        class Breaks {
            Mod mod = new Mod()

            def onNestLeave(final Map value) {
                log.info "breaks.onNestLeave: ${value}"
                return mod.mods
            }

            def set(final String property, final value) {
                Map map = [:]
                map.put("id", property)
                def versions = handleVersionRange(value)
                map.put("versions", versions)
                mod.mods.add(map)
                return PluginResult.remove()
            }
        }

        class Provides {
            private final List provides = []

            def onNestLeave(final Map value) {
                log.info "provides.onNestLeave: ${value}"
                return PluginResult.rename('provides', provides)
            }

            class ProvidesEntry {
                String id

                def onNestLeave(final Map value) {
                    log.info "provides.providesEntry.onNestLeave: ${value}"
                    if (id === null)
                        throw new PluginResult.MDGPluginException("Provides entry is missing an id.")
                    else
                        provides.add(value)
                    return PluginResult.remove()
                }
            }
        }

        PluginResult setLoadType(final loadType) {
            log.debug "loadType: ${loadType}"
            return PluginResult.rename('load_type', loadType.toString().toLowerCase(Locale.ROOT))
        }

        PluginResult setIntermediateMappings(final String intermediateMappings) {
            log.debug "intermediateMappings: ${intermediateMappings}"
            if (!intermediateMappings.matches(/^[a-zA-Z0-9-_.]+:[a-zA-Z0-9-_.]+$/))
                throw new PluginResult.MDGPluginException('intermediateMappings must match the regex /^[a-zA-Z0-9-_.]+:[a-zA-Z0-9-_.]+$/')

            return PluginResult.rename('intermediate_mappings', intermediateMappings)
        }

        class Metadata {
            class Contributors {
                Map contributors = [:]

                Map onNestLeave(final Map value) {
                    log.debug "contributors.onNestLeave: ${value}"
                    contributors.putAll(value)
                    return contributors
                }

                class Contributor {
                    def name
                    def role

                    def onNestLeave(final Map value) {
                        contributors.put(name, role)
                        return PluginResult.remove()
                    }
                }
            }

            class License {
                def onNestLeave(final value) {
                    return PluginResult.move(['quiltLoader', 'metadata', 'licenses', 'license'], value, true)
                }
            }

            class Licenses {
                final List licenses = []

                def setLicense(final value) {
                    licenses.add(value)
                    return PluginResult.remove()
                }

                def onNestLeave(final Map value) {
                    return PluginResult.rename('license', licenses)
                }

                class License {
                    def onNestLeave(final Map value) {
                        licenses.add(value)
                        return PluginResult.remove()
                    }

                    def setUrl(final String url) {
                        log.debug "license.url: ${url}"
                        if (!PluginUtils.isValidHttpUrl(url))
                            throw new PluginResult.MDGPluginException('license url must start with http:// or https://')
                    }
                }
            }

            class Contact {
                void setEmail(final String email) {
                    log.debug "contact.email: ${email}"
                    if (!email.matches(EMAIL_REGEX))
                        throw new PluginResult.MDGPluginException('Invalid contact email.')
                }

                void setHomepage(final String homepage) {
                    log.debug "contact.homepage: ${homepage}"
                    if (!PluginUtils.isValidHttpUrl(homepage))
                        throw new PluginResult.MDGPluginException('homepage must start with http:// or https://')
                }

                void setIssues(final String issues) {
                    log.debug "contact.issues: ${issues}"
                    if (!PluginUtils.isValidHttpUrl(issues))
                        throw new PluginResult.MDGPluginException('issues must start with http:// or https://')
                }

                void setSources(final String sources) {
                    log.debug "contact.issues: ${sources}"
                    if (!PluginUtils.isValidHttpUrl(sources))
                        throw new PluginResult.MDGPluginException('sources must start with http:// or https://')
                }
            }

            void setIcon(final String icon) {
                log.debug "icon: ${icon}"
                if (!icon.contains('.'))
                    throw new PluginResult.MDGPluginException('icon is missing a file extension. Did you forget to put ".png" at the end?')
            }

            class Icon {
                private final Map icons = [:]

                Map onNestLeave(final Map value) {
                    log.debug "icon.onNestLeave: ${value}"
                    value.each { key, val ->
                        if (!val.toString().contains('.'))
                            throw new PluginResult.MDGPluginException("icon of size ${key} is missing a file extension. Did you forget to put \".png\" at the end?")
                    }
                    icons.putAll(value)
                    return icons
                }
            }
        }
    }

    private static def handleVersionRange(final value) {
        if (value instanceof String || value instanceof GString) {
            return VersionRange.of(value as String)
        }
        return value
    }

    class Mod {
        private final List<Map> mods = []

        String id

        def setVersion(final value) {
            return PluginResult.rename('versions', setVersions(value))
        }

        def setVersions(def value) {
            return handleVersionRange(value)
        }

        def onNestLeave(final Map value) {
            log.info "mod.onNestLeave: ${value}"

            if (id == null) {
                throw new PluginResult.MDGPluginException("id is required in a mod block")
            }

            String oldId = id
            this.id = null

            Map map = [id:oldId]
            map.putAll(value)
            mods.add(map)
            return PluginResult.remove()
        }
    }

    PluginResult setSchemaVersion(final int schemaVersion) {
        log.debug "schemaVersion: ${schemaVersion}"
        if (schemaVersion !== 1)
            throw new PluginResult.MDGPluginException('schemaVersion must be 1')

        return PluginResult.rename('schema_version', schemaVersion)
    }

    PluginResult setAccessWidener(final String accessWidener) {
        log.debug "accessWidener: ${accessWidener}"
        return PluginResult.rename('access_widener', accessWidener)
    }

    PluginResult setAccessWidener(final List<String> accessWidener) {
        log.debug "accessWidener: ${accessWidener}"
        return PluginResult.rename('access_widener', accessWidener)
    }

    private Object makeVersionMap(VersionRange range) {
        if (range instanceof VersionRange.SingleVersionRange) {
            var parts = range.version.toSemver().split(' ')
            if (parts.length === 1) {
                return parts[0]
            } else {
                return [
                        'all': parts.toList()
                ]
            }
        } else if (range instanceof VersionRange.OrVersionRange) {
            return [
                    'any': range.versions.collect { makeVersionMap(it) }
            ]
        } else if (range instanceof VersionRange.AndVersionRange) {
            return [
                    'all': range.versions.collect { makeVersionMap(it) }
            ]
        } else {
            return range.toSemver()
        }
    }

    @Override
    List<MapTransform> mapTransforms() {
        return [MapTransform.of(VersionRange, {
            makeVersionMap(it)
        })]
    }

    @Override
    @Nullable
    Map build(Map buildingMap) {
        return [
                schema_version: 1,
                quilt_loader: [
                        intermediate_mappings: "net.fabricmc:intermediary"
                ]
        ]
    }
}
