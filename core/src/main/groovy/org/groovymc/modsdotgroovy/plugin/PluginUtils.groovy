package org.groovymc.modsdotgroovy.plugin

import groovy.transform.CompileStatic
import org.jetbrains.annotations.ApiStatus

import java.util.regex.Pattern

/**
 * Some useful validation methods for plugins.
 */
@CompileStatic
class PluginUtils {
    private static final Pattern HTTP_URL_PATTERN = Pattern.compile($/^https?:///$, Pattern.CASE_INSENSITIVE) // matches http:// and https://
    private static final Pattern STARTS_WITH_NUMBER_PATTERN = ~/^\d/

    /**
     * Checks if the given string starts with "http://" or "https://".
     */
    static boolean isValidHttpUrl(final String url) {
        return url =~ HTTP_URL_PATTERN
    }

    /**
     * Checks if the given URI starts with "http://" or "https://".
     */
    @ApiStatus.Experimental
    static boolean isValidHttpUrl(final URI uri) {
        // todo: test this
        return isValidHttpUrl(uri.schemeSpecificPart)
    }

    /**
     * Checks if the given string starts with "http://".
     */
    static boolean isInsecureHttpUrl(final String url) {
        return url.startsWithIgnoreCase('http://')
    }

    /**
     * Checks if the given URI starts with "http://".
     */
    @ApiStatus.Experimental
    static boolean isInsecureHttpUrl(final URI uri) {
        // todo: test this
        return isInsecureHttpUrl(uri.schemeSpecificPart)
    }
    
    static boolean startsWithNumber(final String string) {
        return string ==~ STARTS_WITH_NUMBER_PATTERN
    }
}
