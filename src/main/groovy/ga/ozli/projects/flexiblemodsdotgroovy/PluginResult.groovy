package ga.ozli.projects.flexiblemodsdotgroovy

import groovy.transform.CompileStatic

@CompileStatic
enum PluginResult {
    /**
     * Default action for not even listening to the property event.
     * Do nothing and let the next plugin handle it, or let the property change go through if no other plugin handles it.
     */
    UNHANDLED,

    /**
     * Default action for listening to the event but *not* changing the property.<br>
     * Useful for validation.<br>
     * Allows the next plugin to handle the property.
     */
    VALIDATE,

    /**
     * Default action for listening to the event and changing the property.<br>
     * Useful for transforming the property. For example, making a string lowercase before it's put into the data map.<br>
     * Allows the next plugin to handle the property.
     */
    TRANSFORM,

    /**
     * Similar to {@link #TRANSFORM}, but stops firing this property event to other plugins.<br>
     * Prevents the next plugin from handling the property.
     */
    BREAK,

    /**
     * Prevents the next plugin from handling the property and prevents the property from being set.<br>
     * Useful for preventing the property from being added to the data map altogether.
     */
    IGNORE

    private PluginResult() {}
}
