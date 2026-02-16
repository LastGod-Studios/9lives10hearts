package lapisteam.kurampa.liveshearts.command.impl;

import lapisteam.kurampa.liveshearts.command.BaseCommand;
import lapisteam.kurampa.liveshearts.config.ConfigKeys;
import lapisteam.kurampa.liveshearts.config.Lang;
import lapisteam.kurampa.liveshearts.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class TotemCommand implements BaseCommand {

    private final JavaPlugin plugin;

    public TotemCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        var lang = Lang.get();
        if (!sender.hasPermission("9l.totem")) {
            sender.sendMessage(lang.msg("no_permission"));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(lang.msg("command_usage"));
            return;
        }
        if (!plugin.getConfig().getBoolean(ConfigKeys.TOTEM_ENABLED, true)) {
            player.sendMessage(lang.msg("totem_disabled"));
            return;
        }

        String name = ColorUtil.translateHex(
                plugin.getConfig().getString(ConfigKeys.TOTEM_NAME, "Unique Totem")
        );
        int cmd = plugin.getConfig().getInt(ConfigKeys.TOTEM_CONTAINER, 12345);

        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = totem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = plugin.getConfig()
                    .getStringList(ConfigKeys.TOTEM_LORE)
                    .stream()
                    .map(ColorUtil::translateHex)
                    .toList();
            meta.setLore(lore);
            meta.setCustomModelData(cmd);
            totem.setItemMeta(meta);
        }

        player.getInventory().addItem(totem);
        player.sendMessage(lang.msg("totem_received", "name", name));
    }
}
