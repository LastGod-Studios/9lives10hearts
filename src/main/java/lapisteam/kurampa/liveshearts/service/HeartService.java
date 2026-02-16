package lapisteam.kurampa.liveshearts.service;

import lapisteam.kurampa.liveshearts.config.ConfigKeys;
import lapisteam.kurampa.liveshearts.config.Lang;
import lapisteam.kurampa.liveshearts.storage.PlayerRepository;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public class HeartService {

    private final PlayerRepository repository;
    private final JavaPlugin plugin;

    public HeartService(PlayerRepository repository, JavaPlugin plugin) {
        this.repository = repository;
        this.plugin     = plugin;
    }

    private int getDefaultHearts() {
        return plugin.getConfig().getInt(ConfigKeys.HEARTS_DEFAULT, 10);
    }

    public int getMaxHearts() {
        return plugin.getConfig().getInt(ConfigKeys.HEARTS_MAX, 10);
    }

    public int getHearts(UUID playerId) {
        return repository.findHearts(playerId)
                .orElse(getDefaultHearts());
    }

    public void setHearts(UUID playerId, int hearts) {
        int h = Math.max(0, Math.min(getMaxHearts(), hearts));
        repository.saveHearts(playerId, h);
        applyHealthAttribute(playerId, h);
    }

    public void addHearts(UUID playerId, int delta) {
        setHearts(playerId, getHearts(playerId) + delta);
    }

    public void removeHearts(UUID playerId, int delta) {
        setHearts(playerId, getHearts(playerId) - delta);
    }

    public void handleDeath(Player player) {
        var lang = Lang.get();
        UUID id = player.getUniqueId();
        int current = getHearts(id);
        boolean immortal = plugin.getConfig().getString("gamemode", "hard")
                .equalsIgnoreCase("immortal");

        // Инкрементируем счётчик смертей
        int deaths = repository.findDeaths(id) + 1;
        repository.saveDeaths(id, deaths);

        // Проверяем порог бесплатных смертей
        int freeDeaths = plugin.getConfig().getInt(ConfigKeys.HEARTS_LOSS_AFTER_DEATH, 0);
        if (deaths <= freeDeaths) {
            return;
        }

        if (immortal) {
            if (current > 1) {
                setHearts(id, current - 1);
            }
            return;
        }

        if (current > 1) {
            setHearts(id, current - 1);
            player.sendMessage(lang.msg("hearts_decreased", "hearts", current - 1));
        } else {
            setHearts(id, 0);

            List<String> cmds = plugin.getConfig()
                    .getStringList(ConfigKeys.ON_ZERO_COMMANDS);

            if (cmds.isEmpty()) {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(lang.msg("spectator_mode"));
            } else {
                String name = player.getName();
                for (String raw : cmds) {
                    String cmd = raw.replace("{player}", name);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            }
        }
    }

    private void applyHealthAttribute(UUID playerId, int hearts) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;

        double maxHp = hearts * 2.0;
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHp);
        if (player.getHealth() > maxHp) {
            player.setHealth(maxHp);
        }
    }
}
