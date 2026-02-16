package lapisteam.kurampa.liveshearts.command.impl;

import lapisteam.kurampa.liveshearts.command.BaseCommand;
import lapisteam.kurampa.liveshearts.config.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class ReloadCommand implements BaseCommand {

    private final JavaPlugin plugin;

    public ReloadCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        var lang = Lang.get();
        if (!sender.hasPermission("9l.reload")) {
            sender.sendMessage(lang.msg("no_permission"));
            return;
        }
        plugin.reloadConfig();
        Lang.get().load();
        sender.sendMessage(lang.msg("config_reloaded"));
    }
}
