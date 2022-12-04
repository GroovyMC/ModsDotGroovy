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
    final ModsDotGroovyCore core = ModsDotGroovyCore.INSTANCE

    void propertyMissing(String name, Object value) {
        core.put(name, value)
    }

    void put(String name, Object value) {
        core.put(name, value)
    }

    /**
     * The license for your mod. This is mandatory metadata and allows for easier comprehension of your redistributive properties.<br>
     * Review your options at <a href="https://choosealicense.com/">https://choosealicense.com/</a>. <br>
     * All rights reserved is the default copyright stance, and is thus the default here.
     */
    void setLicense(final String license) {
        core.put('license', license)
    }

    /**
     * A URL to refer people to when problems occur with this mod.
     */
    void setIssueTrackerUrl(final String issueTrackerUrl) {
        core.put('issueTrackerUrl', issueTrackerUrl)
    }

    /**
     * The name of the mod loader type to load - for regular Java FML @Mod mods it should be {@code javafml}.
     * For GroovyModLoader @GMod mods it should be {@code gml}.
     */
    void setModLoader(final String modLoader) {
        core.put('modLoader', modLoader)
    }

    void onQuilt(final Closure closure) {
        core.put('onQuilt', closure)
    }

    void onFabric(final Closure closure) {
        core.put('onFabric', closure)
    }

    void onForge(@DelegatesTo(value = ModsDotGroovy, strategy = DELEGATE_FIRST) final Closure closure) {
        core.put('onForge', closure)
    }

    void mods(@DelegatesTo(value = ModsBuilder, strategy = DELEGATE_FIRST)
              @ClosureParams(value = SimpleType, options = 'modsdotgroovy.ModsBuilder') final Closure closure) {
        core.put('mods', new Tuple2<PluginAwareMap, Closure>(core, closure))
    }

    static synchronized ModsDotGroovy make(@DelegatesTo(value = ModsDotGroovy, strategy = DELEGATE_FIRST) final Closure closure) {
        final ModsDotGroovy val = new ModsDotGroovy()
        closure.resolveStrategy = DELEGATE_FIRST
        closure.delegate = val
        closure.call(val)
        val.core.build()
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
