package modsdotgroovy

import groovy.transform.CompileStatic

@CompileStatic
class VersionRange {
    List<SingleVersionData> versions = []

    static class SingleVersionData {
        String lower = ''
        String upper = ''
        boolean lowerInclusive = true
        boolean upperInclueive = false

        String toForge() {
            if (lower == upper) {
                return lower
            }
            else return (lowerInclusive?'[':'(') + lower + ',' + upper + (upperInclueive?']':')')
        }

        String toQuilt() {
            final List versionList = []
            if (lower != '') {
                versionList.add((lowerInclusive?">":">=")+lower)
            }
            if (upper != '') {
                versionList.add((upperInclueive?"<=":"<")+upper)
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

    static VersionRange of(String forgeVersion) {
        final VersionRange data = new VersionRange()
        SingleVersionData present = new SingleVersionData()
        data.versions.add(present)
        boolean buildingUpper = false
        boolean insideVersion = false
        for (char c : forgeVersion.chars) {
            switch (c) {
                case '(':
                    present.lowerInclusive = false
                    insideVersion = true
                    break
                case '[':
                    present.lowerInclusive = true
                    insideVersion = true
                    break
                case ',':
                    if (insideVersion)
                        buildingUpper = true
                    else {
                        present = new SingleVersionData()
                        data.versions.add(present)
                    }
                    break
                case ']':
                    present.upperInclueive = true
                    break
                case ')':
                    present.upperInclueive = false
                    break
                default:
                    if (insideVersion) {
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
}
