package org.groovymc.modsdotgroovy.plugin

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.core.Logger
import org.jetbrains.annotations.Nullable

@CompileStatic
@SuppressWarnings('GroovyUnusedDeclaration') // All these methods are dynamically called by ModsDotGroovyCore
@Log4j2(category = 'MDG - QuiltPlugin')
class QuiltPlugin extends ModsDotGroovyPlugin {
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

    PluginResult setGroup(final String group) {
        log.debug "group: ${group}"
        if (!group.matches(/^[a-zA-Z0-9-_.]+$/) || group.startsWith('loader.plugin'))
            throw new PluginResult.MDGPluginException('group must match the regex /^[a-zA-Z0-9-_.]+$/ and cannot start with "loader.plugin"')

        return PluginResult.move(['quilt_loader'], group)
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

        return PluginResult.move(['quilt_loader'], id)
    }

    PluginResult setVersion(final String version) {
        log.debug "version: ${version}"
        return PluginResult.move(['quilt_loader'], version)
    }

    class Entrypoints {
        // todo

        PluginResult onNestLeave(final Deque<String> stack, final Map value) {
            log.debug "entrypoints.onNestLeave: ${value}"
            return PluginResult.move(['quilt_loader', 'entrypoints'], value)
        }
    }

    class Plugins {
        // todo

        PluginResult onNestLeave(final Deque<String> stack, final Map value) {
            log.debug "plugins.onNestLeave: ${value}"
            return PluginResult.move(['quilt_loader', 'plugins'], value)
        }
    }

    class Jars {
        private final List jars = []

        PluginResult onNestLeave(final Deque<String> stack, final Map value) {
            log.debug "jars.onNestLeave: ${value}"
            return PluginResult.move(['quilt_loader', 'jars'], jars)
        }

        void onNestEnter(final Deque<String> stack, final Map value) {
            log.debug "jars.onNestEnter: ${value}"
            jars.clear()
        }

        class Jar {
            String file

            PluginResult onNestLeave(final Deque<String> stack, final Map value) {
                log.debug "jars.jar.onNestLeave: ${value}"
                if (!file.contains('.'))
                    throw new PluginResult.MDGPluginException("jar ${file} is missing a file extension. Did you forget to put \".jar\" at the end?")
                jars.add(value)
                return PluginResult.remove()
            }
        }
    }

    class LanguageAdapters {
        // todo

        PluginResult onNestLeave(final Deque<String> stack, final Map value) {
            log.debug "languageAdapters.onNestLeave: ${value}"
            return PluginResult.move(['quilt_loader', 'language_adapters'], value)
        }
    }

    class Depends {
        Mod mod = new Mod()

        PluginResult onNestLeave(final Deque<String> stack, final Map value) {
            log.info "depends.onNestLeave: ${value}"
            return PluginResult.move(['quilt_loader', 'depends'], value)
        }
    }

    class Breaks {
        Mod mod = new Mod()

        PluginResult onNestLeave(final Deque<String> stack, final Map value) {
            log.info "breaks.onNestLeave: ${value}"
            return PluginResult.move(['quilt_loader', 'breaks'], value)
        }
    }

    class Mod {
        String id
        def versions

        PluginResult onNestLeave(final Deque<String> stack, final Map value) {
            log.info "mod.onNestLeave: ${value}"
            return PluginResult.rename(id, versions)
        }
    }

    PluginResult setLoadType(final String loadType) {
        log.debug "loadType: ${loadType}"
        return PluginResult.move(['quilt_loader'], loadType.toLowerCase(Locale.ROOT))
    }

    PluginResult setRepositories(final List<String> repositories) {
        log.debug "repositories: ${repositories}"
        return PluginResult.move(['quilt_loader'], repositories)
    }

    PluginResult setIntermediateMappings(final String intermediateMappings) {
        log.debug "intermediateMappings: ${intermediateMappings}"
        if (!intermediateMappings.matches(/^[a-zA-Z0-9-_.]+:[a-zA-Z0-9-_.]+$/))
            throw new PluginResult.MDGPluginException('intermediateMappings must match the regex /^[a-zA-Z0-9-_.]+:[a-zA-Z0-9-_.]+$/')

        return PluginResult.move(['quilt_loader'], intermediateMappings)
    }

    class Metadata {
        // todo: contributors

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

            Map onNestLeave(final Deque<String> stack, final Map value) {
                log.debug "icon.onNestLeave: ${value}"
                value.each { key, val ->
                    if (!val.toString().contains('.'))
                        throw new PluginResult.MDGPluginException("icon of size ${key} is missing a file extension. Did you forget to put \".png\" at the end?")
                }
                icons.putAll(value)
                return icons
            }

            PluginResult onNestEnter(final Deque<String> stack, final Map value) {
                log.debug "icon.onNestEnter: ${value}"
                return new PluginResult.Validate()
            }
        }

        PluginResult onNestLeave(final Deque<String> stack, final Map value) {
            log.debug "metadata.onNestLeave: ${value}"
            return PluginResult.move(['quilt_loader', 'metadata'], value)
        }
    }

    PluginResult setMixin(final String mixin) {
        log.debug "mixin: ${mixin}"
        return PluginResult.move(['quilt_loader'], mixin)
    }

    PluginResult setMixin(final List<String> mixin) {
        log.debug "mixin: ${mixin}"
        return PluginResult.move(['quilt_loader'], mixin)
    }

    PluginResult setAccessWidener(final String accessWidener) {
        log.debug "accessWidener: ${accessWidener}"
        return PluginResult.move(['quilt_loader'], accessWidener)
    }

    PluginResult setAccessWidener(final List<String> accessWidener) {
        log.debug "accessWidener: ${accessWidener}"
        return PluginResult.move(['quilt_loader'], accessWidener)
    }

    class Minecraft {
        @CompileDynamic
        PluginResult setEnvironment(final def environment) {
            log.debug "environment: ${environment}"
            return PluginResult.move(['quilt_loader', 'minecraft'], environment.value)
        }
    }

    @Override
    @Nullable
    Map build(Map buildingMap) {
        return [schemaVersion: 1]
    }
}
