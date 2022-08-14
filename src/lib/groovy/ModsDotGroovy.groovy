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
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import modsdotgroovy.ImmutableModInfo
import modsdotgroovy.ModInfoBuilder
import modsdotgroovy.ModsBuilder

import static groovy.lang.Closure.DELEGATE_FIRST

@CompileStatic
@SuppressWarnings('unused')
class ModsDotGroovy {
    protected final Map data = [:]

    void propertyMissing(String name, Object value) {
        put(name, value)
    }

    void put(String name, Object value) {
        data[name] = value
    }

    /**
     * The license for your mod. This is mandatory metadata and allows for easier comprehension of your redistributive properties.<br>
     * Review your options at https://choosealicense.com/. All rights reserved is the default copyright stance, and is thus the default here.
     */
    void setLicense(String license) {
        put 'license', license
    }

    /**
     * A URL to refer people to when problems occur with this mod.
     */
    void setIssueTrackerUrl(String issueTrackerUrl) {
        put 'issueTrackerURL', issueTrackerUrl
    }

    /**
     * The name of the mod loader type to load - for regular Java FFML @Mod mods it should be {@code javafml}.
     * For GroovyModLoader @GMod mods it should be {@code gml}.
     */
    void setModLoader(String modLoader) {
        put 'modLoader', modLoader
    }

    /**
     * A version range to match for the {@link #setModLoader(java.lang.String)}.
     */
    void setLoaderVersion(String loaderVersion) {
        put 'loaderVersion', loaderVersion
    }

    void mod(@DelegatesTo(value = ModInfoBuilder, strategy = DELEGATE_FIRST)
              @ClosureParams(value = SimpleType, options = 'modsdotgroovy.ModInfoBuilder') final Closure closure) {
        final modInfoBuilder = new ModInfoBuilder()
        closure.delegate = modInfoBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(modInfoBuilder)

        pushMod(modInfoBuilder.build())
    }

    void mods(@DelegatesTo(value = ModsBuilder, strategy = DELEGATE_FIRST)
              @ClosureParams(value = SimpleType, options = 'modsdotgroovy.ModsBuilder') final Closure closure) {
        final modsBuilder = new ModsBuilder()
        closure.delegate = modsBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(modsBuilder)
        modsBuilder.mods.each(this.&pushMod)
    }

    protected void pushMod(ImmutableModInfo modInfo) {
        final mods = (data.computeIfAbsent('mods', {[]}) as List)
        modInfo.dependencies.each {
            ((data.computeIfAbsent('dependencies', {[:]}) as Map)
                    .computeIfAbsent(modInfo.modId, {[]}) as List)
                    .push(it.asMap())
        }

        put('modproperties', ["${modInfo.modId}": modInfo.customProperties])

        final modData = [:]
        modData['modId'] = modInfo.modId
        modData['version'] = modInfo.version
        modData['displayName'] = modInfo.displayName
        modData['updateJsonUrl'] = modInfo.updateJsonUrl
        modData['displayUrl'] = modInfo.displayUrl
        modData['credits'] = modInfo.credits
        modData['logoFile'] = modInfo.logoFile
        modData['description'] = modInfo.description
        modData['authors'] = modInfo.authors

        mods.add(modData)
    }

    void sanitize() {
        sanitizeMap(data)
    }

    private static void sanitizeMap(Map data) {
        final copy = Map.copyOf(data)
        copy.forEach { Object key, Object value ->
            if (value === null) data.remove(key)
            else if (value instanceof List) {
                value.removeIf { it === null }
            } else if (value instanceof Map) {
                sanitizeMap(value)
            }
        }
    }

    static ModsDotGroovy make(@DelegatesTo(value = ModsDotGroovy, strategy = DELEGATE_FIRST) Closure closure) {
        final val = new ModsDotGroovy()
        closure.resolveStrategy = DELEGATE_FIRST
        closure.delegate = val
        closure.call(val)
        val.sanitize()
        return val
    }
}