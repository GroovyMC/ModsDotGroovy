package org.groovymc.modsdotgroovy.core

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam

@CompileStatic
final class OnPutTransform {
    final Class<?> type
    final Closure<?> transformer

    private OnPutTransform(final Class<?> type, final Closure<?> transformer) {
        this.type = type
        this.transformer = transformer
    }

    static <T> OnPutTransform of(
            final Class<T> type,
            @ClosureParams(value = FirstParam.FirstGenericType) final Closure<?> transformer
    ) {
        return new OnPutTransform(type, transformer)
    }

    def transform(final Object value) {
        if (type.isInstance(value)) {
            return transformer.call(value)
        }
        return value
    }
}
