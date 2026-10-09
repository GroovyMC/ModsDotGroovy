package org.groovymc.modsdotgroovy.golden

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.groovymc.modsdotgroovy.transform.MDGBindingVarsAdder
import org.groovymc.modsdotgroovy.types.core.Platform
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream

/**
 * Runs mods.groovy scripts through frontend and stock plugin for their platform and compares the built map
 * against a checked-in golden file.<br>
 * Cases are laid out as {@code golden/<platform>/<case>/mods.groovy} with an {@code expected.json} next to each.
 */
@CompileStatic
class GoldenFileTests
{
    private static final Path GOLDEN_DIR = Path.of(System.getProperty('mdg.golden.sourceDir'))
    private static final boolean UPDATE = Boolean.getBoolean('mdg.golden.update')

    @TestFactory
    Stream<DynamicTest> goldenFiles()
    {
        return Files.walk(GOLDEN_DIR).filter { it.fileName.toString() == 'mods.groovy' }.sorted().map { Path script ->
            final Path caseDir = script.parent
            final Platform platform = Platform.of(GOLDEN_DIR.relativize(caseDir).getName(0).toString())
            DynamicTest.dynamicTest(GOLDEN_DIR.relativize(caseDir).toString()) {
                final String actual = JsonOutput.prettyPrint(JsonOutput.toJson(build(script, platform))) + '\n'
                final Path expectedFile = caseDir.resolve('expected.json')

                if (UPDATE || !Files.exists(expectedFile)) {
                    Files.writeString(expectedFile, actual)
                } else {
                    // parsed json so that formatting changes dont fail test
                    final expected = new JsonSlurper().parseText(Files.readString(expectedFile))
                    if (expected != new JsonSlurper().parseText(actual))
                        throw new AssertionError("${expectedFile} doesnt match. Run with -PupdateGolden to regenerate.\nExpected:\n${Files.readString(expectedFile)}\nActual:\n${actual}".toString())
                }
            }
        }
    }

    private static Map build(Path script, Platform platform)
    {
        final config = new CompilerConfiguration()
        // The Gradle plugin's runner gets the frontend classes in the root package remake that here with an import
        config.addCompilationCustomizers(new ImportCustomizer().addStarImports("org.groovymc.modsdotgroovy.frontend.${platform.name().toLowerCase()}"))
        final transform = new ASTTransformationCustomizer(MDGBindingVarsAdder)

        transform.annotationParameters = [className: "${platform}ModsDotGroovy".toString()] as Map<String, Object>
        config.addCompilationCustomizers(transform)

        final binding = new Binding([
                platform         : platform,
                buildProperties  : [:],
                version          : '1.0.0',
                group            : 'com.example',
                conversionSettings: [onlineBehavior: 'SKIP'],
                // what it gathers
                minecraftVersion : '1.20.1',
                platformVersion  : '0.15.0',
        ])
        final result = new GroovyShell(GoldenFileTests.classLoader, binding, config).evaluate(script.toFile())
        return buildMap(result)
    }

    // The script's return value is the frontend instance, which isn't visible to this class at compile time
    @CompileDynamic
    private static Map buildMap(final frontend) {
        return frontend.core.build()
    }
}
