package modsdotgroovy

import groovy.transform.CompileStatic

@CompileStatic
class EntrypointsBuilder {
    Map entrypoints = [:]

    void propertyMissing(String name, value) {
        if (value instanceof Collection) {
            entrypoints[name] = value.toList()
        } else {
            entrypoints[name] = [value]
        }
    }

    void entrypoint(String name, args) {
        propertyMissing(name, args)
    }
}
