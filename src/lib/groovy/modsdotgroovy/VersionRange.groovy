package modsdotgroovy

import groovy.transform.AutoFinal
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovyjarjarantlr4.v4.runtime.misc.Nullable

import java.util.regex.Matcher
import java.util.regex.Pattern

@CompileStatic
class VersionRange {
    List<SingleVersionData> versions = []

    static class SingleVersionData {
        String lower = ''
        String upper = ''
        boolean includeLower = true
        boolean includeUpper = false

        void lower(String lower) {
            this.lower = lower
        }

        void upper(String upper) {
            this.upper = upper
        }

        void includeLower(boolean include) {
            this.includeLower = include
        }

        void includeUpper(boolean include) {
            this.includeUpper = include
        }

        String toForge() {
            if (lower == upper) {
                if (lower == '')
                    return '[0,)'
                return lower
            }
            else return (includeLower?'[':'(') + lower + ',' + upper + (includeUpper?']':')')
        }

        String toQuilt() {
            final List versionList = []
            if (lower != '') {
                versionList.add((includeLower?">":">=")+lower)
            }
            if (upper != '') {
                versionList.add((includeUpper?"<=":"<")+upper)
            }
            if (versionList.size()==0) return "*"
            return String.join(' ',versionList as List<String>)
        }
    }

    String toForge() {
        return String.join(',',versions.collect {it.toForge()})
    }

    List toQuilt() {
        return versions.collect {it.toQuilt()}
    }

    static VersionRange of(String version) {
        if (version.contains('[') || version.contains(']') || version.contains('(') || version.contains(')')) {
            return ofMaven(version)
        }
        return ofSemVer(version)
    }

