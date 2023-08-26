package org.groovymc.modsdotgroovy.frontend.spigot

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.jetbrains.annotations.NotNull

@CompileStatic
@Log4j2(category = 'MDG - Spigot Frontend')
class PermissionsBuilder extends DslBuilder implements Map<String, Void> {
    void permission(final String node) {
        log.debug 'permission(string)'
        core.push('permission')
        core.put('permission', node)
        core.pop()
    }

    void permission(final String node,
                    @DelegatesTo(value = PermissionBuilder, strategy = Closure.DELEGATE_FIRST)
                    @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.spigot.PermissionBuilder')
                    final Closure closure) {
        log.debug('permission(string, closure)')
        core.push('permission')
        core.put('node', node)
        final permissionBuilder = new PermissionBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = permissionBuilder
        closure.call(permissionBuilder)
        core.pop()
    }

    void permission(@DelegatesTo(value = PermissionBuilder, strategy = Closure.DELEGATE_FIRST)
                    @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.spigot.PermissionBuilder')
                    final Closure closure) {
        log.debug('permission(string, closure)')
        core.push('permission')
        final permissionBuilder = new PermissionBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = permissionBuilder
        closure.call(permissionBuilder)
        core.pop()
    }

    def getProperty(final String permissionNode) {
        log.debug "getProperty(permissionNode: $permissionNode)"
        permission permissionNode
    }

    @Override
    Void get(Object key) {
        permission key as String
        return null
    }

    PermissionsBuilder(ModsDotGroovyCore core) {
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
