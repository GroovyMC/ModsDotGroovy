package org.groovymc.modsdotgroovy.core.versioning

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope
import groovy.transform.ToString
import org.jetbrains.annotations.ApiStatus

import java.util.regex.Pattern

@CompileStatic
@ToString(useGetters = false)
@EqualsAndHashCode(useGetters = false)
sealed class VersionRangeEntry permits BypassVersionEntry {
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

    boolean empty = false

    String toMaven() {
        return this.makeString(true)
    }

    String toSemver() {
        return this.makeString(false)
    }

    String makeString(boolean isMaven) {
        if (isMaven) {
            if (empty) return '[]'
            final sb = new StringBuilder()
            sb << (includeLower ? '[' : '(')
            final boolean lowerIsEmpty = lower.isEmpty()
            if (!lowerIsEmpty && upper == lower) {
                sb << lower
            } else {
                sb << (lowerIsEmpty ? '' : lower)
                sb << ','
                sb << (upper.isEmpty() ? '' : upper)
            }
            sb << (includeUpper ? ']' : ')')
            return sb.toString()
        } else {
            final List<String> versionList = []

            if (!lower.empty && lower == upper && includeLower && includeUpper) {
                return "=${lower}"
            }

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
                ? ofMaven(versionRange.trim())
                : ofSemver(versionRange.trim())
    }

    static VersionRangeEntry ofMaven(final String versionRange) {
        final entry = new VersionRangeEntry(
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

    static VersionRangeEntry ofSemver(final String versionRange) {
        // handle hyphen constraints (e.g. "1.0 - 2.0" == ">=1.0.0 <=2.0.0")
        if (versionRange.contains(' - ')) {
            final String[] parts = versionRange.split(' - ')
            return new VersionRangeEntry(
                    lower: parts[0],
                    upper: parts[1],
                    includeLower: true,
                    includeUpper: true
            )
        }

        if (versionRange == '*') {
            return new VersionRangeEntry()
        }

        // handle wildcard constraints (e.g. "1.1.*" == ">=1.1.0 <1.2.0")
        if (versionRange.endsWith('.*') || versionRange.endsWith('.x') || versionRange.endsWith('.X')) {
            String[] parts = versionRange.split('\\.')
            final entry = new VersionRangeEntry()
            if (parts.length >= 2) {
                String secondToLast = parts[parts.length - 2]
                int toInc = secondToLast as int
                if (parts.length === 2) {
                    entry.lower = "${toInc}.0"
                    entry.upper = "${toInc + 1}.0"
                } else {
                    String rest = parts[0..-3].join('.')
                    entry.lower = "${rest}.${toInc}.0"
                    entry.upper = "${rest}.${toInc + 1}.0"
                }
            }
            return entry
        }

        // handle tilde constraints (e.g. "~1.2.3" == ">=1.2.3 <1.3.0")
        if (versionRange.startsWith('~')) {
            def version = versionRange.substring(1)
            String[] parts = version.split('\\.')
            final entry = new VersionRangeEntry(lower: version)
            if (parts.length >= 2) {
                String secondToLast = parts[parts.length - 2]
                int toInc = secondToLast as int
                if (parts.length === 2) {
                    entry.upper = "${toInc + 1}.0"
                } else {
                    String rest = parts[0..-3].join('.')
                    entry.upper = "${rest}.${toInc + 1}.0"
                }
            }
            return entry
        }

        // handle caret constraints (e.g. "^1.2.3" == ">=1.2.3 <2.0.0")
        if (versionRange.startsWith('^')) {
            def version = versionRange.substring(1)
            String[] parts = version.split('\\.')
            final entry = new VersionRangeEntry(lower: version)
            if (parts.size() > 1) {
                if (parts[0].chars.every { it == '0' }) {
                    String first = parts[0]
                    String second = parts.size() > 1 ? parts[1] : '0'
                    int toInc = second as int
                    entry.upper = "${first}.${toInc + 1}${parts.size() > 2 ? ".0".repeat(parts.size() - 2) : ''}"
                } else {
                    String first = parts[0]
                    int toInc = first as int
                    entry.upper = "${toInc + 1}${".0".repeat(parts.size() - 1)}"
                }
            }
            return entry
        }

        // handle =, >=, <=, >, and < constraints
        final entry = new VersionRangeEntry()
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

    VersionRange and(VersionRangeEntry v) {
        return new VersionRange.SingleVersionRange(this) & new VersionRange.SingleVersionRange(v)
    }

    VersionRange or(VersionRangeEntry v) {
        return new VersionRange.SingleVersionRange(this) | new VersionRange.SingleVersionRange(v)
    }

    VersionRange and(VersionRange v) {
        return new VersionRange.SingleVersionRange(this) & v
    }

    VersionRange or(VersionRange v) {
        return new VersionRange.SingleVersionRange(this) | v
    }
}
