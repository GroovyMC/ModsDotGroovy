package modsdotgroovy

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import static groovy.lang.Closure.DELEGATE_FIRST

@CompileStatic
class VersionsBuilder {
    private VersionRange versionRange

    void versionRange(@DelegatesTo(value = VersionRange.SingleVersionData, strategy = DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'modsdotgroovy.VersionRange$SingleVersionData') final Closure closure) {
        VersionRange.SingleVersionData version = new VersionRange.SingleVersionData()
        closure.delegate = version
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(version)
        versionRange.versions.add(version)
    }

    VersionRange build() {
        return versionRange
    }
}
