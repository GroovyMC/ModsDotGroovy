package org.groovymc.modsdotgroovy.core.versioning

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope
import org.jetbrains.annotations.ApiStatus

import java.util.regex.Pattern

@PackageScope
@CompileStatic
@EqualsAndHashCode(useGetters = false)
sealed class VersionRangeEntry permits MavenVersionRangeEntry, SemverVersionRangeEntry {
    /**@
     * The lower bound of the version range. An empty string indicates no lower bound.
     */
    String lower = ''

    /**@
     * The upper bound of the version range. An empty string indicates no upper bound.
     */
    String upper = ''

    /**@
     * Whether the lower bound is considered a part of the version range.
     */
    boolean includeLower = true

    /**@
     * Whether the upper bound is considered a part of the version range.
     */
    boolean includeUpper = false

    @ApiStatus.Internal
    protected boolean isMaven = true

    VersionRangeEntry toMaven() {
        isMaven = true
        return this
    }

    VersionRangeEntry toSemver() {
        isMaven = false
        return this
    }

    @Override
    String toString() {
        if (isMaven) {
            final sb = new StringBuilder()
            sb << (includeLower ? '[' : '(')
            final boolean lowerIsEmpty = lower.isEmpty()
            if (!lowerIsEmpty && upper == lower) {
                sb << lower
            } else {
                sb << (lowerIsEmpty ? '0' : lower)
                sb << ','
                sb << (upper.isEmpty() ? '' : upper)
            }
            sb << (includeUpper ? ']' : ')')
            return sb.toString()
        } else {
            final List<String> versionList = []

            if (!lower.isEmpty())
                versionList << (includeLower ? '>=' : '>') + lower

            if (!upper.isEmpty())
                versionList << (includeUpper ? '<=' : '<') + upper

            if (versionList.isEmpty())
                return '*'

            return versionList.join(' ')
        }
    }

    private static final Pattern IS_MAVEN = Pattern.compile(/^[\[(].*[)\]]$/)

    static VersionRangeEntry of(final String versionRange) {
        return versionRange ==~ IS_MAVEN
                ? MavenVersionRangeEntry.of(versionRange)
                : SemverVersionRangeEntry.of(versionRange)
    }

    final static class MavenVersionRangeEntry extends VersionRangeEntry {
        static VersionRangeEntry of(final String versionRange) {
            final entry = new VersionRangeEntry(
                    isMaven: true,
                    includeLower: versionRange.startsWith('['),
                    includeUpper: versionRange.endsWith(']')
            )

            final int indexOfComma = versionRange.indexOf(',')
            if (indexOfComma === -1) {
                entry.lower = entry.upper = versionRange.substring(1, versionRange.length() - 1)
            } else {
                entry.lower = versionRange.substring(1, indexOfComma)
                entry.upper = versionRange.substring(indexOfComma + 1, versionRange.length() - 1)
            }

            return entry
        }

        private MavenVersionRangeEntry() {}
    }

    final static class SemverVersionRangeEntry extends VersionRangeEntry {
        private static final Pattern SEMVER_WITH_WILDCARDS =
                ~/^(?<major>[0-9]+)(\.(?<minor>[0-9]+|[x*]))?(\.(?<patch>[0-9]+|[x*]))?(?:-(?<pre>[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?(?:\+(?<meta>[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?$/
        private static final Pattern SEMVER_PATCH_OPTIONAL =
                ~/^(?<major>[0-9]+)\.(?<minor>[0-9]+)(\.(?<patch>[0-9]+))?(?:-(?<pre>[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?(?:\+(?<meta>[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?$/

        static VersionRangeEntry of(final String versionRange) {
            // handle hyphen constraints (e.g. "1.0 - 2.0" == ">=1.0.0 <=2.0.0")
            if (versionRange.contains(' - ')) {
                final String[] parts = versionRange.split(' - ')
                return new VersionRangeEntry(
                        isMaven: false,
                        lower: parts[0],
                        upper: parts[1],
                        includeLower: true,
                        includeUpper: true
                )
            }

            // handle wildcard constraints (e.g. "1.1.*" == ">=1.1.0 <1.2.0")
            if (versionRange.contains('.*') || versionRange.contains('.x')) {
                final matchedSemver = MatchedSemver.of(versionRange =~ SEMVER_WITH_WILDCARDS)
                final entry = new VersionRangeEntry(isMaven: false)
                if (matchedSemver.minor() === -1) {
                    entry.lower = matchedSemver.major() + '.0.0'
                    entry.upper = "${matchedSemver.major() + 1}.0.0"
                } else if (matchedSemver.patch() === -1) {
                    final GString mutableString = "${matchedSemver.major()}.${matchedSemver.minor()}.0"
                    entry.lower = mutableString
                    mutableString.values[1] = (mutableString.values[1] as int) + 1
                    entry.upper = mutableString
                } else {
                    entry.lower = entry.upper = versionRange
                    entry.includeUpper = true
                }
                return entry
            }

            // handle tilde constraints (e.g. "~1.2.3" == ">=1.2.3 <1.3.0")
            if (versionRange.startsWith('~')) {
                final entry = new VersionRangeEntry(isMaven: false, lower: versionRange.substring(1))
                final matchedSemver = MatchedSemver.of(entry.lower =~ SEMVER_PATCH_OPTIONAL)
                entry.upper = "${matchedSemver.major()}.${matchedSemver.minor() + 1}.0"
                return entry
            }

            // handle caret constraints (e.g. "^1.2.3" == ">=1.2.3 <2.0.0")
            if (versionRange.startsWith('^')) {
                final entry = new VersionRangeEntry(isMaven: false, lower: versionRange.substring(1))
                final matchedSemver = MatchedSemver.of(entry.lower =~ SEMVER_PATCH_OPTIONAL)
                if (matchedSemver.major() === 0) {
                    entry.upper = matchedSemver.minor() === 0
                            ? versionRange
                            : "0.${matchedSemver.minor() + 1}.0"
                } else {
                    entry.upper = "${matchedSemver.major() + 1}.0.0"
                }
                return entry
            }

            // handle =, >=, <=, >, and < constraints
            final entry = new VersionRangeEntry(isMaven: false)
            if (versionRange.startsWith('>=')) {
                entry.lower = versionRange.substring(2)
            } else if (versionRange.startsWith('<=')) {
                entry.upper = versionRange.substring(2)
                entry.includeUpper = true
            } else if (versionRange.startsWith('>')) {
                entry.lower = versionRange.substring(1)
                entry.includeLower = false
            } else if (versionRange.startsWith('<')) {
                entry.upper = versionRange.substring(1)
            } else if (versionRange.startsWith('=')) {
                entry.lower = entry.upper = versionRange.substring(1)
                entry.includeLower = entry.includeUpper = true
            } else {
                entry.lower = entry.upper = versionRange
                entry.includeLower = entry.includeUpper = true
            }

            return entry
        }

        private SemverVersionRangeEntry() {}
    }
}
