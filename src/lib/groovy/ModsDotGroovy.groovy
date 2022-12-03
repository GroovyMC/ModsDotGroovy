/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

//file:noinspection GrMethodMayBeStatic


import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import modsdotgroovy.ImmutableModInfo
import modsdotgroovy.MixinConfigBuilder
import modsdotgroovy.ModInfoBuilder
import modsdotgroovy.ModsBuilder
import modsdotgroovy.PackMcMetaBuilder
import modsdotgroovy.VersionRange

import static groovy.lang.Closure.DELEGATE_FIRST

@CompileStatic
@SuppressWarnings('unused')
class ModsDotGroovy {
    protected Map data

    protected ModsDotGroovy() {
        if (platform === Platform.QUILT) {
            data = ['schema_version': 1, 'quilt_loader': [:]]
        } else {
            data = [:]
        }
    }

    protected static Platform platform
    protected static String mixinRefMap

    protected static void setPlatform(String name) {
        platform = Platform.valueOf(name.toUpperCase(Locale.ROOT))
    }

    protected static void setMixinRefMap(String refMap) {
        mixinRefMap = refMap.isBlank() ? null : refMap
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
        if (platform == Platform.FORGE) {
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
       if (platform == Platform.FORGE)
            put 'modLoader', modLoader
    }

    /**
     * A version range to match for the {@link #setModLoader(java.lang.String)}.
     */
    void setLoaderVersion(String loaderVersion) {
        if (platform == Platform.FORGE)
            put 'loaderVersion', VersionRange.of(loaderVersion).toForge()
    }

    /**
     * A version range to match for the {@link #setModLoader(java.lang.String)}.
     */
    void setLoaderVersion(List<String> loaderVersion) {
        if (platform == Platform.FORGE) {
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
                modData['updateJsonUrl'] = modInfo.updateJsonUrl
                modData['displayUrl'] = modInfo.displayUrl
                modData['credits'] = modInfo.credits
                modData['logoFile'] = modInfo.logoFile
                modData['description'] = modInfo.description

                String authorsString = ''
                switch (modInfo.authors.size()) {
                    case 0:
                        break
                    case 1:
                        authorsString = modInfo.authors[0]
                        break
                    case 2:
                        authorsString = modInfo.authors[0] + ' and ' + modInfo.authors[1]
                        break
                    default:
                        modInfo.authors.eachWithIndex { String entry, int i ->
                            if (i == 0) authorsString = entry
                            else if (i == modInfo.authors.size() - 1) authorsString += ' and ' + entry
                            else authorsString += ', ' + entry
                        }
                        break
                }
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
                quiltMetadata['contact'] = ["homepage":modInfo.displayUrl]
                quiltMetadata['icon'] = modInfo.logoFile
                quiltMetadata['description'] = modInfo.description
                quiltModData['entrypoints'] = modInfo.entrypoints

                Map quiltContributors = [:]
                modInfo.authors.each {
                    quiltContributors[it] = "Author"
                }
                quiltMetadata['contributors'] = quiltContributors
                quiltModData['metadata'] = quiltMetadata
                this.data = merge(this.data, ["quilt_loader": quiltModData])
        }
    }

    void packMcMeta(@DelegatesTo(value = PackMcMetaBuilder, strategy = DELEGATE_FIRST)
              @ClosureParams(value = SimpleType, options = 'modsdotgroovy.PackMcMetaBuilder') final Closure closure) {
        final builder = new PackMcMetaBuilder()
        closure.delegate = builder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(builder)
        extraMaps.put('packMcMeta', builder)
    }

    void mixinConfig(@DelegatesTo(value = MixinConfigBuilder, strategy = DELEGATE_FIRST)
              @ClosureParams(value = SimpleType, options = 'modsdotgroovy.MixinConfigBuilder') final Closure closure) {
        final builder = new MixinConfigBuilder()
        builder.setRefMap(mixinRefMap)
        closure.delegate = builder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(builder)
        extraMaps.put('mixinConfig', builder)
    }

    void sanitize() {
        sanitizeMap(data)
    }

    private Map getExtraMaps() {
        (Map)data.computeIfAbsent('extraMaps') { new HashMap<>() }
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
}