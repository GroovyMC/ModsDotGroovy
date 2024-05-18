package org.groovymc.modsdotgroovy.types.runner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FilteredStream extends ObjectInputStream {
    private FilteredStream(InputStream input) throws IOException {
        super(input);
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        Class<?> clazz = super.resolveClass(desc);
        if (!allows(clazz)) {
            throw new InvalidClassException("Class not allowed", desc.getName());
        }
        return clazz;
    }

    public static ObjectInputStream filtered(InputStream input) throws IOException {
        return new FilteredStream(input);
    }

    private static boolean allows(Class<?> clazz) {
        if (clazz == null) return false;
        return clazz.isArray() ? allows(clazz.componentType()) : ALLOWED_CLASSES.contains(clazz);
    }

    private static final Set<Class<?>> ALLOWED_CLASSES = Set.of(
            String.class,
            Date.class,

            // Collections
            LinkedHashMap.class,
            HashMap.class,
            ArrayList.class,

            // Primitives
            Integer.class,
            Float.class,
            Double.class,
            Long.class,
            Short.class,
            Byte.class,
            Character.class,
            Boolean.class,
            Number.class,

            // Setup
            URL.class,
            File.class,

            // Lifecycle
            Stop.class,
            Run.class,
            Result.class,
            Failure.class,

            // Error reporting
            StackTraceElement.class
    );

    public static Map<Object, Object> convertToSerializable(final Map<?, ?> map) {
        var result = new LinkedHashMap<>();
        for (var entry : map.entrySet()) {
            result.put(convertToSerializable(entry.getKey()), convertToSerializable(entry.getValue()));
        }
        return result;
    }

    public static Object convertToSerializable(final Object value) {
        if (value instanceof Map<?,?> map) {
            return convertToSerializable(map);
        } else if (value instanceof Collection<?> collection) {
            return convertToSerializable(collection);
        } else if (value instanceof Number || value instanceof Boolean || value instanceof Character || value instanceof String || value instanceof Date) {
            return value;
        } else {
            return value.toString();
        }
    }

    public static List<Object> convertToSerializable(final Collection<?> collection) {
        var result = new ArrayList<>();
        for (var listItem : collection) {
            result.add(convertToSerializable(listItem));
        }
        return result;
    }
}
