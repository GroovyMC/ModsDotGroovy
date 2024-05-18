package org.groovymc.modsdotgroovy.runner

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import groovy.util.logging.Log4j2
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.groovymc.modsdotgroovy.types.core.Platform
import org.groovymc.modsdotgroovy.types.runner.*

import java.lang.annotation.Annotation
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@CompileStatic
@Log4j2(category = 'MDG - Bootstrap Runner')
class ModsDotGroovyRunner implements AutoCloseable {

    private static final CompilerConfiguration MDG_COMPILER_CONFIG = new CompilerConfiguration().tap {
        targetBytecode = JDK17
        optimizationOptions['indy'] = true
    }

    private ModsDotGroovyRunner() throws IOException {
        this.socket = new ServerSocket(0)
    }

    static void main(String[] args) throws IOException {
        try (ModsDotGroovyRunner runner = new ModsDotGroovyRunner()) {
            runner.run()
        }
    }

    private final ServerSocket socket
    private final ExecutorService executor = Executors.newFixedThreadPool(Integer.getInteger("org.groovymc.modsdotgroovy.conversion.threads"))

    @Override
    void close() throws IOException {
        log.info "Shutting down MDG runner..."
        socket.close()
        executor.shutdownNow()
        try {
            executor.awaitTermination(4000, TimeUnit.MILLISECONDS)
        } catch (InterruptedException e) {
            throw new RuntimeException(e)
        }
    }

    private void run() throws IOException {
        // This tells the parent process what port we're listening on
        println(socket.getLocalPort())
        log.info "Starting up MDG runner..."

        var socket = this.socket.accept()
        log.info "Connected to MDG runner..."
        var input = FilteredStream.filtered(socket.getInputStream())
        var os = new ObjectOutputStream(socket.getOutputStream())
        var output = new Output(os)
        while (true) {
            try {
                var obj = input.readObject()
                if (obj instanceof Stop) {
                    break
                } else if (obj instanceof Run) {
                    execute(obj, output)
                } else {
                    throw new IOException("Unexpected object: " + obj)
                }
            } catch (ClassNotFoundException e) {
                throw new IOException(e)
            }
        }
    }

    private void execute(Run run, Output output) {
        var future = executor.submit(() -> {
            try {
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
                    output.writeObject(new Result(run.id(), result))
                }
            } catch (Throwable t) {
                t.printStackTrace()
                output.writeObject(new Failure(run.id(), t.message, t.stackTrace))
                throw new RuntimeException(t)
            }
        })
    }

    @TupleConstructor(includeFields = true)
    private static final class Output {
        private final ObjectOutputStream stream

        synchronized void writeObject(Object obj) {
            stream.writeObject(obj)
        }
    }

    @CompileDynamic
    private static Map<?, ?> fromScriptResult(Object scriptResult) {
        return scriptResult.core.build()
    }
}
