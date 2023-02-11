/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

package modsdotgroovy

import groovy.transform.AutoFinal
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovyjarjarantlr4.v4.runtime.misc.Nullable
import org.apache.groovy.lang.annotation.Incubating

@AutoFinal
@Immutable
@Incubating
@CompileStatic
class ImmutableModInfo {
    String modId
    String displayName
    String version
    @Nullable String updateJsonUrl
    @Nullable String displayUrl
    @Nullable String logoFile
    @Nullable String credits
    List<String> authors
    String description

    List<Dependency> dependencies
    Map customProperties
    Map entrypoints
    DisplayTest displayTest

    ImmutableQuiltModInfo quiltModInfo
}
