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

package modsdotgroovy

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovyjarjarantlr4.v4.runtime.misc.Nullable
import org.apache.groovy.lang.annotation.Incubating

import static groovy.lang.Closure.DELEGATE_FIRST
import static groovy.lang.Closure.DELEGATE_FIRST

@Incubating
@CompileStatic
class ModInfoBuilder {
    /**
     * The modId of the mod. This should match the value of your mod's {@literal @}GMod/{@literal @}Mod annotated main class.
     */
    String modId = 'unknown'

    /**
     * The friendly name of the mod. This is the name that will be displayed in the in-game Mods screen.
     */
    String displayName = modId.capitalize()

    /**
     * The version number of the mod - there's a few well known ${} variables usable here or just hardcode it.<br>
     * ${file.jarVersion} will substitute the value of the Implementation-Version as read from the mod's JAR file metadata.<br>
     * See the associated build.gradle script for how to populate this completely automatically during a build.
     */
    String version = 'unknown'

    /**
     * A URL to query for updates for this mod.<br>
     * See <a href='https://mcforge.readthedocs.io/en/latest/gettingstarted/autoupdate/'>the JSON update specification</a>
     */
    @Nullable String updateJsonUrl = null

    /**
     * A URL for the "homepage" for this mod, displayed in the in-game Mods screen.
     */
    @Nullable String displayUrl = null

    // todo: change type to a File instead of a String
    /**
     * A file name (in the root of the mod JAR) containing a logo for display in the in-game Mods screen.
     */
    @Nullable String logoFile = null

    @Nullable String credits = null
    List<String> authors = []

    /**
     * Display Test controls the display for your mod in the server connection screen.<br>
     */
    // only in 1.19+
    //@Nullable String displayTest = null

    /**
     * A multi-line description text for the mod, displayed in the in-game Mods screen.<br>
     * Groovylicious will automatically strip the fixed length code indent for you.
     */
    String description = ''

    /**
     * The dependencies of the mod
     */
    List<Dependency> dependencies

    /**
     * The custom properties of the mod
     */
    Map properties = [:]

    void propertyMissing(String name, Object value) {
        properties[name] = value
    }

    void dependencies(@DelegatesTo(value = DependenciesBuilder, strategy = DELEGATE_FIRST)
                      @ClosureParams(value = SimpleType, options = 'modsdotgroovy.DependenciesBuilder') final Closure closure) {
        final dependenciesBuilder = new DependenciesBuilder()
        closure.delegate = dependenciesBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(dependenciesBuilder)
        dependencies = dependenciesBuilder.build()
    }

    void setDescription(final String description) {
        this.description = description.stripIndent()
    }

    void setAuthor(final String author) {
        this.authors = [author]
    }

    void author(final String author) {
        this.authors << author
    }

    ImmutableModInfo build() {
        return new ImmutableModInfo(this.modId, this.displayName, this.version, this.updateJsonUrl, this.displayUrl, this.logoFile, this.credits, this.authors, this.description, this.dependencies, this.properties)
    }
}
