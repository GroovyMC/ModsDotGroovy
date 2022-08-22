package modsdotgroovy

class EntrypointsBuilder {
    Map entrypoints = [:]

    void methodMissing(String name, args) {
        if (args.length() > 1) {
            entrypoints[name] = args
        } else if (args.get(0) instanceof Collection) {
            entrypoints[name] = args.get(0).toList()
        } else {
            entrypoints[name] = [args.get(0)]
        }
    }

    void entrypoint(String name, args) {
        methodMissing(name, args)
    }
}
