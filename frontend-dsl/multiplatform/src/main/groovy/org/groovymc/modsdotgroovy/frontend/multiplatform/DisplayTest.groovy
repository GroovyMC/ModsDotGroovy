package org.groovymc.modsdotgroovy.frontend.multiplatform

import groovy.transform.CompileStatic
import org.groovymc.rootpackagetransformer.RootPackage

@CompileStatic
@RootPackage
enum DisplayTest {
    /**
     * (default) A red "X" will be displayed on the server connection screen if the version of this mod differs between the
     * client and server.
     */
    MATCH_VERSION,

    /**
     * When determining whether to show a red "X" on the server connection screen, if and only if this mod is present on
     * the server but not the client, it will be ignored.
     */
    IGNORE_SERVER_VERSION,

    /**
     * This mod will be ignored entirely when determining whether to show a red "X" on the server connection screen.
     */
    IGNORE_ALL_VERSION,

    /**
     * You are setting your own display test for your mod using Forge's API.
     */
    NONE

    DisplayTest() {}
}
