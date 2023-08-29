package org.groovymc.modsdotgroovy.core.versioning

import groovy.transform.CompileStatic

@CompileStatic
final class BypassVersionRange extends VersionRange implements CharSequence {
    @Delegate
    private final String rawString

    BypassVersionRange(final String rawString) {
        super(rawString)
        this.rawString = rawString
    }

    @Override
    VersionRange toMaven() {
        return this
    }

    @Override
    VersionRange toSemver() {
        return this
    }

    @Override
    String toMavenString() {
        return rawString
    }

    @Override
    String toSemverString() {
        return rawString
    }

    @Override
    String toString() {
        return rawString
    }
}
