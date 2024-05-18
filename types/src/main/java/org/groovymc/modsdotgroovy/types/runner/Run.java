package org.groovymc.modsdotgroovy.types.runner;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;

public record Run(int id, URL[] classpath, File input, String platform, boolean multiplatform, Map<String, Object> bindings) implements Serializable {}
