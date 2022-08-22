package modsdotgroovy

class EntrypointsBuilder {
    Map entrypoints = [:]

    def methodMissing(String name, args) {
        if (args.length() > 1) {
            entrypoints[name] = args
        } else if (args.get(0) instanceof Collection) {
            entrypoints[name] = args.get(0).toList()
        } else {
            entrypoints[name] = [args.get(0)]
        }
    }

    Map build() {
        return entrypoints
    }
}
