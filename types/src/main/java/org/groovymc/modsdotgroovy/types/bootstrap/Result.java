package org.groovymc.modsdotgroovy.types.bootstrap;

import java.io.Serializable;
import java.util.Map;

public record Result(int id, Map<String, Object> result) implements Serializable {}
