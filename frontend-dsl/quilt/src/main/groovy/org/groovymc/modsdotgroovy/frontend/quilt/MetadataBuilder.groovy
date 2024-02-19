package org.groovymc.modsdotgroovy.frontend.quilt

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2
import org.groovymc.modsdotgroovy.core.ModsDotGroovyCore
import org.groovymc.modsdotgroovy.frontend.DslBuilder
import org.groovymc.modsdotgroovy.frontend.PropertyInterceptor
import org.jetbrains.annotations.Nullable

@CompileStatic
@Log4j2(category = 'MDG - Quilt Frontend')
class MetadataBuilder extends DslBuilder implements PropertyInterceptor {
    /**@
     * A human-readable name for this mod.
     */
    @Nullable String name = null

    /**@
     * A human-readable description of this mod. This description should be plain text.
     */
    @Nullable String description = null

    /**@
     * Defines the licensing information.
     * <p>This should provide the complete set of preferred licenses conveying the entire mod package. In other words,
     * compliance with all listed licenses should be sufficient for usage, redistribution, etc. of the mod package as a whole.</p>
     * <p>For cases where a part of code is dual-licensed, choose the preferred license. The list is not exhaustive,
     * serves primarily as a kind of hint, and does not prevent you from granting additional rights/licenses on a case-by-case basis.</p>
     * <p>To aid automated tools, it is recommended to use <a href="https://spdx.org/licenses/">SPDX License Identifiers</a> for open-source licenses.</p>
     */
    @Nullable def /* <String | List<String>> */ license = null

    /**@
     * Defines the mod's icon. Icons are square PNG files.
     * (Minecraft resource packs use 128×128, but that is not a hard requirement - a power of two is, however, recommended.)
     * Can be provided in one of two forms:
     * <ul>
     *     <li>A path to a single PNG file.</li>
     *     <li>A dictionary of images widths to their files' paths.</li>
     * </ul>
     */
    @Nullable String icon = null

    /**@
     * A collection of key: value pairs denoting the persons or organizations that contributed to this project.
     * The key should be the name of the person or organization, while the value can be either a string representing a
     * single role or an array of strings each one representing a single role.
     * <p>A role can be any valid string. The "Owner" role is defined as being the person(s) or organization in charge
     * of the project.</p>
     */
    void contributors(@DelegatesTo(value = ContributorsBuilder, strategy = Closure.DELEGATE_FIRST)
                      @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.ContributorsBuilder')
                      final Closure closure) {
        log.debug "contributors(closure)"
        core.push('contributors')
        final contributorsBuilder = new ContributorsBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = contributorsBuilder
        closure.call(contributorsBuilder)
        core.pop()
    }

    void licenses(@DelegatesTo(value = LicensesBuilder, strategy = Closure.DELEGATE_FIRST)
                  @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.LicensesBuilder')
                  final Closure closure) {
        log.debug "licenses(closure)"
        core.push('licenses')
        final licensesBuilder = new LicensesBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = licensesBuilder
        closure.call(licensesBuilder)
        core.pop()
    }

    void license(@DelegatesTo(value = LicenseBuilder, strategy = Closure.DELEGATE_FIRST)
                  @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.LicenseBuilder')
                  final Closure closure) {
        log.debug "license(closure)"
        core.push('license')
        final licenseBuilder = new LicenseBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = licenseBuilder
        closure.call(licenseBuilder)
        core.pop()
    }

    /**@
     * Defines the contact information for the project.
     * The list is not exhaustive - mods may provide additional, non-standard keys (such as discord, slack, twitter, etc) - if possible, they should be valid URLs.
     * @param closure
     */
    void contact(@DelegatesTo(value = ContactBuilder, strategy = Closure.DELEGATE_FIRST)
                 @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.ContactBuilder')
                 final Closure closure) {
        log.debug "contact(closure)"
        core.push('contact')
        final contactBuilder = new ContactBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = contactBuilder
        closure.call(contactBuilder)
        core.pop()
    }

    // todo: license object support. https://github.com/QuiltMC/rfcs/blob/main/specification/0002-quilt.mod.json.md#the-license-field

    void icon(final int size, final String path) {
        log.debug "icon(int, string)"
        core.push('icon')
        core.put(size as String, path)
        core.pop()
    }

    void icon(@DelegatesTo(value = IconBuilder, strategy = Closure.DELEGATE_FIRST)
              @ClosureParams(value = SimpleType, options = 'org.groovymc.modsdotgroovy.frontend.quilt.IconBuilder')
              final Closure closure) {
        log.debug "icon(closure)"
        core.push('icon')
        final customFieldsBuilder = new IconBuilder(core)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = customFieldsBuilder
        closure.call(customFieldsBuilder)
        core.pop()
    }

    MetadataBuilder(final ModsDotGroovyCore core) {
        super(core)
    }
}
