package org.groovymc.modsdotgroovy.plugin.forgelike

import groovy.json.JsonParserType
import groovy.json.JsonSlurper
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

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.regex.Matcher

@CompileStatic
@SuppressWarnings('GroovyUnusedDeclaration') // All these methods are dynamically called by ModsDotGroovyCore
@Log4j2(category = 'MDG - ForgeLikePlugin')
final class ForgeLikePlugin extends ModsDotGroovyPlugin {
    @Nullable String issueTrackerUrl

    @Override
    void init(final Map<String, ?> environment) {
        log.info "Environment: ${environment}"
    }

    // note: void methods are executed and treated as PluginResult.VALIDATE
    void setModLoader(final String modLoader) {
        log.debug "modLoader: ${modLoader}"
        if (PluginUtils.startsWithNumber(modLoader))
            throw new PluginResult.MDGPluginException('modLoader must not start with a number.')
    }

    def setVersionRange(def value) {
        log.debug "loaderVersion: ${value}"
        return handleVersionRange(value)
    }

    void setLicense(final String license) {
        log.debug "license: ${license}"
        if (license.isBlank())
            throw new PluginResult.MDGPluginException('license cannot be set to a blank string. Omit the setter altogether if you don\'t want to specify a license.')
    }

    /** Support British spelling as an alias */
    PluginResult setLicence(final String licence) {
        log.debug "licence: ${licence}"
        return new PluginResult.Rename(newPropertyName: 'license', newValue: licence, reentrant: true)
    }

    PluginResult setIssueTrackerUrl(final String issueTrackerUrl) {
        log.debug "issueTrackerUrl: ${issueTrackerUrl}"
        if (!PluginUtils.isValidHttpUrl(issueTrackerUrl))
            throw new PluginResult.MDGPluginException('issueTrackerUrl must start with http:// or https://')

        this.issueTrackerUrl = issueTrackerUrl

        return PluginResult.rename('issueTrackerURL', issueTrackerUrl)
    }

    class Mods {
        private final List modInfos = []

        def onNestLeave(final Map value) {
            log.debug "mods.onNestLeave: ${value}"
            return modInfos
        }

        class ModInfo {
            @Nullable String modId = null
            @Nullable String displayUrl = null
            @Nullable String updateJsonUrl = null

            PluginResult onNestLeave(final Map value) {
                log.debug "mods.modInfo.onNestLeave"

                if (updateJsonUrl === null || updateJsonUrl.isBlank())
                    value['updateJSONURL'] = inferUpdateJsonUrl(modId, displayUrl, issueTrackerUrl)

                modInfos.add(value)
                return PluginResult.remove()
            }

            def setModId(final String modId) {
                log.debug "mods.modInfo.modId: ${modId}"

                // validate the modId string
                // https://github.com/MinecraftForge/MinecraftForge/blob/4b813e4319fbd4e7f1ea2a7edaedc82ba617f797/fmlloader/src/main/java/net/minecraftforge/fml/loading/moddiscovery/ModInfo.java#L32
                if (!modId.matches(/^[a-z][a-z0-9_]{1,63}\u0024/)) {
                    // if the modId is invalid, do a bunch of checks to generate a more helpful error message
                    final StringBuilder errorMsg = new StringBuilder('modId must match the regex /^[a-z][a-z0-9_]{1,63}$/.').with {
                        if (modId.contains('-') || modId.contains(' '))
                            append '\nDashes and spaces are not allowed in modId as per the JPMS spec. Use underscores instead.'
                        if (PluginUtils.startsWithNumber(modId))
                            append '\nmodId cannot start with a number.'
                        if (modId != modId.toLowerCase(Locale.ROOT))
                            append '\nmodId must be lowercase.'

                        if (modId.length() > 64)
                            append '\nmodId cannot be longer than 64 characters.'

                        return it
                    }

                    throw new PluginResult.MDGPluginException(errorMsg.toString())
                } else if (modId.length() < 4) {
                    log.warn 'modId should be at least 4 characters long to avoid conflicts.'
                }
                this.modId = modId
                return new PluginResult.Validate()
            }

            PluginResult setAuthor(final String author) {
                log.debug "mods.modInfo.author: ${author}"
                return PluginResult.rename('authors', author)
            }

