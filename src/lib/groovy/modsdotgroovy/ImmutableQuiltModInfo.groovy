/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

package modsdotgroovy

import groovy.transform.AutoFinal
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import org.apache.groovy.lang.annotation.Incubating

@AutoFinal
@Immutable
@Incubating
@CompileStatic
class ImmutableQuiltModInfo {
    String intermediateMappings
    List<Dependency> breaks
    Map<String, String> contact
}
