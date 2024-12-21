package org.groovymc.modsdotgroovy.types.runner;

import java.io.Serializable;
import java.util.Map;

public record Result(Map<?, ?> result) implements Serializable {}
