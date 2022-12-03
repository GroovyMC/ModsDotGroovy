/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

//file:noinspection GrMethodMayBeStatic

import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovyjarjarantlr4.v4.runtime.misc.Nullable
import modsdotgroovy.ImmutableModInfo
import modsdotgroovy.ModInfoBuilder
import modsdotgroovy.ModsBuilder
import modsdotgroovy.VersionRange

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.regex.Matcher

import static groovy.lang.Closure.DELEGATE_FIRST

@CompileStatic
@SuppressWarnings('unused')
class ModsDotGroovy {
    protected Map data

    protected ModsDotGroovy() {
        this.data = switch (platform) {
            case Platform.QUILT -> ["schema_version": 1, "quilt_loader": [:]]
            case Platform.FORGE -> [:]
        }
    }

    protected static Platform platform

    protected static void setPlatform(String name) {
        platform = Platform.valueOf(name.toUpperCase(Locale.ROOT))
    }

    void propertyMissing(String name, Object value) {
        put(name, value)
    }

    void put(String name, Object value) {
        data[name] = value
    }

    /**
     * Run a given block only if the plugin is configuring the mods.toml file for forge.
     */
    void onForge(Closure closure) {
        if (platform === Platform.FORGE) {
            closure.resolveStrategy = DELEGATE_FIRST
            closure.call()
        }
    }

    /**
     * Run a given block only if the plugin is configuring the quilt.mod.json file for quilt.
     */
    void onQuilt(Closure closure) {
        if (platform == Platform.QUILT) {
            closure.resolveStrategy = DELEGATE_FIRST
            closure.call()
        }
    }

    /**
     * The license for your mod. This is mandatory metadata and allows for easier comprehension of your redistributive properties.<br>
     * Review your options at <a href="https://choosealicense.com/">https://choosealicense.com/</a>. <br>
     * All rights reserved is the default copyright stance, and is thus the default here.
     */
    void setLicense(String license) {
        switch (platform) {
            case Platform.FORGE:
                put 'license', license
                break
            case Platform.QUILT:
                this.data = merge(this.data, ["quilt_loader": ["metadata": ["license": license]]])
        }
    }

    /**
     * A URL to refer people to when problems occur with this mod.
     */
    void setIssueTrackerUrl(String issueTrackerUrl) {
        switch (platform) {
            case Platform.FORGE:
                put 'issueTrackerURL', issueTrackerUrl
                break
            case Platform.QUILT:
                this.data = merge(this.data, ["quilt_loader": ["metadata": ["contact": ["issues": issueTrackerUrl]]]])
        }
    }

    /**
     * The name of the mod loader type to load - for regular Java FML @Mod mods it should be {@code javafml}.
     * For GroovyModLoader @GMod mods it should be {@code gml}.
     */
    void setModLoader(String modLoader) {
       if (platform === Platform.FORGE)
            put 'modLoader', modLoader
    }

    /**
     * A version range to match for the {@link #setModLoader(java.lang.String)}.
     */
    void setLoaderVersion(String loaderVersion) {
        if (platform === Platform.FORGE)
            put 'loaderVersion', VersionRange.of(loaderVersion).toForge()
    }

    /**
     * A version range to match for the {@link #setModLoader(java.lang.String)}.
     */
    void setLoaderVersion(List<String> loaderVersion) {
        if (platform === Platform.FORGE) {
            final VersionRange range = new VersionRange()
            range.versions = loaderVersion.collectMany {VersionRange.of(it).versions}
            put 'loaderVersion', range.toForge()
        }
    }

    void mod(@DelegatesTo(value = ModInfoBuilder, strategy = DELEGATE_FIRST)
              @ClosureParams(value = SimpleType, options = 'modsdotgroovy.ModInfoBuilder') final Closure closure) {
        final modInfoBuilder = new ModInfoBuilder(platform)
        closure.delegate = modInfoBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(modInfoBuilder)

        pushMod(modInfoBuilder.build())
    }

    void mods(@DelegatesTo(value = ModsBuilder, strategy = DELEGATE_FIRST)
              @ClosureParams(value = SimpleType, options = 'modsdotgroovy.ModsBuilder') final Closure closure) {
        final modsBuilder = new ModsBuilder(platform)
        closure.delegate = modsBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(modsBuilder)
        modsBuilder.mods.each(this.&pushMod)
    }

