package org.groovymc.modsdotgroovy.gradle.internal;

import dev.lukebemish.forkedtaskexecutor.ForkedTaskExecutor;
import dev.lukebemish.forkedtaskexecutor.ForkedTaskExecutorSpec;
import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.gradle.api.tasks.Input;
import org.groovymc.modsdotgroovy.types.core.Platform;
import org.groovymc.modsdotgroovy.types.runner.FilteredStream;
import org.groovymc.modsdotgroovy.types.runner.Result;
import org.groovymc.modsdotgroovy.types.runner.Run;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Map;

public abstract class ConvertService implements BuildService<ConvertService.Parameters>, AutoCloseable {
    public static final String LOG_LEVEL_PROPERTY = "org.groovymc.modsdotgroovy.conversion.logging";
    public static final String STACKTRACE_PROPERTY = "org.groovymc.modsdotgroovy.conversion.hidestacktrace";
    
    public abstract static class Parameters implements BuildServiceParameters {
        @Input
        public abstract Property<String> getLogLevel();
        @Input
        public abstract Property<Boolean> getHideStacktrace();
    }
    
    private ForkedTaskExecutor forkedTaskExecutor;

    @Inject
    public ConvertService() {}
    
    private synchronized void start(String runnerClasspath) {
        if (forkedTaskExecutor == null) {
            var specBuilder = ForkedTaskExecutorSpec.builder()
                    .javaExecutable(ProcessHandle.current().info().command().orElseThrow())
                    .addJvmOption("-cp")
                    .addJvmOption(runnerClasspath)
                    .addJvmOption("-D" + LOG_LEVEL_PROPERTY + "=" + getParameters().getLogLevel().get())
                    //.addJvmOption("-Dlog4j2.disableJmx=true") // blame log4j2
                    .hideStacktrace(getParameters().getHideStacktrace().get())
                    .taskClass("org.groovymc.modsdotgroovy.runner.ModsDotGroovyRunner")
                    .build();
            forkedTaskExecutor = new ForkedTaskExecutor(specBuilder);
        }
    }
    
    @Override
    public synchronized void close() {
        if (forkedTaskExecutor != null) {
            forkedTaskExecutor.close();
        }
    }

    public Map<?, ?> run(String runnerClasspath, URL[] classpath, File input, Platform platform, boolean multiplatform, Map<String, Object> bindings) {
        start(runnerClasspath);
        var run = new Run(classpath, input, platform.name(), multiplatform, bindings);
        var output = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(output)) {
            oos.writeObject(run);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        var bytes = forkedTaskExecutor.submit(output.toByteArray());
        try (var inputStream = FilteredStream.filtered(new ByteArrayInputStream(bytes))) {
            Object obj = inputStream.readObject();
            if (obj instanceof Result result) {
                return result.result();
            } else {
                throw new RuntimeException("Unexpected object: " + obj);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
