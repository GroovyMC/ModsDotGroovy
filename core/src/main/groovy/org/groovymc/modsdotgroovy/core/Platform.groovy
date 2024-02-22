package org.groovymc.modsdotgroovy.core

import groovy.transform.*
import groovy.transform.options.Visibility
import groovy.transform.stc.POJO

import java.util.concurrent.ConcurrentHashMap

@CompileStatic
@POJO
@KnownImmutable
@EqualsAndHashCode(pojo = true, cache = true, includeFields = true)
final class Platform implements Serializable {
    private final String name

    @NullCheck
    private Platform(final String name) {
        this.name = name
    }

    private static final Map<String, Platform> REGISTRY = new ConcurrentHashMap<>()

    public static final Platform FORGE = new Platform("forge")
    public static final Platform NEOFORGE = new Platform("neoForge")
    public static final Platform FABRIC = new Platform("fabric")
    public static final Platform QUILT = new Platform("quilt")
    public static final Platform SPIGOT = new Platform("spigot")
    public static final Platform UNKNOWN = new Platform("unknown")

    public static final Set<Platform> STOCK_PLATFORMS = Set.of(FORGE, NEOFORGE, FABRIC, QUILT, SPIGOT)

    static {
        for (def platform : STOCK_PLATFORMS) {
            REGISTRY[platform.name().toLowerCase(Locale.ROOT)] = platform
        }
        REGISTRY['unknown'] = UNKNOWN
    }

    String name() {
        return this.name
    }

    String toString() {
        return name.capitalize()
    }

    /**
     * Gets a {@link Platform} from its name, creating a new {@link Platform} object if it doesn't exist.
     * @param name the name of the {@link Platform}
     * @return the {@link Platform} object
     */
    static Platform of(final String name) {
        return internalOf(name, true)
    }

    /**
     * Gets a {@link Platform} from its name, returning {@link Platform#UNKNOWN} if it doesn't exist.
     * @param name the name of the {@link Platform}
     * @return the {@link Platform} object
     */
    static Platform fromRegistry(final String name) {
        return internalOf(name, false)
    }

    /**
     * @param name the name of the {@link Platform}
     * @param create whether to create a new {@link Platform} object if it doesn't already exist
     * @return the {@link Platform} object
     */
    private static Platform internalOf(String name, final boolean create) {
        name = name.toLowerCase(Locale.ROOT)
        final platform = REGISTRY.get(name)
        if (platform === null) {
            return create ? REGISTRY.putIfAbsent(name, new Platform(name)) : UNKNOWN
        } else {
            return platform
        }
    }
}
