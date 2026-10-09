FabricModsDotGroovy.make {
    id = "examplemod"
    version = "1.0.0"

    entrypoints {
        main "net.fabricmc.example.ExampleMod"
        main {
            adapter = "kotlin"
            value = "hello.kotlin.World"
        }
        client "net.fabricmc.example.ExampleModClient"
        rei = "net.fabricmc.example.ReiPlugin"
        entrypoint('fabric:datagen') {
            value = "net.fabricmc.example.ExampleModData"
        }
    }
}
