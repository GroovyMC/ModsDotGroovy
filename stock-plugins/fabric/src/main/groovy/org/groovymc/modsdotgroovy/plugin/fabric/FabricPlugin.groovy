package org.groovymc.modsdotgroovy.plugin.fabric

import groovy.transform.CompileDynamic
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
@Log4j2(category = 'MDG - FabricPlugin')
final class FabricPlugin extends ModsDotGroovyPlugin {
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

    @CompileDynamic
    PluginResult setEnvironment(final def environment) {
        log.debug "environment: ${environment}"
        return PluginResult.of(environment.value)
    }

    PluginResult setId(final String id) {
        log.debug "id: ${id}"
        // See https://github.com/FabricMC/fabric-loader/blob/67bf49ca39e313aeb3476bb6015352fd5284d49b/src/main/java/net/fabricmc/loader/impl/metadata/MetadataVerifier.java#L36
        if (!id.matches("[a-z][a-z0-9-_]{1,63}")) {
            // if the id is invalid, do a bunch of checks to generate a more helpful error message
            final StringBuilder errorMsg = new StringBuilder('id must match the regex /^[a-z][a-z0-9_]{3,63}$/.').with {
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

    void setIcon(final String icon) {
        log.debug "icon: ${icon}"
        if (!icon.contains('.'))
            throw new PluginResult.MDGPluginException('icon is missing a file extension. Did you forget to put ".png" at the end?')
    }

    class Icon {
        private final Map icons = [:]

        def onNestLeave(final Map value) {
            log.debug "icon.onNestLeave: ${value}"
            value.each { key, val ->
                if (!val.toString().contains('.'))
                    throw new PluginResult.MDGPluginException("icon of size ${key} is missing a file extension. Did you forget to put \".png\" at the end?")
            }
            icons.putAll(value)
            return icons
        }
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
            return entrypointsByType
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

    class Depends {
        Mod mod = new Mod()

        def set(final String property, final value) {
            return PluginResult.rename(property, handleVersionRange(value))
        }
    }

    class Recommends {
        Mod mod = new Mod()

        def set(final String property, final value) {
            return PluginResult.change(handleVersionRange(value))
        }
    }

    class Suggests {
        Mod mod = new Mod()

        def set(final String property, final value) {
            return PluginResult.change(handleVersionRange(value))
        }
    }

    class Breaks {
        Mod mod = new Mod()

        def set(final String property, final value) {
            return PluginResult.change(handleVersionRange(value))
        }
    }

    class Conflicts {
        Mod mod = new Mod()

        def set(final String property, final value) {
            return PluginResult.change(handleVersionRange(value))
        }
    }

    class Mod {
        String modId

        def setVersionRange(value) {
            return handleVersionRange(value)
        }

        PluginResult onNestLeave(final Map value) {
            log.info "mod.onNestLeave: ${value}"

            if (modId == null) {
                throw new PluginResult.MDGPluginException("modId is required in a mod block")
            }

            String oldModId = modId
            this.modId = null

            return PluginResult.rename(oldModId, value.versionRange)
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

    class Mixins {
        private final List mixins = []

        def onNestLeave(final Map value) {
            log.debug "mixins.onNestLeave: ${value}"
            return mixins
        }

        def onNestEnter(final Map value) {
            log.debug "mixins.onNestEnter: ${value}"
            mixins.clear()
            return new PluginResult.Validate()
        }

        class Mixin {
            String config
            def environment

            PluginResult onNestLeave(final Map value) {
                log.debug "mixins.mixin.onNestLeave: ${value}"
                if (value["config"] != null && value.size() === 1) {
                    mixins.add(value["config"])
                } else {
                    mixins.add(value)
                }
                return PluginResult.remove()
            }

            @CompileDynamic
            PluginResult setEnvironment(final def environment) {
                log.debug "mixins.mixin.environment: ${environment}"
                return PluginResult.of(environment.value)
            }
        }
    }

    class Authors {
        private final List authors = []

        def onNestLeave(final Map value) {
            log.debug "authors.onNestLeave: ${value}"
            return authors
        }

        class Author {
            String name
            Contact contact = new Contact()

            PluginResult onNestLeave(final Map value) {
                log.debug "authors.author.onNestLeave: ${value}"

                if (!value.containsKey("name")) {
                    throw new PluginResult.MDGPluginException("Author name is required")
                } else if (value.size() === 1) {
                    authors.add(value["name"])
                } else {
                    authors.add(value)
                }
                return PluginResult.remove()
            }
        }
    }

    class Contributors {
        private final List contributors = []

        def onNestLeave(final Map value) {
            log.debug "contributors.onNestLeave: ${value}"
            return contributors
        }

        class Contributor {
            String name
            Contact contact = new Contact()

            PluginResult onNestLeave(final Map value) {
                log.debug "contributors.contributor.onNestLeave: ${value}"
                if (!value.containsKey("name")) {
                    throw new PluginResult.MDGPluginException("Contributor name is required")
                } else if (value.size() === 1) {
                    contributors.add(value["name"])
                } else {
                    contributors.add(value)
                }
                return PluginResult.remove()
            }
        }
    }

    @Override
    @Nullable
    Map build(Map buildingMap) {
        return [schemaVersion: 1]
    }

    private static def handleVersionRange(final Object value) {
        if (value instanceof String || value instanceof GString) {
            return VersionRange.of(value.toString())
        } else if (value instanceof List) {
            return new VersionRange.OrVersionRange(value.collect { (it instanceof String || it instanceof GString) ? VersionRange.of(it as String) : it as VersionRange })
        }
        return value
    }

    @Override
    List<MapTransform> mapTransforms() {
        return [MapTransform.of(VersionRange, {
            var vs = it.toSemver()
            if (vs.size() === 1) return vs.get(0)
            return vs
        })]
    }
}
