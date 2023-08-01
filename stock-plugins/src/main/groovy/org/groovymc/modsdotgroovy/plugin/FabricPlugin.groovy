package org.groovymc.modsdotgroovy.plugin

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.core.Logger
import org.groovymc.modsdotgroovy.core.Platform

@CompileStatic
@SuppressWarnings('GroovyUnusedDeclaration') // All these methods are dynamically called by ModsDotGroovyCore
@Log4j2(category = 'MDG - FabricPlugin')
class FabricPlugin extends ModsDotGroovyPlugin {

    @Override
    void init(final Map<String, ?> environment) {
        log.info "Environment: ${environment}"
    }

    @Override
    Logger getLog() {
        return log
    }

    @Override
    EnumSet<Platform> getPlatforms() {
        return EnumSet.of(Platform.FABRIC)
    }

    @CompileDynamic
    PluginResult setEnvironment(final def environment) {
        log.debug "environment: ${environment}"
        return PluginResult.of(environment.value)
    }

    @Override
    Map build(Map buildingMap) {
        return !buildingMap.containsKey("license") ? [license: "All Rights Reserved"] : [:]
    }

    class Entrypoints {
        private final List<Map> entrypoints = []

        def onNestLeave(final Deque<String> stack, final Map value) {
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

        def onNestEnter(final Deque<String> stack, final Map value) {
            log.debug "entrypoints.onNestEnter: ${value}"
            entrypoints.clear()
            return new PluginResult.Validate()
        }

        class Entrypoint {
            String type
            String adapter
            String value
            
            def onNestLeave(final Deque<String> stack, final Map value) {
                log.debug "entrypoints.entrypoint.onNestLeave: ${value}"
                entrypoints.add(value)
                return PluginResult.remove()
            }
        }
    }

    class Jars {
        private final List jars = []

        def onNestLeave(final Deque<String> stack, final Map value) {
            log.debug "jars.onNestLeave: ${value}"
            return jars
        }

        def onNestEnter(final Deque<String> stack, final Map value) {
            log.debug "jars.onNestEnter: ${value}"
            jars.clear()
            return new PluginResult.Validate()
        }

        class Jar {
            String file

            PluginResult onNestLeave(final Deque<String> stack, final Map value) {
                log.debug "jars.jar.onNestLeave: ${value}"
                jars.add(value)
                return PluginResult.remove()
            }
        }
    }

    class Mixins {
        private final List mixins = []

        def onNestLeave(final Deque<String> stack, final Map value) {
            log.debug "mixins.onNestLeave: ${value}"
            return mixins
        }

        def onNestEnter(final Deque<String> stack, final Map value) {
            log.debug "mixins.onNestEnter: ${value}"
            mixins.clear()
            return new PluginResult.Validate()
        }

        class Mixin {
            String config
            def environment

            PluginResult onNestLeave(final Deque<String> stack, final Map value) {
                log.debug "mixins.mixin.onNestLeave: ${value}"
                if (value["config"] != null && value.size() == 1) {
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

        def onNestLeave(final Deque<String> stack, final Map value) {
            log.debug "authors.onNestLeave: ${value}"
            return authors
        }

        def onNestEnter(final Deque<String> stack, final Map value) {
            log.debug "authors.onNestEnter: ${value}"
            authors.clear()
            return new PluginResult.Validate()
        }

        class Author {
            String name
            def contact

            PluginResult onNestLeave(final Deque<String> stack, final Map value) {
                log.debug "authors.author.onNestLeave: ${value}"

                if (!value.containsKey("name")) {
                    throw new PluginResult.MDGPluginException("Author name is required")
                } else if (value.size() == 1) {
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

        def onNestLeave(final Deque<String> stack, final Map value) {
            log.debug "contributors.onNestLeave: ${value}"
            return contributors
        }

        def onNestEnter(final Deque<String> stack, final Map value) {
            log.debug "contributors.onNestEnter: ${value}"
            contributors.clear()
            return new PluginResult.Validate()
        }

        class Contributor {
            String name
            def contact

            PluginResult onNestLeave(final Deque<String> stack, final Map value) {
                log.debug "contributors.contributor.onNestLeave: ${value}"
                if (!value.containsKey("name")) {
                    throw new PluginResult.MDGPluginException("Contributor name is required")
                } else if (value.size() == 1) {
                    contributors.add(value["name"])
                } else {
                    contributors.add(value)
                }
                return PluginResult.remove()
            }
        }
    }
}
