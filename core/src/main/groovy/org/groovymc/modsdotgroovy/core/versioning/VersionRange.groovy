package org.groovymc.modsdotgroovy.core.versioning

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode

@CompileStatic
@EqualsAndHashCode(useGetters = false, includeFields = true)
sealed class VersionRange permits BypassVersionRange {
    private final List<VersionRangeEntry> versions = []

    VersionRange(final String string) {
        // split on ", " or " " (but not on " - " or ",")
        final String[] entries = string.split(/(?<!-), | /)
        for (final entry in entries) {
            this.versions << VersionRangeEntry.of(entry)
        }
    }

    VersionRange(final String[] strings) {
        for (final string in strings) {
            this.versions << VersionRangeEntry.of(string)
        }
    }

    VersionRange(final List<String> strings) {
        this(strings as String[])
    }

    VersionRange toMaven() {
        versions*.toMaven()
        return this
    }

    VersionRange toSemver() {
        versions*.toSemver()
        return this
    }

    /**
     * Shorthand for {@code toMaven().toString()}.
     */
    String toMavenString() {
        return versions*.toMaven().join(',')
    }

    /**
     * Shorthand for {@code toSemver().toString()}.
     */
    String toSemverString() {
        return versions*.toSemver().join(' ')
    }

    @Override
    String toString() {
        return versions.join(versions[0]?.isMaven ? ',' : ' ')
    }
}
