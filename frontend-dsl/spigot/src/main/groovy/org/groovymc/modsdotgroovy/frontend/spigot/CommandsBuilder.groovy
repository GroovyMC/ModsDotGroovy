package org.groovymc.modsdotgroovy.frontend.spigot

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.jetbrains.annotations.NotNull

import static groovy.lang.Closure.DELEGATE_FIRST

@CompileStatic
@Log4j2(category = 'MDG - Spigot Frontend')
class CommandsBuilder extends DslBuilder implements Map<String, Void> {
    /**@
     * The name of a command your plugin wishes to register.
     */
    void command(final String name) {
        log.debug 'command(string)'
        core.push('command')
        core.put('name', name)
        core.pop()
    }

    /**@
     * The name of a command your plugin wishes to register, along with any optional attributes.
     */
    void command(final String name,
                 @DelegatesTo(value = CommandBuilder, strategy = DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.spigot.CommandBuilder')
                 final Closure closure) {
        log.debug 'command(string, closure)'
        core.push('command')
        final commandBuilder = new CommandBuilder(core)
        core.put('name', name)
        closure.resolveStrategy = DELEGATE_FIRST
        closure.delegate = commandBuilder
        closure.call(commandBuilder)
        core.pop()
    }

    /**@
     * Register a command, specifying its name and optional attributes inside the closure.
     */
    void command(@DelegatesTo(value = CommandBuilder, strategy = DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.spigot.CommandBuilder')
                 final Closure closure) {
        log.debug 'command(closure)'
        core.push('command')
        final commandBuilder = new CommandBuilder(core)
        closure.resolveStrategy = DELEGATE_FIRST
        closure.delegate = commandBuilder
        closure.call(commandBuilder)
        core.pop()
    }

    def getProperty(final String name) {
        log.debug "getProperty(name: $name)"
        command name
    }

    @Override
    Void get(Object key) {
        command key as String
        return null
    }

    CommandsBuilder(final ModsDotGroovyCore core) {
        super(core)
    }

    // Todo: Move to @AutoImplement once IntelliJ supports it again
    //region Map boilerplate
    @Override
    int size() {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean isEmpty() {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean containsKey(Object key) {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean containsValue(Object value) {
        throw new UnsupportedOperationException()
    }

    @Override
    Void put(String key, Void value) {
        throw new UnsupportedOperationException()
    }

    @Override
    Void remove(Object key) {
        throw new UnsupportedOperationException()
    }

    @Override
    void putAll(@NotNull Map<? extends String, ? extends Void> m) {
        throw new UnsupportedOperationException()
    }

    @Override
    void clear() {
        throw new UnsupportedOperationException()
    }

    @Override
    Set<String> keySet() {
        throw new UnsupportedOperationException()
    }

    @Override
    Collection<Void> values() {
        throw new UnsupportedOperationException()
    }

    @Override
    Set<Entry<String, Void>> entrySet() {
        throw new UnsupportedOperationException()
    }
    //endregion Map boilerplate
}
