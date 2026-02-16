package lapisteam.kurampa.liveshearts.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import lapisteam.kurampa.liveshearts.config.ConfigKeys;
import lapisteam.kurampa.liveshearts.service.HeartService;
import lapisteam.kurampa.liveshearts.config.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public final class ItemUtil {

    private ItemUtil() {}

    public static boolean isHeartHead(ItemStack item, JavaPlugin plugin) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return false;
        var meta = item.getItemMeta();
        return meta != null
                && meta.hasCustomModelData()
                && meta.getCustomModelData() == plugin.getConfig().getInt(ConfigKeys.HEAD_HEART_CONTAINER);
    }

    public static boolean isCursedHead(ItemStack item, JavaPlugin plugin) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return false;
        var meta = item.getItemMeta();
        return meta != null
                && meta.hasCustomModelData()
                && meta.getCustomModelData() == plugin.getConfig().getInt(ConfigKeys.HEAD_HEART_CURSED_CONTAINER);
    }

    public static ItemStack createHeartHead(Player dead, JavaPlugin plugin) {
        return createHead(dead, plugin, ConfigKeys.HEAD_HEART_NAME,
                ConfigKeys.HEAD_HEART_LORE,
                ConfigKeys.HEAD_HEART_CONTAINER,
                ConfigKeys.HEAD_HEART_VALUE,
                ConfigKeys.HEAD_HEART_DROP_CHANCE,
                1);
    }

    public static ItemStack createCursedHead(Player dead, JavaPlugin plugin) {
        return createHead(dead, plugin, ConfigKeys.HEAD_HEART_CURSED_NAME,
                ConfigKeys.HEAD_HEART_CURSED_LORE,
                ConfigKeys.HEAD_HEART_CURSED_CONTAINER,
                ConfigKeys.HEAD_HEART_VALUE,
                ConfigKeys.HEAD_HEART_CURSED_CHANCE,
                plugin.getConfig().getInt(ConfigKeys.HEAD_HEART_CURSED_AMOUNT, 1));
    }

    private static ItemStack createHead(Player dead,
                                        JavaPlugin plugin,
                                        String nameKey,
                                        String loreKey,
                                        String containerKey,
                                        String valueKey,
                                        String chanceKey,
                                        int amount) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return head;

        String rawName = plugin.getConfig().getString(nameKey, "{player}");
        String display = rawName.replace("{player}", dead.getName())
                .replace("{amount}", String.valueOf(amount));
        meta.setDisplayName(ColorUtil.translateHex(display));

        List<String> rawLore = plugin.getConfig().getStringList(loreKey);
        meta.setLore(rawLore.stream()
                .map(line -> ColorUtil.translateHex(
                        line.replace("{player}", dead.getName())
                                .replace("{amount}", String.valueOf(amount))
                ))
                .toList()
        );

        int cmd = plugin.getConfig().getInt(containerKey, 12345);
        meta.setCustomModelData(cmd);

        String value = plugin.getConfig().getString(valueKey, "").trim();
        if (!value.isEmpty()) {
            try {
                Object server = Bukkit.getServer();
                Method createProfile = server.getClass()
                        .getMethod("createProfile", UUID.class);
                Object profObj = createProfile.invoke(server, UUID.randomUUID());
                PlayerProfile profile = (PlayerProfile) profObj;
                profile.getProperties().add(new ProfileProperty("textures", value));
                meta.setPlayerProfile(profile);
            } catch (ReflectiveOperationException ex) {
                plugin.getLogger().warning("Не удалось установить текстуру головы: " + ex.getMessage());
            }
        }

        head.setItemMeta(meta);
        return head;
    }

    public static void handleHeartHead(Player player,
                                       ItemStack item,
                                       HeartService service,
                                       Lang lang) {
        UUID id     = player.getUniqueId();
        int current = service.getHearts(id);
        int max     = service.getMaxHearts();

        if (current >= max) {
            player.sendMessage(lang.msg("error_max_hearts", "max", max));
            return;
        }

        if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
        else player.getInventory().remove(item);

        service.addHearts(id, 1);
        player.sendMessage(lang.msg("heart_recovered", "hearts", service.getHearts(id)));
    }

    public static void handleCursedHead(Player player,
                                        ItemStack item,
                                        HeartService service,
                                        Lang lang,
                                        JavaPlugin plugin) {
        UUID id      = player.getUniqueId();
        int current  = service.getHearts(id);
        int amount   = plugin.getConfig().getInt(ConfigKeys.HEAD_HEART_CURSED_AMOUNT, 1);

        if (current <= 0) {
            player.sendMessage(lang.msg("hearts_spectator_mode"));
            return;
        }

        if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
        else player.getInventory().remove(item);

        service.removeHearts(id, amount);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 5 * 20, 0));
        player.sendMessage(lang.msg("cursed_used", "amount", amount));
    }

    public static boolean isUniqueTotem(ItemStack item, JavaPlugin plugin) {
        if (item == null || item.getType() != Material.TOTEM_OF_UNDYING) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        String name = ColorUtil.translateHex(plugin.getConfig().getString(ConfigKeys.TOTEM_NAME, ""));
        int cmd     = plugin.getConfig().getInt(ConfigKeys.TOTEM_CONTAINER, 12345);

        if (meta.hasDisplayName() && meta.getDisplayName().equals(name)) return true;
        return meta.hasCustomModelData() && meta.getCustomModelData() == cmd;
    }

    public static void handleTotem(Player player,
                                   HeartService service,
                                   Lang lang) {
        UUID id     = player.getUniqueId();
        int current = service.getHearts(id);
        int max     = service.getMaxHearts();

        if (current >= max) {
            player.sendMessage(lang.msg("error_max_hearts", "max", max));
            return;
        }

        service.addHearts(id, 1);
        player.sendMessage(lang.msg("heart_recovered_thematic", "hearts", service.getHearts(id)));
    }

    public static ItemStack createTotem(JavaPlugin plugin) {
        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta   = totem.getItemMeta();
        if (meta != null) {
            String name = plugin.getConfig().getString(ConfigKeys.TOTEM_NAME, "");
            int cmd     = plugin.getConfig().getInt(ConfigKeys.TOTEM_CONTAINER, 12345);
            meta.setDisplayName(name);
            meta.setLore(plugin.getConfig().getStringList(ConfigKeys.TOTEM_LORE));
            meta.setCustomModelData(cmd);
            totem.setItemMeta(meta);
        }
        return totem;
    }
}
