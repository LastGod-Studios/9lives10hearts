package lapisteam.kurampa.liveshearts.config;

import lapisteam.kurampa.liveshearts.util.ColorUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Lang {
    private static Lang INSTANCE;

    private final JavaPlugin plugin;
    private FileConfiguration lang;

    private Lang(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public static void init(JavaPlugin plugin) {
        if (INSTANCE == null) {
            INSTANCE = new Lang(plugin);
        }
    }

    public static Lang get() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Lang not initialized");
        }
        return INSTANCE;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "lang.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource("lang.yml", false);
        }
        lang = YamlConfiguration.loadConfiguration(file);
    }

    public String msg(String path, Object... placeholders) {
        String raw = lang.getString("messages." + path, "§c<" + path + ">");
        String s = ColorUtil.translateHex(raw);
        if (placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                s = s.replace("{" + placeholders[i] + "}", String.valueOf(placeholders[i + 1]));
            }
        } else {
            for (int i = 0; i < placeholders.length; i++) {
                s = s.replace("{" + i + "}", String.valueOf(placeholders[i]));
            }
        }
        return s;
    }
}
