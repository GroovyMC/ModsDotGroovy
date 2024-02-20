final mdg = SpigotModsDotGroovy.make {
    main = 'org.spigotmc.modsdotgroovyexample.Test'
    name = 'TestPlugin'
    version = '1.0.0'
    description = 'A test plugin'
    load = LoadState.POSTWORLD
    authors = ['md_5', 'Paint_Ninja']
    website = 'https://www.spigotmc.org'
    prefix = 'Testing'

    depend = ['WorldEdit', 'Towny']
    softDepend = ['FAWE']
    loadBefore = ['Essentials']

    commands {
        bish
        bash
        bosh

        command('foo') {
            description = 'Foo command'
            aliases = ['foobar', 'fubar']
            permission {
                node = 'test.foo'
                message = 'You do not have permission!'
            }
            usage = "/${command} [test|stop]"
        }
    }

    permissions {
        'example.permission'

        permission {
            node = 'test.all'
            description = 'All test permissions'
            permissionDefault = PermissionDefault.OP
            children = [
                'test.foo': true,
                'test.bar': false
            ]
        }
    }
}
Map builtMap = mdg.core.build() // for viewing the built map that consumers will receive
println builtMap
return mdg
