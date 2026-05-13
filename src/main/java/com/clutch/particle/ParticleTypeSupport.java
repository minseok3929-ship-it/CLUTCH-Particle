package com.clutch.particle;

import org.bukkit.Color;
import org.bukkit.Particle;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class ParticleTypeSupport {
    private static final Set<String> SUPPORTED_TYPES = Set.of(
            "DUST", "END_ROD", "ENCHANT", "HEART", "SMOKE", "TOTEM", "FLAME"
    );

    private ParticleTypeSupport() {
    }

    public static boolean isSupported(String type) {
        return SUPPORTED_TYPES.contains(normalize(type));
    }

    public static String normalize(String type) {
        return type.toUpperCase(Locale.ROOT);
    }

    public static Optional<Particle> toBukkitParticle(String supportedType) {
        return switch (normalize(supportedType)) {
            case "DUST" -> firstExisting("DUST", "REDSTONE");
            case "ENCHANT" -> firstExisting("ENCHANT", "ENCHANTMENT_TABLE");
            case "SMOKE" -> firstExisting("SMOKE", "SMOKE_NORMAL");
            case "TOTEM" -> firstExisting("TOTEM_OF_UNDYING", "TOTEM");
            default -> firstExisting(normalize(supportedType));
        };
    }

    public static Optional<Color> parseHexColor(String color) {
        if (color == null || color.equalsIgnoreCase("none")) {
            return Optional.empty();
        }
        if (!color.matches("#[0-9a-fA-F]{6}")) {
            return Optional.empty();
        }
        int red = Integer.parseInt(color.substring(1, 3), 16);
        int green = Integer.parseInt(color.substring(3, 5), 16);
        int blue = Integer.parseInt(color.substring(5, 7), 16);
        return Optional.of(Color.fromRGB(red, green, blue));
    }

    public static String supportedTypeText() {
        return String.join(", ", SUPPORTED_TYPES);
    }

    private static Optional<Particle> firstExisting(String... candidates) {
        return Arrays.stream(candidates)
                .map(name -> {
                    try {
                        return Particle.valueOf(name);
                    } catch (IllegalArgumentException exception) {
                        return null;
                    }
                })
                .filter(particle -> particle != null)
                .findFirst();
    }
}
