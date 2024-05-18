package org.groovymc.modsdotgroovy.core.versioning

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.groovymc.modsdotgroovy.types.internal.FlexVerComparator

@CompileStatic
sealed abstract class VersionRange permits AndVersionRange, OrVersionRange, SingleVersionRange {
    static VersionRange of(final String string) {
        final List<String> or = splitMavenParts(string).collect { it.trim() }.findAll { !it.blank }
        final List<List<String>> and = or.collect { splitSemverParts(it).collect { it.trim() }.findAll { !it.blank } }.findAll { !it.empty }
        final List<VersionRange> andRanges = and.collect { andRange ->
            if (andRange.size() === 1) {
                return new SingleVersionRange(VersionRangeEntry.of(andRange.get(0)))
            }
            return new AndVersionRange(andRange.collect { (VersionRange) new SingleVersionRange(VersionRangeEntry.of(it)) })
        }

        if (andRanges.size() === 1) {
            return andRanges.get(0)
        }

        return new OrVersionRange(andRanges)
    }

    VersionRange and(final VersionRange other) {
        return new AndVersionRange([this, other])
    }

    VersionRange or(final VersionRange other) {
        return new OrVersionRange([this, other])
    }

    abstract VersionRange bitwiseNegate()

    private static List<String> splitMavenParts(final String string) {
        // Split on commas outside of version ranges
        boolean inRange = false
        StringBuilder current = new StringBuilder()
        List<String> parts = new ArrayList<>()
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i)
            if (c == '[' as char || c == '(' as char) {
                inRange = true
            } else if (c == ']' as char || c == ')' as char) {
                inRange = false
            }
            if (c == ',' as char && !inRange) {
                parts.add(current.toString())
                current = new StringBuilder()
            } else {
                current.append(c)
            }
        }
        parts.add(current.toString())
        return parts
    }

    private static List<String> splitSemverParts(final String string) {
        // Split on spaces outside of version ranges
        boolean inRange = false
        StringBuilder current = new StringBuilder()
        List<String> parts = new ArrayList<>()
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i)
            if (c == '[' as char || c == '(' as char) {
                inRange = true
            } else if (c == ']' as char || c == ')' as char) {
                inRange = false
            }
            if (c.whitespace && !inRange) {
                parts.add(current.toString())
                current = new StringBuilder()
            } else {
                current.append(c)
            }
        }
        parts.add(current.toString())
        return parts
    }

    String toMaven() {
        return collapse().collapsedToMaven()
    }

    List<String> toSemver() {
        return collapse().collapsedToSemver()
    }

    abstract protected String collapsedToMaven()

    abstract protected List<String> collapsedToSemver()
    abstract protected String collapsedToSemverSingle()

    /**
     * Collapse the version range into a form using only "or" at the top level, with "and" below that, and "single" below that.
     */
    abstract VersionRange collapse();

    @ToString(useGetters = false)
    @EqualsAndHashCode(useGetters = false)
    final static class SingleVersionRange extends VersionRange {
        final VersionRangeEntry version

        SingleVersionRange(final VersionRangeEntry version) {
            this.version = version
        }

        @Override
        protected String collapsedToMaven() {
            return version.toMaven()
        }

        @Override
        protected List<String> collapsedToSemver() {
            return List.of(version.toSemver())
        }

        @Override
        protected String collapsedToSemverSingle() {
            return version.toSemver()
        }

        @Override
        VersionRange collapse() {
            // Already collapsed
            return this
        }

        @Override
        VersionRange and(VersionRange other) {
            if (other instanceof AndVersionRange) {
                List<VersionRange> vs = new ArrayList<>(other.versions)
                vs.add(this)
                return new AndVersionRange(vs)
            }
            return super.and(other)
        }

        @Override
        VersionRange or(VersionRange other) {
            if (other instanceof OrVersionRange) {
                List<VersionRange> vs = new ArrayList<>(other.versions)
                vs.add(this)
                return new OrVersionRange(vs)
            }
            return super.or(other)
        }

        @Override
        VersionRange bitwiseNegate() {
            if (version instanceof BypassVersionEntry) {
                throw new IllegalArgumentException("Cannot negate a bypass version entry")
            }
            if (version.empty) {
                return new SingleVersionRange(new VersionRangeEntry())
            } else if (version.upper.empty) {
                if (version.lower.empty) {
                    return new SingleVersionRange(new VersionRangeEntry(empty: true))
                }
                return new SingleVersionRange(new VersionRangeEntry(upper: version.lower, includeUpper: !version.includeLower))
            } else if (version.lower.empty) {
                return new SingleVersionRange(new VersionRangeEntry(lower: version.upper, includeLower: !version.includeUpper))
            } else {
                return new OrVersionRange([
                        new VersionRangeEntry(upper: version.lower, includeUpper: !version.includeLower),
                        new VersionRangeEntry(lower: version.upper, includeLower: !version.includeUpper)
                ].collect { (VersionRange) new SingleVersionRange(it) })
            }
        }
    }

    @ToString(useGetters = false)
    @EqualsAndHashCode(useGetters = false)
    final static class OrVersionRange extends VersionRange {
        final List<VersionRange> versions = []

        OrVersionRange(final List<VersionRange> versions) {
            this.versions.addAll(versions)
        }

        @Override
        protected String collapsedToMaven() {
            return versions.collect { it.collapsedToMaven() }.join(',')
        }

        @Override
        protected List<String> collapsedToSemver() {
            return versions.collect { it.collapsedToSemverSingle() }
        }

        @Override
        protected String collapsedToSemverSingle() {
            throw new UnsupportedOperationException("Cannot collapse an 'or' version range to a single semver string; should be collapsed to a list instead")
        }

        @Override
        VersionRange collapse() {
            def children = new ArrayList<>(versions)
            List<VersionRange> vs = new ArrayList<>()
            for (int i = 0; i < versions.size(); i++) {
                def v = versions.get(i)
                if (v instanceof OrVersionRange) {
                    children.addAll(v.versions)
                } else if (v instanceof AndVersionRange) {
                    vs.add(v.collapse())
                } else {
                    vs.add(v)
                }
            }
            return new OrVersionRange(vs)
        }

        @Override
        VersionRange or(VersionRange other) {
            List<VersionRange> vs = new ArrayList<>(this.versions)
            if (other instanceof OrVersionRange) {
                vs.addAll(other.versions)
            } else {
                vs.add(other)
            }
            return new OrVersionRange(vs)
        }

        @Override
        VersionRange bitwiseNegate() {
            return new AndVersionRange(versions.collect { ~it })
        }
    }

    @ToString(useGetters = false)
    @EqualsAndHashCode(useGetters = false)
    final static class AndVersionRange extends VersionRange {
        final List<VersionRange> versions = []

        AndVersionRange(final List<VersionRange> versions) {
            this.versions.addAll(versions)
        }

        @Override
        protected String collapsedToMaven() {
            VersionRangeEntry range = null
            for (VersionRange version : versions) {
                SingleVersionRange single = version as SingleVersionRange
                if (range === null) {
                    range = single.version
                } else {
                    String lower1 = range.lower
                    String lower2 = single.version.lower
                    String upper1 = range.upper
                    String upper2 = single.version.upper
                    boolean includeLower1 = range.includeLower
                    boolean includeLower2 = single.version.includeLower
                    boolean includeUpper1 = range.includeUpper
                    boolean includeUpper2 = single.version.includeUpper

                    if (
                            single.version instanceof BypassVersionEntry ||
                            range.empty || single.version.empty ||
                            (!lower2.empty && !upper1.empty && FlexVerComparator.compare(lower2, upper1) > 0) ||
                            (!lower1.empty && !upper2.empty && FlexVerComparator.compare(lower1, upper2) > 0)
                    ) {
                        // ranges are non-overlapping, or contain incomparable bypassversionentry
                        range = new VersionRangeEntry()
                        range.includeUpper = true
                        range.empty = true
                        break
                    }

                    if (lower1.empty) {
                        range.lower = lower2
                        range.includeLower = includeLower2
                    } else if (!lower2.empty) {
                        if (FlexVerComparator.compare(lower1, lower2) < 0) {
                            // lower1 is less than lower2
                            range.lower = lower2
                            range.includeLower = includeLower2
                        } else if (FlexVerComparator.compare(lower1, lower2) == 0) {
                            // lower1 is equal to lower2
                            range.includeLower = includeLower1 && includeLower2
                        }
                    }

                    if (upper1.empty) {
                        range.upper = upper2
                        range.includeUpper = includeUpper2
                    } else if (!upper2.empty) {
                        if (FlexVerComparator.compare(upper1, upper2) > 0) {
                            // upper1 is greater than upper2
                            range.upper = upper2
                            range.includeUpper = includeUpper2
                        } else if (FlexVerComparator.compare(upper1, upper2) == 0) {
                            // upper1 is equal to upper2
                            range.includeUpper = includeUpper1 && includeUpper2
                        }
                    }
                }
            }
            return range.toMaven()
        }

        @Override
        protected List<String> collapsedToSemver() {
            return List.of(collapsedToSemverSingle())
        }

        @Override
        protected String collapsedToSemverSingle() {
            if (versions.any { (it as SingleVersionRange).version.empty }) {
                return '>1 <1'
            }
            return versions.collect {
                SingleVersionRange single = it as SingleVersionRange
                single.version.toSemver()
            }.join(' ')
        }

        @Override
        VersionRange and(VersionRange other) {
            List<VersionRange> vs = new ArrayList<>(this.versions)
            if (other instanceof AndVersionRange) {
                vs.addAll(other.versions)
            } else {
                vs.add(other)
            }
            return new AndVersionRange(vs)
        }

        @Override
        VersionRange bitwiseNegate() {
            return new OrVersionRange(versions.collect { ~it })
        }

        @Override
        VersionRange collapse() {
            def children = new ArrayList<>(versions)
            List<VersionRange> vs = new ArrayList<>()
            List<List<VersionRange>> splits = new ArrayList<>()
            for (int i = 0; i < versions.size(); i++) {
                def v = versions.get(i)
                if (v instanceof AndVersionRange) {
                    children.addAll(v.versions)
                } else if (v instanceof OrVersionRange) {
                    splits.add(v.versions)
                } else {
                    vs.add(v)
                }
            }
            List<AndVersionRange> alternatives = new ArrayList<>()
            alternatives.add(new AndVersionRange(vs))
            for (List<VersionRange> split : splits) {
                List<AndVersionRange> newAlternatives = new ArrayList<>()
                for (AndVersionRange alt : alternatives) {
                    for (VersionRange v : split) {
                        newAlternatives.add(new AndVersionRange(alt.versions + v))
                    }
                }
                alternatives = newAlternatives
            }
            return new OrVersionRange(alternatives.collect { (VersionRange) new AndVersionRange(it.versions.collect { it.collapse() }) })
        }
    }
}
