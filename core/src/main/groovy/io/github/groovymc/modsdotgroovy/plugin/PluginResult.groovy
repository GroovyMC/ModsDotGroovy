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
     * plugin is assumed to be in a recoverable state - it just recieved invalid data.
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
    static Change move(final Deque<String> newLocation, final def value) {
        return new Change(newLocation: newLocation, newValue: value)
    }

    @CompileDynamic
    static Change move(final List<String> newLocation, final def value) {
        return new Change(newLocation: new ArrayDeque<>(newLocation), newValue: value)
    }

    static Change remove() {
        return new Change(newValue: null)
    }

    /**
     * Default action for not even listening to the property event.
     * Do nothing and let the next plugin handle it, or let the property change go through if no other plugin handles it.
     */
//    UNHANDLED,

    /**
     * Default action for listening to the event but *not* changing the property.<br>
     * Useful for validation.<br>
     * Allows the next plugin to handle the property.
     */
//    VALIDATE,

    /**
     * Default action for listening to the event and changing the property.<br>
     * Useful for transforming the property. For example, making a string lowercase before it's put into the rootMap map.<br>
     * Allows the next plugin to handle the property.
     */
//    TRANSFORM,

    /**
     * Similar to {@link #TRANSFORM}, but stops firing this property event to other plugins.<br>
     * Prevents the next plugin from handling the property.
     */
//    BREAK,

    /**
     * Prevents the next plugin from handling the property and prevents the property from being set.<br>
     * Useful for preventing the property from being added to the rootMap map altogether.
     */
//    IGNORE
}
