package ga.ozli.projects.flexiblemodsdotgroovy

class ModInfoBuilder extends PluginAwareMap {

    ModInfoBuilder(PluginAwareMap parent) {
        super(parent)
    }

    /**
     * The modId of the mod. This should match the value of your mod's {@literal @}GMod/{@literal @}Mod annotated main class.
     */
//    String modId = null

    void setModId(final String modId) {
        put('modId', modId)
    }
}
