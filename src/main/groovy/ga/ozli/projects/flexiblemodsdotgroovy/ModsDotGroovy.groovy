package ga.ozli.projects.flexiblemodsdotgroovy

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * This is the frontend layer
 */
@CompileStatic
class ModsDotGroovy {
    final ModsDotGroovyCore data = ModsDotGroovyCore.INSTANCE

    void propertyMissing(String name, Object value) {
        data.put(name, value)
    }

    void put(String name, Object value) {
        data.put(name, value)
    }

    /**
     * The license for your mod. This is mandatory metadata and allows for easier comprehension of your redistributive properties.<br>
     * Review your options at <a href="https://choosealicense.com/">https://choosealicense.com/</a>. <br>
     * All rights reserved is the default copyright stance, and is thus the default here.
     */
    void setLicense(String license) {
        data.put('license', license)
    }

    /**
     * A URL to refer people to when problems occur with this mod.
     */
    void setIssueTrackerUrl(String issueTrackerUrl) {
        data.put('issueTrackerUrl', issueTrackerUrl)
    }

    /**
     * The name of the mod loader type to load - for regular Java FML @Mod mods it should be {@code javafml}.
     * For GroovyModLoader @GMod mods it should be {@code gml}.
     */
    void setModLoader(String modLoader) {
        data.put('modLoader', modLoader)
    }

    void onQuilt(Closure closure) {}
    void onFabric(Closure closure) {}

    void onForge(@DelegatesTo(value = ModsDotGroovy, strategy = DELEGATE_FIRST) Closure closure) {
        data.put('onForge', closure)
    }

    void mods(@DelegatesTo(value = ModsBuilder, strategy = DELEGATE_FIRST)
              @ClosureParams(value = SimpleType, options = 'modsdotgroovy.ModsBuilder') final Closure closure) {
        final modsBuilder = new ModsBuilder()
        closure.delegate = modsBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(modsBuilder)
        data.put('mods', modsBuilder.mods)
    }

    static synchronized ModsDotGroovy make(@DelegatesTo(value = ModsDotGroovy, strategy = DELEGATE_FIRST) Closure closure) {
        final ModsDotGroovy val = new ModsDotGroovy()
        closure.resolveStrategy = DELEGATE_FIRST
        closure.delegate = val
        closure.call(val)
        val.data.build()
        return val
    }






    /**
     * This is the toString()'ed data currently outputted from ModsDotGroovy.make() (before FlexibleModsDotGroovy)
     */
//    protected Map currentData = [
//            modLoader: 'gml',
//            loaderVersion: '[1,)',
//            license: 'MIT',
//            mods: [
//                    [
//                            modId: 'no',
//                            version: 1.190,
//                            displayName: 'No',
//                            updateJsonUrl: null,
//                            displayUrl: null,
//                            credits: 'hello_world',
//                            logoFile: null,
//                            description: '',
//                            authors: 'Matyrobbrt and Paint_Ninja'
//                    ]
//            ],
//            dependencies: [
//                    no: [
//                            [
//                                    mandatory:true,
//                                    versionRange: '[1.1,)',
//                                    ordering: 'AFTER',
//                                    side: 'BOTH',
//                                    modId: 'patchouli'
//                            ],
//                            [
//                                    mandatory:true,
//                                    versionRange: '[1.0.1,)',
//                                    ordering: 'NONE',
//                                    side: 'CLIENT',
//                                    modId: 'dynamic_asset_generator'
//                            ],
//                            [
//                                    mandatory:true,
//                                    versionRange:'[43.0.0,)',
//                                    ordering:NONE,
//                                    side:BOTH,
//                                    modId:forge
//                            ],
//                            [
//                                    mandatory:true,
//                                    versionRange:'[1.19,1.20)',
//                                    ordering:NONE,
//                                    side:BOTH,
//                                    modId:minecraft
//                            ]
//                    ]
//            ],
//            modproperties: [
//                    no: [
//                            customProperty: hello
//                    ]
//            ]
//    ]
}
