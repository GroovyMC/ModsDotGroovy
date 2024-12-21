package org.groovymc.modsdotgroovy.runner

import dev.lukebemish.forkedtaskexecutor.runner.Task
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.groovymc.modsdotgroovy.types.core.Platform
import org.groovymc.modsdotgroovy.types.runner.FilteredStream
import org.groovymc.modsdotgroovy.types.runner.Result
import org.groovymc.modsdotgroovy.types.runner.Run

import java.lang.annotation.Annotation

@CompileStatic
class ModsDotGroovyRunner implements Task {
    private static final CompilerConfiguration MDG_COMPILER_CONFIG = new CompilerConfiguration().tap {
        targetBytecode = JDK17
        optimizationOptions['indy'] = true
    }

    ModsDotGroovyRunner(String[] args) {}

    @Override
    byte[] run(byte[] bytes) throws Exception {
        try (var input = FilteredStream.filtered(new ByteArrayInputStream(bytes))) {
            var run = input.readObject()
            if (run instanceof Run) {
                try (var mdgClassLoader = new URLClassLoader(run.classpath())) {

                    final compilerConfig = new CompilerConfiguration(MDG_COMPILER_CONFIG)
                    compilerConfig.classpathList = mdgClassLoader.URLs*.toString()

                    final bindingAdderTransform = new ASTTransformationCustomizer(Class.forName('org.groovymc.modsdotgroovy.transform.MDGBindingVarsAdder', false, mdgClassLoader) as Class<? extends Annotation>)
                    final Platform platform = Platform.of(run.platform())
                    final GString frontendClassName = "${platform.toString()}ModsDotGroovy"
                    if (run.multiplatform())
                        frontendClassName.values[0] = 'Multiplatform'

                    bindingAdderTransform.annotationParameters = [className: frontendClassName.toString()] as Map<String, Object>

                    compilerConfig.addCompilationCustomizers(bindingAdderTransform)

                    Map bindingValues = new LinkedHashMap<>(run.bindings())
                    bindingValues.platform = platform

                    final bindings = new Binding(bindingValues)
                    final shell = new GroovyShell(mdgClassLoader, bindings, compilerConfig)

                    // set context classloader to MDG classloader -- needed for proper service discovery
                    shell.evaluate('Thread.currentThread().contextClassLoader = this.class.classLoader')

                    var result = FilteredStream.convertToSerializable(fromScriptResult(shell.evaluate(run.input())))
                    var output = new ByteArrayOutputStream()
                    try (var oos = new ObjectOutputStream(output)) {
                        oos.writeObject(new Result(result))
                    }
                    return output.toByteArray()
                }
            } else {
                throw new IllegalArgumentException("Expected Run object, got ${run.getClass().name}")
            }
        }
    }

    @CompileDynamic
    private static Map<?, ?> fromScriptResult(Object scriptResult) {
        return scriptResult.core.build()
    }
}
