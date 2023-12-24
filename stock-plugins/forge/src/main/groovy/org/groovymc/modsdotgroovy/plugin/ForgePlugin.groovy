package org.groovymc.modsdotgroovy.plugin

import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.core.Logger
import org.groovymc.modsdotgroovy.core.Platform
import org.groovymc.modsdotgroovy.core.versioning.VersionRange
import org.jetbrains.annotations.Nullable

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.regex.Matcher

@CompileStatic
@SuppressWarnings('GroovyUnusedDeclaration') // All these methods are dynamically called by ModsDotGroovyCore
@Log4j2(category = 'MDG - ForgePlugin')
class ForgePlugin extends ModsDotGroovyPlugin {

    @Override
    byte getPriority() {
        return -10 // we want to run after ForgeLikePlugin
    }

    @Override
    Logger getLog() {
        return log
    }

    class Mods {
        class ModInfo {
            @Nullable String modId = null
        }
    }
}
