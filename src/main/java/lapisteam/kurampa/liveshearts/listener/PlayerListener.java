package lapisteam.kurampa.liveshearts.listener;

import lapisteam.kurampa.liveshearts.service.HeartService;
import lapisteam.kurampa.liveshearts.config.ConfigKeys;
import lapisteam.kurampa.liveshearts.config.Lang;
import lapisteam.kurampa.liveshearts.util.ItemUtil;
import lapisteam.kurampa.liveshearts.util.VersionCompat;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerListener implements Listener {

    private final HeartService service;
    private final JavaPlugin plugin;

    public PlayerListener(HeartService service, JavaPlugin plugin) {
        this.service = service;
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        applyHearts(e.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        // RespawnEvent fires before the server has completed creating the
        // respawned player. Applying attributes here can be overwritten.
        Player player = e.getPlayer();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                applyHearts(player);
            }
        });
    }

    private void applyHearts(Player player) {
        int hearts = service.getHearts(player.getUniqueId());
        if (hearts <= 0) {
            player.setGameMode(GameMode.SPECTATOR);
            return;
        }
        VersionCompat.applyMaxHealth(player, Math.min(hearts, service.getMaxHearts()));
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent e) {
        var lang = Lang.get();
        if (!plugin.getConfig().getBoolean(ConfigKeys.RECOVERY_EAT_ENABLED, true)) return;

        Material food = Material.matchMaterial(
                plugin.getConfig().getString(ConfigKeys.RECOVERY_EAT_FOOD, "ENCHANTED_GOLDEN_APPLE")
        );
        if (food == null || e.getItem().getType() != food) return;

        Player player = e.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.sendMessage(lang.msg("hearts_spectator_mode"));
            return;
        }

        int current = service.getHearts(player.getUniqueId());
        int max = service.getMaxHearts();
        if (current >= max) {
            player.sendMessage(lang.msg("error_max_hearts", "max", max));
            return;
        }

        service.addHearts(player.getUniqueId(), 1);
        player.sendMessage(lang.msg("heart_recovered", "hearts", service.getHearts(player.getUniqueId())));
    }

    @EventHandler
    public void onResurrect(EntityResurrectEvent e) {
        var lang = Lang.get();
        if (e.isCancelled() || !(e.getEntity() instanceof Player player)) return;
        if (!plugin.getConfig().getBoolean(ConfigKeys.TOTEM_ENABLED, true)) return;

        if (ItemUtil.isUniqueTotem(player.getInventory().getItemInMainHand(), plugin)
                || ItemUtil.isUniqueTotem(player.getInventory().getItemInOffHand(), plugin)) {
            ItemUtil.handleTotem(player, service, lang);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player dead = e.getEntity();
        Player killer = dead.getKiller();
        service.handleDeath(dead);

        if (!plugin.getConfig().getBoolean(ConfigKeys.HEAD_HEART_ENABLED, true)) return;

        boolean onlyPvP = plugin.getConfig().getBoolean(ConfigKeys.HEAD_HEART_ONLY_PVP, true);
        double dropChance = plugin.getConfig().getDouble(ConfigKeys.HEAD_HEART_DROP_CHANCE, 1.0);
        boolean cursedEnabled = plugin.getConfig().getBoolean(ConfigKeys.HEAD_HEART_CURSED_ENABLED, false);
        double cursedChance = plugin.getConfig().getDouble(ConfigKeys.HEAD_HEART_CURSED_CHANCE, 0.0);

        if (onlyPvP && killer == null) return;
        if (Math.random() >= dropChance) return;

        if (cursedEnabled && Math.random() < cursedChance) {
            e.getDrops().add(ItemUtil.createCursedHead(dead, plugin));
        } else {
            e.getDrops().add(ItemUtil.createHeartHead(dead, plugin));
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        var lang = Lang.get();
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR
                && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (ItemUtil.isHeartHead(item, plugin)) {
            ItemUtil.handleHeartHead(player, item, service, lang);
            e.setCancelled(true);
            return;
        }

        if (ItemUtil.isCursedHead(item, plugin)) {
            ItemUtil.handleCursedHead(player, item, service, lang, plugin);
            e.setCancelled(true);
        }
    }
}
