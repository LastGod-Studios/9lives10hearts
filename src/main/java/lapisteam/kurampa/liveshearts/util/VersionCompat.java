package lapisteam.kurampa.liveshearts.util;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;

/**
 * Resolve renamed Bukkit registry constants without linking to a field that
 * does not exist on older/newer server versions. No NMS or version packages.
 */
public final class VersionCompat {
    private VersionCompat() {
    }

    public static Attribute maxHealthAttribute() {
        return constant(Attribute.class, "MAX_HEALTH", "GENERIC_MAX_HEALTH");
    }

    public static PotionEffectType nauseaEffect() {
        return constant(PotionEffectType.class, "NAUSEA", "CONFUSION");
    }

    public static void applyMaxHealth(Player player, int hearts) {
        // Zero health is not a valid max-health value on all supported servers.
        // The death / spectator policy is handled separately by HeartService.
        if (hearts <= 0 || player.isDead()) {
            return;
        }
        Attribute attribute = maxHealthAttribute();
        if (attribute == null) {
            throw new IllegalStateException("No supported maximum-health attribute found");
        }
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            throw new IllegalStateException("Player does not have a maximum-health attribute");
        }
        double maxHealth = hearts * 2.0;
        instance.setBaseValue(maxHealth);
        if (player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        }
    }

    private static <T> T constant(Class<T> type, String... names) {
        for (String name : names) {
            try {
                Field field = type.getField(name);
                return type.cast(field.get(null));
            } catch (NoSuchFieldException ignored) {
                // The alternative spelling belongs to another server release.
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Cannot access " + type.getName() + '.' + name, exception);
            }
        }
        return null;
    }
}