    protected void pushMod(ImmutableModInfo modInfo) {
        switch (platform) {
            case Platform.FORGE:
                final mods = (data.computeIfAbsent('mods', {[]}) as List)
                modInfo.dependencies?.each {
                    ((data.computeIfAbsent('dependencies', {[:]}) as Map)
                            .computeIfAbsent(modInfo.modId, {[]}) as List)
                            .push(it.asForgeMap())
                }
                if (modInfo.customProperties !== null && !modInfo.customProperties.isEmpty())
                    (data.computeIfAbsent('modproperties', {[:]}) as Map)
                            .put(modInfo.modId, modInfo.customProperties)
                final modData = [:]

                modData['modId'] = modInfo.modId
                modData['version'] = modInfo.version
                modData['displayName'] = modInfo.displayName
                modData['displayUrl'] = modInfo.displayUrl
                modData['updateJsonUrl'] = modInfo.updateJsonUrl ?: inferUpdateJsonUrl(modInfo)
                modData['credits'] = modInfo.credits
                modData['logoFile'] = modInfo.logoFile
                modData['description'] = modInfo.description

                List<String> authorList = (modInfo.quiltModInfo.contributors.collectMany {it.value}+modInfo.authors).unique()
                String authorsString = combineAsString(authorList)
                modData['authors'] = authorsString
                mods.add(modData)
                break
            case Platform.QUILT:
                modInfo.dependencies?.each {
                    Map map = it.asQuiltMap()
                    /*
                    This whole chunk is a temporary fix for Quilt's lack of full representation of possible version
                    ranges. See: https://github.com/QuiltMC/quilt-loader/issues/110

                    TODO: Remove this code once Quilt Loader implements a version range spec that can represent any range.
                    (The current spec can only represent unions of certain types of ranges; for instance, the range
                    ">=1.0.0 <1.2.3 || >=2.0.0 <2.5.4" (or anything of a similar form) cannot be represented, and will
                    generate a quilt.mod.json that cannot be read)
                     */
                    if ((map.versions as List).size() == 1) {
                        final versionString = ((map.versions as List)[0] as String)
                        if (versionString.contains(' ')) {
                            String[] parts = versionString.split(' ',2)
                            ((this.data.computeIfAbsent('quilt_loader',{[:]}) as Map).computeIfAbsent('depends',{[]}) as List)
                                    .add(new HashMap(map).tap { it.versions = parts[0]})
                            ((this.data.computeIfAbsent('quilt_loader',{[:]}) as Map).computeIfAbsent('depends',{[]}) as List)
                                    .add(new HashMap(map).tap { it.versions = parts[1]})
                            return
                        }
                    }


                    ((this.data.computeIfAbsent('quilt_loader',{[:]}) as Map).computeIfAbsent('depends',{[]}) as List)
                            .add(map)
                }

                if (modInfo.customProperties !== null && !modInfo.customProperties.isEmpty())
                    this.data = merge(this.data, ['quilt_loader': modInfo.customProperties])

                boolean otherQuiltModsExist = (this.data?.quilt_loader as Map)?.id !== null

                final quiltModData = [:]
                final quiltMetadata = [:]
                if (!otherQuiltModsExist) {
                    quiltModData['id'] = modInfo.modId
                    quiltModData['version'] = modInfo.version
                } else {
                    quiltModData.computeIfAbsent('provides', {[]})
                    List provides = quiltModData['provides'] as List
                    provides.add(['id': modInfo.modId, 'version': modInfo.version])
                }
                quiltMetadata['name'] = modInfo.displayName
                quiltMetadata['contact'] = merge(['homepage':modInfo.displayUrl], modInfo.quiltModInfo.contact)
                quiltMetadata['icon'] = modInfo.logoFile
                quiltMetadata['description'] = modInfo.description
                quiltModData['entrypoints'] = modInfo.entrypoints
                quiltModData['intermediate_mappings'] = modInfo.quiltModInfo.intermediateMappings

                Map<String, List<String>> intermediateContributors = [:]
                modInfo.authors.each { author ->
                    intermediateContributors.computeIfAbsent(author, {[]}) << 'Author'
                }
                modInfo.quiltModInfo.contributors.each { title, people ->
                    people.each { person ->
                        intermediateContributors.computeIfAbsent(person, {[]}) << title
                    }
                }
                Map quiltContributors = [:]
                intermediateContributors.each {person, titles ->
                    quiltContributors[person] = combineAsString(titles)
                }
                quiltMetadata['contributors'] = quiltContributors
                quiltModData['metadata'] = quiltMetadata
                this.data = merge(this.data, ["quilt_loader": quiltModData])
        }
    }

    private static String combineAsString(List<String> parts) {
        String fullString = ''
        switch (parts.size()) {
            case 0:
                break
            case 1:
                fullString = parts[0]
                break
            case 2:
                fullString = parts[0] + ' and ' + parts[1]
                break
            default:
                parts.eachWithIndex { String entry, int i ->
                    if (i == 0) fullString = entry
                    else if (i == parts.size() - 1) fullString += ' and ' + entry
                    else fullString += ', ' + entry
                }
                break
        }
        return fullString
    }

