FabricModsDotGroovy.make {
    id = "examplemod"
    version = "1.0.0"

    mixins {
        mixin "modid.mixins.json"
        mixin "modid-client.mixins.json", Environment.CLIENT
        mixin {
            config = "modid-server.mixins.json"
            environment = Environment.SERVER
        }
    }
}
