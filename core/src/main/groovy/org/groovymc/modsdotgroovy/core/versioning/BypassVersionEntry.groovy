package org.groovymc.modsdotgroovy.core.versioning

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@CompileStatic
@ToString(useGetters = false, includes = ['rawString'])
@EqualsAndHashCode(useGetters = false, includes = ['rawString'])
final class BypassVersionEntry extends VersionRangeEntry {
    final String rawString

    BypassVersionEntry(final String rawString) {
        this.rawString = rawString
    }

    @Override
    String toMaven() {
        return this.rawString
    }

    @Override
    String toSemver() {
        return this.rawString
    }
}
