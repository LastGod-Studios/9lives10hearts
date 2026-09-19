package lapisteam.kurampa.liveshearts.util;

import lapisteam.kurampa.liveshearts.config.ConfigKeys;
import lapisteam.kurampa.liveshearts.config.Lang;
import lapisteam.kurampa.liveshearts.service.HeartService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ItemUtil {

    // Config values contain the standard base64-encoded Mojang textures JSON.
    // Validate the host before passing a texture URL to the Bukkit profile API.
    private static final Pattern SKIN_URL = Pattern.compile(
            "\"url\"\\s*:\\s*\"(https?://textures\\.minecraft\\.net/texture/[a-zA-Z0-9]+)\"");

    private ItemUtil() {
    }

    public static boolean isHeartHead(ItemStack item, JavaPlugin plugin) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null
                && meta.hasCustomModelData()
                && meta.getCustomModelData() == plugin.getConfig().getInt(ConfigKeys.HEAD_HEART_CONTAINER);
    }

    public static boolean isCursedHead(ItemStack item, JavaPlugin plugin) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null
                && meta.hasCustomModelData()
                && meta.getCustomModelData() == plugin.getConfig().getInt(ConfigKeys.HEAD_HEART_CURSED_CONTAINER);
    }

    public static ItemStack createHeartHead(Player dead, JavaPlugin plugin) {
        return createHead(dead, plugin, ConfigKeys.HEAD_HEART_NAME,
                ConfigKeys.HEAD_HEART_LORE,
                ConfigKeys.HEAD_HEART_CONTAINER,
                ConfigKeys.HEAD_HEART_VALUE,
                1);
    }

    public static ItemStack createCursedHead(Player dead, JavaPlugin plugin) {
        return createHead(dead, plugin, ConfigKeys.HEAD_HEART_CURSED_NAME,
                ConfigKeys.HEAD_HEART_CURSED_LORE,
                ConfigKeys.HEAD_HEART_CURSED_CONTAINER,
                ConfigKeys.HEAD_HEART_CURSED_VALUE,
                plugin.getConfig().getInt(ConfigKeys.HEAD_HEART_CURSED_AMOUNT, 1));
    }

    private static ItemStack createHead(Player dead,
                                        JavaPlugin plugin,
                                        String nameKey,
                                        String loreKey,
                                        String containerKey,
                                        String valueKey,
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
                                .replace("{amount}", String.valueOf(amount))))
                .toList());

        meta.setCustomModelData(plugin.getConfig().getInt(containerKey, 12345));
        setHeadTexture(meta, plugin.getConfig().getString(valueKey, ""), dead, plugin);
        head.setItemMeta(meta);
        return head;
    }

    private static void setHeadTexture(SkullMeta meta, String encoded,
                                       Player dead, JavaPlugin plugin) {
        if (encoded == null || encoded.isBlank()) {
            meta.setOwningPlayer(dead);
            return;
        }
        try {
            String json = new String(Base64.getDecoder().decode(encoded.trim()), StandardCharsets.UTF_8);
            Matcher matcher = SKIN_URL.matcher(json);
            if (!matcher.find()) {
                throw new IllegalArgumentException("No valid textures.minecraft.net skin URL");
            }
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(matcher.group(1)));
            profile.setTextures(textures);
            meta.setOwnerProfile(profile);
        } catch (IllegalArgumentException | MalformedURLException exception) {
            plugin.getLogger().warning("Invalid head texture, falling back to player skin: " + exception.getMessage());
            meta.setOwningPlayer(dead);
        }
    }

    /** The listener only calls this for a head held in the main hand. */
    private static void consumeMainHand(Player player, ItemStack item) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
    }

    public static void handleHeartHead(Player player,
                                       ItemStack item,
                                       HeartService service,
                                       Lang lang) {
        UUID id = player.getUniqueId();
        int current = service.getHearts(id);
        int max = service.getMaxHearts();

        if (current >= max) {
            player.sendMessage(lang.msg("error_max_hearts", "max", max));
            return;
        }

        consumeMainHand(player, item);
        service.addHearts(id, 1);
        player.sendMessage(lang.msg("heart_recovered", "hearts", service.getHearts(id)));
    }

    public static void handleCursedHead(Player player,
                                        ItemStack item,
                                        HeartService service,
                                        Lang lang,
                                        JavaPlugin plugin) {
        UUID id = player.getUniqueId();
        int current = service.getHearts(id);
        int amount = plugin.getConfig().getInt(ConfigKeys.HEAD_HEART_CURSED_AMOUNT, 1);

        if (current <= 0) {
            player.sendMessage(lang.msg("hearts_spectator_mode"));
            return;
        }

        consumeMainHand(player, item);
        service.removeHearts(id, amount);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 0));
        PotionEffectType nausea = VersionCompat.nauseaEffect();
        if (nausea != null) {
            player.addPotionEffect(new PotionEffect(nausea, 5 * 20, 0));
        }
        player.sendMessage(lang.msg("cursed_used", "amount", amount));
    }

    public static boolean isUniqueTotem(ItemStack item, JavaPlugin plugin) {
        if (item == null || item.getType() != Material.TOTEM_OF_UNDYING) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        String name = ColorUtil.translateHex(plugin.getConfig().getString(ConfigKeys.TOTEM_NAME, ""));
        int cmd = plugin.getConfig().getInt(ConfigKeys.TOTEM_CONTAINER, 12345);

        if (meta.hasDisplayName() && meta.getDisplayName().equals(name)) return true;
        return meta.hasCustomModelData() && meta.getCustomModelData() == cmd;
    }

    public static void handleTotem(Player player,
                                   HeartService service,
                                   Lang lang) {
        UUID id = player.getUniqueId();
        int current = service.getHearts(id);
        int max = service.getMaxHearts();

        if (current >= max) {
            player.sendMessage(lang.msg("error_max_hearts", "max", max));
            return;
        }

        service.addHearts(id, 1);
        player.sendMessage(lang.msg("heart_recovered_thematic", "hearts", service.getHearts(id)));
    }

    public static ItemStack createTotem(JavaPlugin plugin) {
        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = totem.getItemMeta();
        if (meta != null) {
            String name = plugin.getConfig().getString(ConfigKeys.TOTEM_NAME, "");
            int cmd = plugin.getConfig().getInt(ConfigKeys.TOTEM_CONTAINER, 12345);
            meta.setDisplayName(ColorUtil.translateHex(name));
            meta.setLore(plugin.getConfig().getStringList(ConfigKeys.TOTEM_LORE)
                    .stream().map(ColorUtil::translateHex).toList());
            meta.setCustomModelData(cmd);
            totem.setItemMeta(meta);
        }
        return totem;
    }
}