            def setAuthors(final def authors) {
                log.debug "mods.modInfo.authors: ${authors}"
                if (authors instanceof List) {
                    if (authors.size() === 1) {
                        return authors[0]
                    } else {
                        def parts = authors.collect()
                        parts[parts.size() - 1] = "and " + parts[parts.size() - 1]
                        return authors.size() === 2 ? parts.join(' ') : parts.join(', ')
                    }
                }
            }

            void setLogoFile(final String logoFile) {
                log.debug "mods.modInfo.logoFile: ${logoFile}"
                if (!logoFile.contains('.'))
                    throw new PluginResult.MDGPluginException('logoFile is missing a file extension. Did you forget to put ".png" at the end?')
            }

            PluginResult setDisplayUrl(final String displayUrl) {
                log.debug "mods.modInfo.displayUrl: ${displayUrl}"
                this.displayUrl = displayUrl
                return PluginResult.rename('displayURL', displayUrl)
            }

            PluginResult setUpdateJsonUrl(final String updateJsonUrl) {
                log.debug "mods.modInfo.updateJsonUrl: ${updateJsonUrl}"
                if (!PluginUtils.isValidHttpUrl(updateJsonUrl))
                    throw new PluginResult.MDGPluginException('updateJsonUrl must start with http:// or https://')

                this.updateJsonUrl = updateJsonUrl
                return PluginResult.rename('updateJSONURL', updateJsonUrl)
            }

            class Dependencies {
                private final List dependencies = []

                PluginResult onNestLeave(final Map value) {
                    if (ModInfo.this.modId === null)
                        throw new PluginResult.MDGPluginException('modId must be set before dependencies can be set.')
                    log.debug "mods.modInfo.dependencies.onNestLeave"
                    return PluginResult.move(['dependencies', ModInfo.this.modId], dependencies)
                }

                class Dependency {
                    @Nullable String modId = null

                    def setVersionRange(def value) {
                        log.debug "mods.modInfo.dependencies.dependency.versionRange: ${value}"
                        return handleVersionRange(value)
                    }

                    PluginResult onNestLeave(final Map value) {
                        log.debug "mods.modInfo.dependencies.dependency.onNestLeave"
                        if (this.modId === null)
                            throw new PluginResult.MDGPluginException('dependency is missing a modId')

                        if (value.versionRange === null)
                            throw new PluginResult.MDGPluginException("dependency \"${this.modId}\" is missing a versionRange")

                        dependencies.add(value)
                        this.modId = null
                        return PluginResult.remove()
                    }
                }
            }