    void sanitize() {
        sanitizeMap(data)
    }

    private static void sanitizeMap(Map data) {
        final copy = new LinkedHashMap(data) // cannot use Map.copyOf as we wish to remove null values
        copy.forEach { Object key, Object value ->
            if (value === null) data.remove(key)
            else if (value instanceof List) {
                value.removeIf { it === null }
            } else if (value instanceof Map) {
                sanitizeMap(value)
            } else if (value instanceof GString) {
                data[key] = value.toString()
            }
        }
    }

    private static Map merge(Map left, Map right) {
        if (left === null) return right
        if (right === null) return left
        return right.inject(new LinkedHashMap(left)) { map, entry ->
            if (map[entry.key] instanceof Map && entry.value instanceof Map) {
                map[entry.key] = merge(map[entry.key] as Map, entry.value as Map)
            } else if (map[entry.key] instanceof List && entry.value instanceof List) {
                map[entry.key] = (map[entry.key] as List) + (entry.value as List)
            } else if (map[entry.key] instanceof List) {
                map[entry.key] = (map[entry.key] as List) + entry.value
            } else if (entry.value instanceof List) {
                List values = [map[entry.key]]
                values.addAll(entry.value as List)
                map[entry.key] = values
            } else {
                map[entry.key] = entry.value
            }
            return map
        } as Map
    }

    static synchronized ModsDotGroovy make(@DelegatesTo(value = ModsDotGroovy, strategy = DELEGATE_FIRST) Closure closure) {
        final val = new ModsDotGroovy()
        closure.resolveStrategy = DELEGATE_FIRST
        closure.delegate = val
        closure.call(val)
        val.sanitize()
        return val
    }

    /**
     * Infers the updateJsonUrl from the provided modInfo.
     * @param modInfo
     * @return the inferred updateJsonUrl, or null if it could not be inferred
     */
    @Nullable
    String inferUpdateJsonUrl(final ImmutableModInfo modInfo) {
        if (platform !== Platform.FORGE) return null

        final String displayOrIssueTrackerUrl = modInfo.displayUrl ?: ((String) data.issueTrackerUrl)
        if (displayOrIssueTrackerUrl.is null) return null

        @Nullable
        HttpClient httpClient = null

        // determine if the displayUrl is a CurseForge URL and if so, extract the project ID and slug from it if possible
        // and use those to construct an updateJsonUrl using forge.curseupdate.com
        // example in: https://www.curseforge.com/minecraft/mc-mods/spammycombat?projectId=623297
        // example out: 623297/spammycombat
        final Matcher cfMatcher = displayOrIssueTrackerUrl =~ $/.*curseforge.com/minecraft/mc-mods/([^?/]+)\?projectId=(\d+)/$
        if (cfMatcher.matches()) {
            httpClient ?= HttpClient.newBuilder().build()
            // determine the modId first from the modId, falling back to the slug in the URL if that fails
            final String updateJsonUrlRoot = "https://forge.curseupdate.com/${cfMatcher.group(2)}/"
            final String[] updateJsonUrls = [
                    updateJsonUrlRoot + modInfo.modId,
                    updateJsonUrlRoot + cfMatcher.group(1)
            ]

            // make a GET request to the updateJsonUrl and to see if it's valid
            final jsonSlurper = new JsonSlurper().setType(JsonParserType.INDEX_OVERLAY)
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

        // todo: create a web service that returns the projectId from a given slug and use that to avoid the need for
        // explicitly specifying the projectId in the displayUrl

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
                final String updateJsonUrlMainBranchRoot = updateJsonUrlRoot[0..-6] + 'main'
                updateJsonUrls.addAll([
                        "${updateJsonUrlMainBranchRoot}/src/main/resources/update.json",
                        "${updateJsonUrlMainBranchRoot}/src/main/resources/updates.json",
                        "${updateJsonUrlMainBranchRoot}/update.json",
                        "${updateJsonUrlMainBranchRoot}/updates.json"
                ])
            }

            // todo: check if the updates.json file exists locally and skip the network request if it does

            // make a HEAD request to the updateJsonUrl to see if it exists using HttpClient
            for (final String url in updateJsonUrls) {
                final HttpRequest request = HttpRequest.newBuilder(URI.create(url)).method("HEAD", HttpRequest.BodyPublishers.noBody()).build()
                try {
                    final HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding())
                    if (response.statusCode() === HttpURLConnection.HTTP_OK)
                        return url
                } catch (final IOException ignored) {}
            }
        }

        return null
    }
}
