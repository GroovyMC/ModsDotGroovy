/*
 * MIT License
 *
 * Copyright (c) 2022 GroovyMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


import groovy.transform.CompileStatic
import groovy.transform.Internal
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import modsdotgroovy.ImmutableModInfo
import modsdotgroovy.ModInfoBuilder
import modsdotgroovy.ModsBuilder

import static groovy.lang.Closure.DELEGATE_FIRST

@CompileStatic
@SuppressWarnings('unused')
class ModsDotGroovy {
    protected Map data

    protected ModsDotGroovy() {
        switch (platform) {
            case Platform.QUILT:
                this.data = ["schema_version":1, "quilt_loader":[:]]
                break
            case Platform.FORGE:
                this.data = [:]
        }
    }

    static Platform platform

    static void setPlatform(String name) {
        platform = Platform.valueOf(name.toUpperCase(Locale.ROOT))
    }

    void propertyMissing(String name, Object value) {
        put(name, value)
    }

    void put(String name, Object value) {
        data[name] = value
    }

    void onForge(@DelegatesTo(value = ModsDotGroovy, strategy = DELEGATE_FIRST)
               @ClosureParams(value = SimpleType, options = 'ModsDotGroovy') Closure closure) {
        if (platform == Platform.FORGE) {
            closure.delegate = this
            closure.resolveStrategy = DELEGATE_FIRST
            closure.call(this)
        }
    }

    void onQuilt(@DelegatesTo(value = ModsDotGroovy, strategy = DELEGATE_FIRST)
               @ClosureParams(value = SimpleType, options = 'ModsDotGroovy') Closure closure) {
        if (platform == Platform.QUILT) {
            closure.delegate = this
            closure.resolveStrategy = DELEGATE_FIRST
            closure.call(this)
        }
    }

    /**
     * The license for your mod. This is mandatory metadata and allows for easier comprehension of your redistributive properties.<br>
     * Review your options at https://choosealicense.com/. All rights reserved is the default copyright stance, and is thus the default here.
     */
    void setLicense(String license) {
        switch (platform) {
            case Platform.FORGE:
                put 'license', license
                break
            case Platform.QUILT:
                this.data = merge(this.data, ["quilt_loader":["metadata":["license":license]]])
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
                this.data = merge(this.data, ["quilt_loader":["metadata":["contact":["issues":issueTrackerUrl]]]])
        }
    }

    /**
     * The name of the mod loader type to load - for regular Java FML @Mod mods it should be {@code javafml}.
     * For GroovyModLoader @GMod mods it should be {@code gml}.
     */
    void setModLoader(String modLoader) {
        switch (platform) {
            case Platform.FORGE:
                put 'modLoader', modLoader
                break
            case Platform.QUILT:
                break
        }
    }

    /**
     * A version range to match for the {@link #setModLoader(java.lang.String)}.
     */
    void setLoaderVersion(String loaderVersion) {
        switch (platform) {
            case Platform.FORGE:
                put 'loaderVersion', loaderVersion
                break
            case Platform.QUILT:
                break        }
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
                    ((this.data.computeIfAbsent('quilt_loader',{[:]}) as Map).computeIfAbsent('depends',{[]}) as List)
                            .add(it.asQuiltMap())
                }

                if (modInfo.customProperties !== null && !modInfo.customProperties.isEmpty())
                    this.data = merge(this.data, ['quilt_loader':modInfo.customProperties])

                boolean otherQuiltModsExist = (this.data?.quilt_loader as Map)?.id !== null

                final quiltModData = [:]
                final quiltMetadata = [:]
                if (!otherQuiltModsExist) {
                    quiltModData['id'] = modInfo.modId
                    quiltModData['version'] = modInfo.version
                } else {
                    quiltModData.computeIfAbsent('provides', {[]})
                    List provides = quiltModData['provides'] as List
                    provides.add(['id':modInfo.modId,'version':modInfo.version])
                }
                quiltMetadata['name'] = modInfo.displayName
                quiltMetadata['contact'] = ["homepage":modInfo.displayUrl]
                quiltMetadata['icon'] = modInfo.logoFile
                quiltMetadata['description'] = modInfo.description

                Map quiltContributors = [:]
                modInfo.authors.each {
                    quiltContributors[it] = "Author"
                }
                quiltMetadata['contributors'] = quiltContributors
                quiltModData['metadata'] = quiltMetadata
                this.data = merge(this.data, ["quilt_loader":quiltModData])
        }
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
            }
        }
    }

    private static Map merge(Map left, Map right) {
        if (left === null) return right
        if (right === null) return right
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