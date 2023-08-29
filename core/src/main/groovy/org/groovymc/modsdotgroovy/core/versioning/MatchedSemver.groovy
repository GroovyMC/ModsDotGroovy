package org.groovymc.modsdotgroovy.core.versioning

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.jetbrains.annotations.Nullable

import java.util.regex.Matcher

@PackageScope
@CompileStatic
record MatchedSemver(int major, int minor, int patch) {
    static MatchedSemver of(final Matcher matcher) {
        matcher.find()

        @Nullable final String majorStr = matcher.group('major')
        @Nullable final String minorStr = matcher.group('minor')
        @Nullable final String patchStr = matcher.group('patch')

        final int major = toIntWithFallback(majorStr, 0)
        final int minor = partHasWildcard(minorStr) ? -1 : toIntWithFallback(minorStr, 0)
        final int patch = partHasWildcard(patchStr) ? -1 : toIntWithFallback(patchStr, 0)

        return new MatchedSemver(major, minor, patch)
    }

    private static int toIntWithFallback(@Nullable final String string, final int fallback) {
        if (string === null || string.isEmpty()) return fallback
        else return string as int
    }

    private static boolean partHasWildcard(@Nullable final String string) {
        return string?.contains('*') || string?.contains('x')
    }

    String toString() {
        return "${major}.${minor}.${patch}"
    }
}
