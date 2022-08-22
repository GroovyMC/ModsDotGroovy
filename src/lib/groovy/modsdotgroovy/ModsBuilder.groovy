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
import org.apache.groovy.lang.annotation.Incubating

import static groovy.lang.Closure.DELEGATE_FIRST

@Incubating
@CompileStatic
class ModsBuilder {
    private List<ImmutableModInfo> mods = []
    private Platform platform

    ModsBuilder(Platform platform) {
        this.platform = platform
    }

    List<ImmutableModInfo> getMods() {
        return mods
    }

    void onQuilt(@DelegatesTo(value = ModsBuilder, strategy = DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = "modsdotgroovy.ModsBuilder") final Closure closure) {
        if (platform == Platform.QUILT) {
            closure.delegate = this
            closure.resolveStrategy = DELEGATE_FIRST
            closure.call(this)
        }
    }

    void onForge(@DelegatesTo(value = ModsBuilder, strategy = DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = "modsdotgroovy.ModsBuilder") final Closure closure) {
        if (platform == Platform.FORGE) {
            closure.delegate = this
            closure.resolveStrategy = DELEGATE_FIRST
            closure.call(this)
        }
    }

    void modInfo(@DelegatesTo(value = ModInfoBuilder, strategy = DELEGATE_FIRST)
              @ClosureParams(value = SimpleType, options = 'modsdotgroovy.ModInfoBuilder') final Closure closure) {
        final modInfoBuilder = new ModInfoBuilder(platform)
        closure.delegate = modInfoBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(modInfoBuilder)
        mods << modInfoBuilder.build()
    }
    void mod(@DelegatesTo(value = ModInfoBuilder, strategy = DELEGATE_FIRST)
              @ClosureParams(value = SimpleType, options = 'modsdotgroovy.ModInfoBuilder') final Closure closure) {
        modInfo(closure)
    }
}
