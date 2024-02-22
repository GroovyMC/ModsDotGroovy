package org.groovymc.modsdotgroovy.core

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam

@CompileStatic
record MapTransform<T>(Class<T> type, @ClosureParams(FirstParam.FirstGenericType) Closure<?> transformer) {
    static <T> MapTransform of(
            final Class<T> type,
            @ClosureParams(FirstParam.FirstGenericType) final Closure<?> transformer
    ) {
        return new MapTransform(type, transformer)
    }

    def transform(final def value) {
        if (type.isInstance(value)) {
            return transformer.call(value)
        }
        return value
    }
}
