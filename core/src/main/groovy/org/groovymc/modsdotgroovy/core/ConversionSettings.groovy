package org.groovymc.modsdotgroovy.core

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
class ConversionSettings {
    final OnlineBehavior onlineBehavior

    enum OnlineBehavior {
        ALLOW,
        SKIP,
        ERROR
    }

    static ConversionSettings load(Map map) {
        String behavior = map.onlineBehavior ?: "ALLOW"
        return new ConversionSettings(
                OnlineBehavior.valueOf(behavior.toUpperCase(Locale.ROOT))
        )
    }
}
