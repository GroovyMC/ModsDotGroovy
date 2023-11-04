package org.groovymc.modsdotgroovy.core

import groovy.transform.*
import groovy.transform.options.Visibility

/*
 * Note: This is an emulated record as Gradle 8.4 doesn't seem to support serialising native records into the configuration cache yet
 */
@CompileStatic
@VisibilityOptions(constructor = Visibility.PRIVATE)
@EqualsAndHashCode(pojo = true, cache = true, useGetters = false)
@RecordOptions(mode = RecordTypeMode.EMULATE, toList = false, toMap = false)
record Platform(String name) implements Serializable {
    private static final Map<String, Platform> REGISTRY = [:]

    public static final Platform FORGE = new Platform("forge")
    public static final Platform NEOFORGE = new Platform("neoForge")
    public static final Platform FABRIC = new Platform("fabric")
    public static final Platform QUILT = new Platform("quilt")
    public static final Platform SPIGOT = new Platform("spigot")
    public static final Platform UNKNOWN = new Platform("unknown")

    public static final Set<Platform> STOCK_PLATFORMS = Set.of(FORGE, NEOFORGE, FABRIC, QUILT, SPIGOT)

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
        return switch (name) {
            case "forge" -> FORGE
            case "neoforge" -> NEOFORGE
            case "fabric" -> FABRIC
            case "quilt" -> QUILT
            case "spigot" -> SPIGOT
            default -> {
                final platform = REGISTRY.get(name)
                if (platform === null) {
                    yield create ? REGISTRY.putIfAbsent(name, new Platform(name)) : UNKNOWN
                } else {
                    yield platform
                }
            }
        }
    }

    private Platform {}
}
