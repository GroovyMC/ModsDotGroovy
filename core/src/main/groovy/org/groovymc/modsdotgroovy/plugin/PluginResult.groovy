package org.groovymc.modsdotgroovy.plugin

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

    @TupleConstructor
    static class Change extends PluginResult {
        /**
         * Whether the change is reentrant. If true, the new property will be reprocessed by the plugin.
         */
        boolean reentrant = false

        /**
         * The new value of the property.
         * If null, the property will be removed.
         */
        @Nullable def newValue = null
    }

    @TupleConstructor
    static class Move extends Change {
        /**
         * The new location of the property.
         */
        List<String> newLocation = null
    }

    @TupleConstructor
    static class Rename extends Change {
        /**
         * The new name of the property.
         */
        String newPropertyName = null
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

    static Rename rename(final String newPropertyName, final def value) {
        return new Rename(newPropertyName: newPropertyName, newValue: value)
    }

    static Move move(final List<String> newLocation, final def value) {
        return new Move(newLocation: newLocation, newValue: value)
    }

    static Change change(final def value) {
        return new Change(newValue: value)
    }

    static Change remove() {
        return new Change(newValue: null)
    }
}
