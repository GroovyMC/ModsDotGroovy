package org.groovymc.modsdotgroovy.frontend.neoforge

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.MapClosureInterceptor
import org.jetbrains.annotations.Nullable

import static groovy.lang.Closure.DELEGATE_FIRST

@CompileStatic
@Log4j2(category = 'MDG - NeoForge Frontend')
class DependenciesBuilder extends DslBuilder implements MapClosureInterceptor {
    @Nullable def neoforge = null

    @Nullable def minecraft = null

    void mod(@DelegatesTo(value = DependencyBuilder, strategy = DELEGATE_FIRST)
             @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.DependencyBuilder')
             final Closure closure) {
        log.debug "mod(closure)"
        core.push('dependency')
        final dependencyBuilder = new DependencyBuilder(core)
        closure.delegate = dependencyBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(dependencyBuilder)
        core.pop()
    }

    void mod(final String modId,
             @DelegatesTo(value = DependencyBuilder, strategy = DELEGATE_FIRST)
             @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.DependencyBuilder')
             final Closure closure) {
        log.debug "mod(string, closure)"
        core.push('dependency')
        final dependencyBuilder = new DependencyBuilder(core)
        core.put('modId', modId)
        closure.delegate = dependencyBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(dependencyBuilder)
        core.pop()
    }

    void optional(@DelegatesTo(value = DependencyBuilder, strategy = DELEGATE_FIRST)
                  @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.DependencyBuilder')
                  final Closure closure) {
        log.debug "optional(closure)"
        core.push('dependency')
        final dependencyBuilder = new DependencyBuilder(core)
        core.put('type', 'optional')
        closure.delegate = dependencyBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(dependencyBuilder)
        core.pop()
    }

    void optional(final String modId,
                  @DelegatesTo(value = DependencyBuilder, strategy = DELEGATE_FIRST)
                  @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.DependencyBuilder')
                  final Closure closure) {
        log.debug "optional(string, closure)"
        core.push('dependency')
        final dependencyBuilder = new DependencyBuilder(core)
        core.put('type', 'optional')
        core.put('modId', modId)
        closure.delegate = dependencyBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(dependencyBuilder)
        core.pop()
    }

    void required(@DelegatesTo(value = DependencyBuilder, strategy = DELEGATE_FIRST)
                  @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.DependencyBuilder')
                  final Closure closure) {
        log.debug "required(closure)"
        core.push('dependency')
        final dependencyBuilder = new DependencyBuilder(core)
        core.put('type', 'required')
        closure.delegate = dependencyBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(dependencyBuilder)
        core.pop()
    }

    void required(final String modId,
                  @DelegatesTo(value = DependencyBuilder, strategy = DELEGATE_FIRST)
                  @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.DependencyBuilder')
                  final Closure closure) {
        log.debug "required(string, closure)"
        core.push('dependency')
        final dependencyBuilder = new DependencyBuilder(core)
        core.put('type', 'required')
        core.put('modId', modId)
        closure.delegate = dependencyBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(dependencyBuilder)
        core.pop()
    }

    void incompatible(@DelegatesTo(value = DependencyBuilder, strategy = DELEGATE_FIRST)
                      @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.DependencyBuilder')
                      final Closure closure) {
        log.debug "incompatible(closure)"
        core.push('dependency')
        final dependencyBuilder = new DependencyBuilder(core)
        core.put('type', 'incompatible')
        closure.delegate = dependencyBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(dependencyBuilder)
        core.pop()
    }

    void incompatible(final String modId,
                      @DelegatesTo(value = DependencyBuilder, strategy = DELEGATE_FIRST)
                      @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.DependencyBuilder')
                      final Closure closure) {
        log.debug "incompatible(string, closure)"
        core.push('dependency')
        final dependencyBuilder = new DependencyBuilder(core)
        core.put('type', 'incompatible')
        core.put('modId', modId)
        closure.delegate = dependencyBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(dependencyBuilder)
        core.pop()
    }

    void discouraged(@DelegatesTo(value = DependencyBuilder, strategy = DELEGATE_FIRST)
                      @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.DependencyBuilder')
                      final Closure closure) {
        log.debug "discouraged(closure)"
        core.push('dependency')
        final dependencyBuilder = new DependencyBuilder(core)
        core.put('type', 'discouraged')
        closure.delegate = dependencyBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(dependencyBuilder)
        core.pop()
    }

    void discouraged(final String modId,
                      @DelegatesTo(value = DependencyBuilder, strategy = DELEGATE_FIRST)
                      @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.neoforge.DependencyBuilder')
                      final Closure closure) {
        log.debug "discouraged(string, closure)"
        core.push('dependency')
        final dependencyBuilder = new DependencyBuilder(core)
        core.put('type', 'discouraged')
        core.put('modId', modId)
        closure.delegate = dependencyBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(dependencyBuilder)
        core.pop()
    }

    @CompileDynamic
    void setProperty(final String name, final def value) {
        mod {
            modId = name
            versionRange = value
        }
    }

    DependenciesBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
