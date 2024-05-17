package org.groovymc.modsdotgroovy.types.core;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Platform implements Serializable {
    private final String name;

    public String getName() {
        return name;
    }

    private Platform(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    private static final Map<String, Platform> REGISTRY = new ConcurrentHashMap<>();

    public static final Platform FORGE = new Platform("forge");
    public static final Platform NEOFORGE = new Platform("neoForge");
    public static final Platform FABRIC = new Platform("fabric");
    public static final Platform QUILT = new Platform("quilt");
    public static final Platform SPIGOT = new Platform("spigot");
    public static final Platform UNKNOWN = new Platform("unknown");

    public static final Set<Platform> STOCK_PLATFORMS = Set.of(FORGE, NEOFORGE, FABRIC, QUILT, SPIGOT);

    static {
        for (var platform : STOCK_PLATFORMS) {
            REGISTRY.put(platform.name.toLowerCase(Locale.ROOT), platform);
        }
        REGISTRY.put("unknown", UNKNOWN);
    }

    public String name() {
        return this.name;
    }

    @Override
    public String toString() {
        return name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
    }

    /**
     * Gets a {@link Platform} from its name, creating a new {@link Platform} object if it doesn't exist.
     * @param name the name of the {@link Platform}
     * @return the {@link Platform} object
     */
    public static Platform of(final String name) {
        return internalOf(name, true);
    }

    /**
     * Gets a {@link Platform} from its name, returning {@link Platform#UNKNOWN} if it doesn't exist.
     * @param name the name of the {@link Platform}
     * @return the {@link Platform} object
     */
    public static Platform fromRegistry(final String name) {
        return internalOf(name, false);
    }

    /**
     * @param name the name of the {@link Platform}
     * @param create whether to create a new {@link Platform} object if it doesn't already exist
     * @return the {@link Platform} object
     */
    private static Platform internalOf(String name, final boolean create) {
        name = name.toLowerCase(Locale.ROOT);
        var platform = REGISTRY.get(name);
        if (platform == null) {
            return create ? REGISTRY.putIfAbsent(name, new Platform(name)) : UNKNOWN;
        } else {
            return platform;
        }
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Platform platform && name.equals(platform.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