    private static final Pattern SEMVER_ALL_OPTIONAL =
            ~/^(?<major>[0-9]+)(\.(?<minor>[0-9]+))?(\.(?<patch>[0-9]+))?(?:-(?<pre>[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?(?:\+(?<meta>[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?$/
    private static final Pattern SEMVER_WITH_WILDCARDS =
            ~/^(?<major>[0-9]+)(\.(?<minor>[0-9]+|[x*]))?(\.(?<patch>[0-9]+|[x*]))?(?:-(?<pre>[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?(?:\+(?<meta>[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?$/
    private static final Pattern SEMVER_PATCH_OPTIONAL =
            ~/^(?<major>[0-9]+)\.(?<minor>[0-9]+)(\.(?<patch>[0-9]+))?(?:-(?<pre>[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?(?:\+(?<meta>[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?$/
    private static final Pattern SEMVER_DASH_RANGE =
            ~/[0-9]+(\.[0-9]+)?(\.[0-9]+)?(?:-[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*)?(?:\+[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*)? +- +[0-9]+(\.[0-9]+)?(\.[0-9]+)?(?:-[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*)?(?:\+[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*)?/

    static VersionRange ofSemVer(String quiltVersion) {
        final VersionRange data = new VersionRange()

        for (String part : quiltVersion.split(/\|\|/)) {
            part = part.trim()
            String[] ss = part.split(/(?<!( -)| )( +)(?!(- )| )/)
            if (ss.every {it == '*' || it == 'x'}) {
                data.versions.add(new SingleVersionData())
            }
            SingleVersionData working = new SingleVersionData()
            for (String sOrig : ss) {
                String s = sOrig
                s = s.trim()
                if (s.isEmpty() || s == '*' || s == 'x') {
                    continue
                }

                if (s.startsWith('>=')) {
                    s = s.substring(2)
                    if (s.matches(SEMVER_ALL_OPTIONAL)) {
                        working.includeLower = true
                        working.lower = s.find(SEMVER_ALL_OPTIONAL)
                        continue
                    }
                } else if (s.startsWith('<=')) {
                    s = s.substring(2)
                    if (s.matches(SEMVER_ALL_OPTIONAL)) {
                        working.includeUpper = true
                        working.upper = s.find(SEMVER_ALL_OPTIONAL)
                        continue
                    }
                } else if (s.startsWith('>')) {
                    s = s.substring(1)
                    if (s.matches(SEMVER_ALL_OPTIONAL)) {
                        working.includeLower = false
                        working.lower = s.find(SEMVER_ALL_OPTIONAL)
                        continue
                    }
                } else if (s.startsWith('<')) {
                    s = s.substring(1)
                    if (s.matches(SEMVER_ALL_OPTIONAL)) {
                        working.includeUpper = false
                        working.upper = s.find(SEMVER_ALL_OPTIONAL)
                        continue
                    }
                } else if (s.startsWith('=')) {
                    s = s.substring(1)
                    if (s.matches(SEMVER_ALL_OPTIONAL)) {
                        String version = s.find(SEMVER_ALL_OPTIONAL)
                        working.upper = version
                        working.lower = version
                        continue
                    }
                } else if (s.startsWith('~')) {
                    // Same to next minor version
                    s = s.substring(1)
                    Matcher matcher = SEMVER_PATCH_OPTIONAL.matcher(s)
                    if (matcher.find()) {
                        MatchedSemVer semver = MatchedSemVer.of(matcher)
                        working.lower = semver.toString()
                        working.includeLower = true
                        working.upper = "${semver.major}.${semver.minor + 1}.0"
                        working.includeUpper = false
                        continue
                    } else {
                        Matcher majorOnly = SEMVER_ALL_OPTIONAL.matcher(s)
                        if (matcher.find()) {
                            MatchedSemVer semVer = MatchedSemVer.of(matcher)
                            working.lower = semVer.toString()
                            working.includeLower = true
                            working.upper = "${semVer.major+1}.0.0"
                            working.includeUpper = false
                            continue
                        }
                    }
                } else if (s.startsWith('^')) {
                    s = s.substring(1)
                    Matcher matcher = SEMVER_PATCH_OPTIONAL.matcher(s)
                    if (matcher.find()) {
                        MatchedSemVer semVer = MatchedSemVer.of(matcher)
                        working.lower = semVer.toString()
                        working.includeLower = true
                        if (semVer.major == 0) {
                            if (semVer.minor == 0) {
                                working.upper = semVer.toString()
                            } else {
                                working.upper = "0.${semVer.minor+1}.0"
                            }
                        } else {
                            working.upper = "${semVer.major+1}.0.0"
                        }
                        working.includeUpper = false
                        continue
                    }
                }
                Matcher matcher = SEMVER_WITH_WILDCARDS.matcher(s)
                if (matcher.find()) {
                    MatchedSemVer semVer = MatchedSemVer.of(matcher)
                    working.includeLower = true
                    working.includeUpper = true
                    if (semVer.minor == -1) {
                        working.includeUpper = false
                        working.lower = "${semVer.major}.0.0"
                        working.upper = "${semVer.major+1}.0.0"
                    } else if (semVer.patch == -1) {
                        working.includeUpper = false
                        working.lower = "${semVer.major}.${semVer.minor}.0"
                        working.upper = "${semVer.major}.${semVer.minor+1}.0"
                    } else {
                        working.lower = s
                        working.upper = s
                    }
                    continue
                }
                matcher = SEMVER_DASH_RANGE.matcher(s)
                if (matcher.find()) {
                    String[] ends = s.split(' +- +')
                    String low = ends[0]
                    String high = ends[1]
                    Matcher highMatch = SEMVER_ALL_OPTIONAL.matcher(high)
                    highMatch.find()
                    MatchedSemVer highVer = MatchedSemVer.ofWildcardMissing(highMatch)
                    working.includeLower = true
                    working.lower = low
                    if (highVer.patch == -1) {
                        working.includeUpper = false
                        working.upper = "${highVer.major}.${highVer.minor+1}.0"
                    } else if (highVer.minor == -1) {
                        working.includeUpper = false
                        working.upper = "${highVer.major+1}.0.0"
                    } else {
                        working.includeUpper = true
                        working.upper = high
                    }
                    continue
                }
                throw new IllegalArgumentException("\"${sOrig}\" cannot be parsed as a SemVer version range.")
            }
            data.versions.add(working)
        }

        return data
    }

    static VersionRange ofMaven(String forgeVersion) {
        final VersionRange data = new VersionRange()
        SingleVersionData present = new SingleVersionData()
        data.versions.add(present)
        boolean buildingUpper = false
        boolean startingSingleVersion = false
        boolean insideSingleVersion = false
        for (char c : forgeVersion.chars) {
            switch (c) {
                case '(':
                    present.includeLower = false
                    startingSingleVersion = false
                    insideSingleVersion = true
                    break
                case '[':
                    present.includeLower = true
                    startingSingleVersion = false
                    insideSingleVersion = true
                    break
                case ',':
                    if (insideSingleVersion)
                        buildingUpper = true
                    else if (!startingSingleVersion) {
                        present = new SingleVersionData()
                        data.versions.add(present)
                        startingSingleVersion = true
                    }
                    break
                case ' ':
                    if (!insideSingleVersion && !startingSingleVersion) {
                        present = new SingleVersionData()
                        data.versions.add(present)
                        startingSingleVersion = true
                    }
                    break
                case ']':
                    present.includeUpper = true
                    break
                case ')':
                    present.includeUpper = false
                    break
                default:
                    if (insideSingleVersion) {
                        if (buildingUpper) present.upper = present.upper + c
                        else present.lower = present.lower + c
                    } else {
                        present.lower = present.lower + c
                        present.upper = present.upper + c
                    }
            }
        }
        return data
    }

    @Immutable
    @AutoFinal
    static class MatchedSemVer {
        int major
        int minor
        int patch
        @Nullable String pre
        @Nullable String meta

        static MatchedSemVer of(Matcher matcher) {
            String major = matcher.group("major")
            String minor = matcher.group("minor")
            String patch = matcher.group("patch")
            String pre = matcher.group("pre")
            String meta = matcher.group("meta")

            if (major==null || major.isEmpty()) major = '0'
            if (minor==null || minor.isEmpty()) minor = '0'
            if (patch==null || patch.isEmpty()) patch = '0'

            if (minor=='x' || minor=='*') minor = '-1'
            if (patch=='x' || patch=='*') patch = '-1'

            if (pre!==null && pre.isEmpty()) pre = null
            if (meta!==null && meta.isEmpty()) meta = null

            return new MatchedSemVer(Integer.valueOf(major), Integer.valueOf(minor), Integer.valueOf(patch), pre, meta)
        }

        static MatchedSemVer ofWildcardMissing(Matcher matcher) {
            String major = matcher.group("major")
            String minor = matcher.group("minor")
            String patch = matcher.group("patch")
            String pre = matcher.group("pre")
            String meta = matcher.group("meta")

            if (major==null || major.isEmpty()) major = '-1'
            if (minor==null || minor.isEmpty()) minor = '-1'
            if (patch==null || patch.isEmpty()) patch = '-1'

            if (minor=='x' || minor=='*') minor = '-1'
            if (patch=='x' || patch=='*') patch = '-1'

            if (pre!==null && pre.isEmpty()) pre = null
            if (meta!==null && meta.isEmpty()) meta = null

            return new MatchedSemVer(Integer.valueOf(major), Integer.valueOf(minor), Integer.valueOf(patch), pre, meta)
        }

        @Override
        String toString() {
            return "$major.$minor.$patch${pre!==null?"-$pre":''}${meta!==null?"+$meta":''}"
        }
    }
}
