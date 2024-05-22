package org.groovymc.modsdotgroovy.gradle.internal;

import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.gradle.api.tasks.Input;
import org.groovymc.modsdotgroovy.types.runner.Failure;
import org.groovymc.modsdotgroovy.types.runner.FilteredStream;
import org.groovymc.modsdotgroovy.types.runner.Result;
import org.groovymc.modsdotgroovy.types.runner.Run;
import org.groovymc.modsdotgroovy.types.runner.Stop;
import org.groovymc.modsdotgroovy.types.core.Platform;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ConvertService implements BuildService<ConvertService.Parameters>, AutoCloseable {
    public static final String THREAD_COUNT_PROPERTY = "org.groovymc.modsdotgroovy.conversion.threads";
    public static final String LOG_LEVEL_PROPERTY = "org.groovymc.modsdotgroovy.conversion.logging";
    public static final String STACKTRACE_PROPERTY = "org.groovymc.modsdotgroovy.conversion.hidestacktrace";

    public abstract static class Parameters implements BuildServiceParameters {
        @Input
        public abstract Property<String> getThreads();
        @Input
        public abstract Property<String> getLogLevel();
        @Input
        public abstract Property<Boolean> getHideStacktrace();
    }

    private Process process;
    private Socket socket;
    private ResultListener listener;

    @Inject
    public ConvertService() {}

    private ResultListener start(String runnerClasspath) {
        synchronized (this) {
            if (process == null) {
                var builder = new ProcessBuilder();
                builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
                // We'd like to redirect this normally... but we can't, cause INHERIT doesn't seem to work here. Gradle weirdness?
                builder.redirectError(ProcessBuilder.Redirect.PIPE);
                var java = ProcessHandle.current().info().command().orElseThrow();

                builder.command(List.of(
                        java,
                        "-cp",
                        runnerClasspath,
                        "-D"+THREAD_COUNT_PROPERTY+"="+getParameters().getThreads().get(),
                        "-D"+LOG_LEVEL_PROPERTY+"="+getParameters().getLogLevel().get(),
                        "-D"+STACKTRACE_PROPERTY+"="+getParameters().getHideStacktrace().get(),
                        "org.groovymc.modsdotgroovy.runner.ModsDotGroovyRunner"
                ));
                try {
                    process = builder.start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                CompletableFuture<String> socketPort = new CompletableFuture<>();
                var thread = new StreamWrapper(process.getInputStream(), socketPort);
                new Thread(() -> {
                    try {
                        InputStreamReader reader = new InputStreamReader(process.getErrorStream());
                        BufferedReader bufferedReader = new BufferedReader(reader);
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            System.err.println(line);
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }).start();
                thread.start();
                try {
                    String socketPortString = socketPort.get(4000, TimeUnit.MILLISECONDS);
                    int port = Integer.parseInt(socketPortString);
                    socket = new Socket(InetAddress.getLoopbackAddress(), port);

                    listener = new ResultListener(socket);
                    listener.start();
                } catch (InterruptedException | ExecutionException | TimeoutException | IOException e) {
                    throw new RuntimeException(e);
                }
            }

            // We should gracefully handle shutting down the forked process if necessary
            WeakReference<ConvertService> reference = new WeakReference<>(this);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                ConvertService service = reference.get();
                if (service != null) {
                    try {
                        service.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }));

            return listener;
        }
    }

    private static final class StreamWrapper extends Thread {
        private final InputStream stream;
        private final CompletableFuture<String> socketPort;

        private StreamWrapper(InputStream stream, CompletableFuture<String> socketPort) {
            this.stream = stream;
            this.socketPort = socketPort;
            this.setUncaughtExceptionHandler((t, e) -> {
                socketPort.completeExceptionally(e);
                StreamWrapper.this.getThreadGroup().uncaughtException(t, e);
            });
        }

        @Override
        public void run() {
            try {
                var reader = new BufferedReader(new InputStreamReader(stream));
                socketPort.complete(reader.readLine());
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }
    }

    private static final class ResultListener extends Thread {
        private final Map<Integer, CompletableFuture<Result>> results = new HashMap<>();
        private final Socket socket;
        private final ObjectOutputStream output;

        private ResultListener(Socket socket) throws IOException {
            this.socket = socket;
            output = new ObjectOutputStream(socket.getOutputStream());
            this.setUncaughtExceptionHandler((t, e) -> {
                try {
                    close(e);
                } catch (IOException ex) {
                    var exception = new UncheckedIOException(ex);
                    exception.addSuppressed(e);
                    ResultListener.this.getThreadGroup().uncaughtException(t, exception);
                }
                ResultListener.this.getThreadGroup().uncaughtException(t, e);
            });
        }

        public synchronized CompletableFuture<Result> listen(Run run) throws IOException {
            if (closed) {
                throw new IOException("Listener is closed");
            }
            var out = results.computeIfAbsent(run.id(), i -> new CompletableFuture<>());
            output.writeObject(run);
            return out;
        }

        private volatile boolean closed = false;

        private void close(Throwable e) throws IOException {
            if (this.closed) return;
            this.closed = true;
            for (var future : results.values()) {
                future.completeExceptionally(e);
            }
            results.clear();

            output.writeObject(new Stop());
        }

        public void close() throws IOException {
            close(new IOException("Execution was interrupted"));
        }

        @Override
        public void run() {
            try {
                if (!closed) {
                    var input = FilteredStream.filtered(socket.getInputStream());
                    while (!closed) {
                        Object obj = input.readObject();
                        if (obj instanceof Result result) {
                            var future = results.remove(result.id());
                            if (future != null) {
                                future.complete(result);
                            }
                        } else if (obj instanceof Failure failure) {
                            var future = results.remove(failure.id());
                            if (future != null) {
                                var exception = new RuntimeException(failure.message());
                                exception.setStackTrace(failure.stackTrace());
                                future.completeExceptionally(exception);
                            }
                        } else {
                            throw new IOException("Unexpected object: " + obj);
                        }
                    }
                }
            } catch (EOFException ignored) {

            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (this) {
            List<Exception> suppressed = new ArrayList<>();
            if (listener != null) {
                try {
                    listener.close();
                    if (listener.isAlive()) {
                        listener.interrupt();
                    }
                    listener.join();
                } catch (Exception e) {
                    suppressed.add(e);
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {
                    suppressed.add(e);
                }
            }
            if (process != null) {
                try {
                    process.destroy();
                    process.waitFor();
                } catch (Exception e) {
                    suppressed.add(e);
                }
            }
            if (!suppressed.isEmpty()) {
                var exception = new IOException("Failed to close resources");
                suppressed.forEach(Exception::printStackTrace);
                suppressed.forEach(exception::addSuppressed);
                throw exception;
            }
        }
    }

    private final AtomicInteger id = new AtomicInteger();

    public Map<?, ?> run(String runnerClasspath, URL[] classpath, File input, Platform platform, boolean multiplatform, Map<String, Object> bindings) {
        var listener = start(runnerClasspath);
        var nextId = id.getAndIncrement();
        var run = new Run(nextId, classpath, input, platform.name(), multiplatform, bindings);
        try {
            var value = listener.listen(run).get();
            return value.result();
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
