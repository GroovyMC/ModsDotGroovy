package org.groovymc.modsdotgroovy.frontend.multiplatform

import groovy.transform.CompileStatic
import org.groovymc.rootpackagetransformer.RootPackage

@CompileStatic
@RootPackage
enum LoadType {
    /**@
     * If any versions of this mod are present, then one of them will be loaded.
     * Due to how mod loading actually works if any of the different versions of this mod are present, and one of them
     * has "load_type" set to "always", then all of them are treated as it being set to "always".
     */
    ALWAYS,

    /**@
     * If this mod can be loaded, then it will - otherwise it will silently not be loaded.
     */
    IF_POSSIBLE,

    /**@
     * If this mod is in another mods "depends" field then it will be loaded, otherwise it will silently not be loaded.
     */
    IF_REQUIRED

    @Override
    String toString() {
        return name().toLowerCase(Locale.ROOT)
    }

    LoadType() {}
}
