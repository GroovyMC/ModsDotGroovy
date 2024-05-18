package org.groovymc.modsdotgroovy.types.runner;

import java.io.Serializable;
import java.util.Map;

public record Result(int id, Map<?, ?> result) implements Serializable {}
