package io.github.groovymc.modsdotgroovy.plugin

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import org.jetbrains.annotations.Nullable

@CompileStatic
class PluginResult {
    static final class Unhandled extends PluginResult {}

    /**
     * Don't make any changes to the property.
     */
    static class Validate extends PluginResult {}

    /**
     * If all null, then the property will be removed. If you don't want to make any changes, use {@link Validate} instead.
     */
    @CompileDynamic
    @TupleConstructor
    static class Change extends PluginResult {
        /**
         * The new name of the property.
         * If null, the property will keep its original name.
         */
        @Nullable String newPropertyName = null

        /**
         * The new value of the property.
         * If null, the property will be removed.
         */
        @Nullable def newValue = null

        /**
         * The new location of the property.
         * If null, the property will keep its original location.
         */
        @Nullable Deque<String> newLocation = null
    }

    /**
     * Represents an error in validation that occurred during the plugin's execution. If this exception is thrown, the
     * plugin is assumed to be in a recoverable state - it just received invalid data.
     */
    static class MDGPluginException extends RuntimeException {
        MDGPluginException(final String message) {
            super(message)
        }

        MDGPluginException(final String message, final Throwable cause) {
            super(message, cause)
        }

        MDGPluginException(Throwable throwable) {
            super(throwable)
        }
    }

    @CompileDynamic
    static <T extends PluginResult> T of(final def obj) {
        switch (obj) {
            case PluginResult: return (T) obj
            case null: return (T) new Validate()
            default: return (T) new Change(newValue: obj)
        }
    }

    @CompileDynamic
    static Change rename(final String newPropertyName, final def value) {
        return new Change(newPropertyName: newPropertyName, newValue: value)
    }

    @CompileDynamic
    static Change move(final Deque<String> newParentLocation, final def value) {
        return new Change(newLocation: newParentLocation, newValue: value)
    }

    @CompileDynamic
    static Change move(final List<String> newParentLocation, final def value) {
        return new Change(newLocation: new ArrayDeque<>(newParentLocation), newValue: value)
    }

    @CompileDynamic
    static Change move(final Deque<String> newParentLocation, final String newPropertyName, final def value) {
        return new Change(newLocation: newParentLocation, newValue: value, newPropertyName: newPropertyName)
    }

    @CompileDynamic
    static Change move(final List<String> newParentLocation, final String newPropertyName, final def value) {
        return new Change(newLocation: new ArrayDeque<>(newParentLocation), newValue: value, newPropertyName: newPropertyName)
    }

    static Change remove() {
        return new Change(newValue: null)
    }
}
