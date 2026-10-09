FabricModsDotGroovy.make {
    id = "examplemod"
    version = "1.0.0"

    depends {
        fabricloader = ">=${environmentInfo.platformVersion}"
        minecraft = "~1.20.1"
        java = ">=17"
        it.'fabric-api' = "*"

        mod "another-mod", ">=1.5.0"
        mod("something-else", v(">0.5") & v('<1.0'))
        mod {
            modId = "rats"
            versionRange = "*"
        }
    }

    recommends {
        example_foss_mod = "*"
    }

    suggests {
        another_nice_mod = [">=0.5.0", '<0.7.8']
    }

    breaks {
        example_arr_mod = "*"
    }

    conflicts {
        naughty_mod = "*"
    }
}
