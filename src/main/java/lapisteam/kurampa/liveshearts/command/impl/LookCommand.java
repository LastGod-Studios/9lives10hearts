package lapisteam.kurampa.liveshearts.command.impl;

import lapisteam.kurampa.liveshearts.command.BaseCommand;
import lapisteam.kurampa.liveshearts.config.Lang;
import lapisteam.kurampa.liveshearts.service.HeartService;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class LookCommand implements BaseCommand {

    private final HeartService service;

    public LookCommand(HeartService service) {
        this.service = service;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        var lang = Lang.get();
        if (!sender.hasPermission("9l.look")) {
            sender.sendMessage(lang.msg("no_permission"));
            return;
        }
        if (args.length != 2) {
            sender.sendMessage(lang.msg("command_usage"));
            return;
        }

        OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
        if (!op.hasPlayedBefore() && !op.isOnline()) {
            sender.sendMessage(lang.msg("invalid_player"));
            return;
        }
        UUID id = op.getUniqueId();
        int hearts = service.getHearts(id);

        Player online = op.getPlayer();
        if (online != null && online.getGameMode() == GameMode.SPECTATOR) {
            sender.sendMessage(lang.msg("hearts_spectator_mode"));
        } else {
            sender.sendMessage(lang.msg("hearts_look", "hearts", hearts));
        }
    }
}
