package org.groovymc.modsdotgroovy.frontend

import groovy.transform.CompileStatic
import org.groovymc.modsdotgroovy.core.versioning.BypassVersionEntry
import org.groovymc.modsdotgroovy.core.versioning.VersionRange

@CompileStatic
interface VersionProducer {
    default VersionRange v(final String string) {
        return VersionRange.of(string)
    }

    /**@
     * If you need to declare a custom version range that ModsDotGroovy doesn't support, you can use this method to
     * bypass the version range parsing.
     */
    default VersionRange rawVersionRange(final String string) {
        return new VersionRange.SingleVersionRange(new BypassVersionEntry(string))
    }
}