            class Features {
                def onNestLeave(final Map value) {
                    log.debug "mods.modInfo.features.onNestEnter: ${value}"
                    if (ModInfo.this.modId === null)
                        throw new PluginResult.MDGPluginException('modId must be set before features can be set.')
                    return PluginResult.move(['features', ModInfo.this.modId], value)
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
    def set(final List<String> stack, final String name, def value) {
        log.debug "set(name: $name, value: $value)"

        return new PluginResult.Unhandled()
    }

    @Override
    @CompileDynamic
    def onNestLeave(final List<String> stack, Map value) {
        log.debug "onNestLeave(name: ${stack.last()}, value: $value)"
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

    @Nullable
    private static String inferUpdateJsonUrl(final String modId, @Nullable final String displayUrl, @Nullable final String issueTrackerUrl) {
        final String displayOrIssueTrackerUrl = displayUrl ?: issueTrackerUrl ?: ''
        if (displayOrIssueTrackerUrl.isBlank())
            return null

        @Nullable
        HttpClient httpClient = null

        // determine if the displayUrl is a CurseForge URL and if so, extract the project ID and slug from it if possible
        // and use those to construct an updateJsonUrl using forge.curseupdate.com
        // example in: https://www.curseforge.com/minecraft/mc-mods/spammycombat?projectId=623297
        // example out: 623297/spammycombat
        final Matcher cfMatcher = displayOrIssueTrackerUrl =~ $/.*curseforge.com/minecraft/mc-mods/([^?/]+)(?:\?projectId=(\d+))?/$
        if (cfMatcher.matches()) {
            httpClient ?= HttpClient.newBuilder().build()
            final jsonSlurper = new JsonSlurper().setType(JsonParserType.INDEX_OVERLAY)

            List<String> updateJsonUrls = []
            if (cfMatcher.groupCount() === 3) { // whole string, slug, project ID
                // determine the modId first from the modId, falling back to the slug in the URL if that fails
                final String updateJsonUrlRoot = "https://forge.curseupdate.com/${cfMatcher.group(2)}/"
                updateJsonUrls.add(updateJsonUrlRoot + modId)
                updateJsonUrls.add(updateJsonUrlRoot + cfMatcher.group(1))
            } else { // Whole string, slug
                // Determine the projectId based on the slug
                final String slug = cfMatcher.group(1)
                try {
                    final response = httpClient.send(
                            HttpRequest.newBuilder(URI.create("https://api.cfwidget.com/minecraft/mc-mods/$slug")).GET().build(),
                            HttpResponse.BodyHandlers.ofString()
                    )

                    if (response.statusCode() === HttpURLConnection.HTTP_OK) {
                        final Integer pId = jsonSlurper.parseText(response.body())['id'] as Integer
                        if (pId !== null) {
                            updateJsonUrls.add("https://forge.curseupdate.com/$pId/$slug".toString())
                        }
                    }
                } catch (IOException ignored) {}
            }

            // make a GET request to the updateJsonUrl and to see if it's valid
            for (final String url in updateJsonUrls) {
                final HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build()
                try {
                    final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                    if (response.statusCode() === HttpURLConnection.HTTP_OK) {
                        final json = jsonSlurper.parseText(response.body())
                        if (!(json?.getAt('promos') as Map).isEmpty())
                            return url
                    }
                } catch (final IOException ignored) {}
            }
        }

        // determine if the displayUrl is a GitHub URL and if so, see if it has an updates.json file next to the mods.groovy
        // example in: https://github.com/PaintNinja/Ninjas-Cash
        // example out: https://raw.githubusercontent.com/PaintNinja/Ninjas-Cash/master/src/main/resources/updates.json
        final Matcher ghMatcher = displayOrIssueTrackerUrl =~ $/.*github.com/([^?/]+)/([^?/]+)(?:/tree/([^?/]+))?/?/$
        if (ghMatcher.matches()) {
            httpClient ?= HttpClient.newBuilder().build()
            // it's a GitHub URL, so let's see if it has an update.json file on the repo in any of the standard locations
            final String updateJsonUrlRoot = "https://raw.githubusercontent.com/${ghMatcher.group(1)}/${ghMatcher.group(2)}/${ghMatcher.group(3) ?: 'master'}"
            final List updateJsonUrls = [
                    "${updateJsonUrlRoot}/src/main/resources/update.json",
                    "${updateJsonUrlRoot}/src/main/resources/updates.json",
                    "${updateJsonUrlRoot}/update.json",
                    "${updateJsonUrlRoot}/updates.json"
            ]
            if (updateJsonUrlRoot.endsWith('master')) {
                final String updateJsonUrlMainBranchRoot = updateJsonUrlRoot.substring(0, updateJsonUrlRoot.length() - 6) + 'main'
                updateJsonUrls.addAll([
                        "${updateJsonUrlMainBranchRoot}/src/main/resources/update.json",
                        "${updateJsonUrlMainBranchRoot}/src/main/resources/updates.json",
                        "${updateJsonUrlMainBranchRoot}/update.json",
                        "${updateJsonUrlMainBranchRoot}/updates.json"
                ])
            }

            // make a HEAD request to the updateJsonUrl to see if it exists using HttpClient
            for (final String url in updateJsonUrls) {
                final HttpRequest request = HttpRequest.newBuilder(URI.create(url)).method('HEAD', HttpRequest.BodyPublishers.noBody()).build()
                try {
                    final HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding())
                    if (response.statusCode() === HttpURLConnection.HTTP_OK)
                        return url
                } catch (final IOException ignored) {}
            }
        }

        return null
    }

    private static def handleVersionRange(final Object value) {
        if (value instanceof String || value instanceof GString) {
            return VersionRange.of(value.toString())
        }
        return value
    }

    @Override
    List<MapTransform> mapTransforms() {
        return [MapTransform.of(VersionRange, { it.toMaven() })]
    }
}
