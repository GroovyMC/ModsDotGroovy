package org.groovymc.modsdotgroovy.core

import groovy.transform.CompileStatic

@CompileStatic
enum Platform {
    FORGE,
    FABRIC,
    QUILT,
    UNKNOWN

    @Override
    String toString() {
        return name().toLowerCase(Locale.ROOT).capitalize()
    }

    static Platform of(final String name) {
        return valueOf(name.toUpperCase(Locale.ROOT))
    }

    Platform() {}
}
