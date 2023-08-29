package org.groovymc.modsdotgroovy.core.versioning

import groovy.transform.CompileStatic

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**@
 * Marker annotation for fields that should be treated as version ranges.
 * <p>Traits such as {@link org.groovymc.modsdotgroovy.frontend.PropertyInterceptor} will automatically convert
 * {@link String} properties/fields annotated with this annotation to {@link VersionRange} objects.</p>
 */
@CompileStatic
@Target([ElementType.FIELD, ElementType.PARAMETER])
@Retention(RetentionPolicy.RUNTIME)
@interface VersionRangeAware {}
