package ga.ozli.projects.flexiblemodsdotgroovy

class ModInfoBuilder extends PluginAwareMap {

    protected ModInfoBuilder(PluginAwareMap parent) {
        super(parent)
    }

    /**
     * The modId of the mod. This should match the value of your mod's {@literal @}GMod/{@literal @}Mod annotated main class.
     */
    String modId = null

    void setModId(String modId) {
        this.modId = modId
    }
}
