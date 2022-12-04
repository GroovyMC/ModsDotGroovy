package ga.ozli.projects.flexiblemodsdotgroovy

enum PluginMode {
    /**
     * Completely overwrites the Map - use sparingly!
     */
    OVERWRITE,

    /**
     * Merges the Map with the existing one, keeping any existing values that are not in your Map
     */
    MERGE
}