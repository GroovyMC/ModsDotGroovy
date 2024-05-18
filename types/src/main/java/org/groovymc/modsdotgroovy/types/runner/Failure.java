package org.groovymc.modsdotgroovy.types.runner;

import java.io.Serializable;

public record Failure(int id, String message, StackTraceElement[] stackTrace) implements Serializable {}
