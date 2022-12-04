package modsdotgroovy

import groovy.transform.CompileStatic

@CompileStatic
class MapBackend {
    protected final Map data = [:]
    void put(String key, Object value) {
        data[key] = value
    }
    void propertyMissing(String name, Object value) {
        data[name] = value
    }

    <T> T asType(Class<T> type) {
        if (type == Map.class) return (T)data
        return type.cast(this)
    }
}
