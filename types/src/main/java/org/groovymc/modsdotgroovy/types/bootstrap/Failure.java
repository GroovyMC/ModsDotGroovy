package org.groovymc.modsdotgroovy.types.bootstrap;

import java.io.Serializable;

public record Failure(int id, String message, StackTraceElement[] stackTrace) implements Serializable {}
