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

public final class RemoveCommand implements BaseCommand {

    private final HeartService service;

    public RemoveCommand(HeartService service) {
        this.service = service;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        var lang = Lang.get();
        if (!sender.hasPermission("9l.remove")) {
            sender.sendMessage(lang.msg("no_permission"));
            return;
        }
        if (args.length != 3) {
            sender.sendMessage(lang.msg("command_usage"));
            return;
        }

        OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
        if (!op.isOnline() && !op.hasPlayedBefore()) {
            sender.sendMessage(lang.msg("invalid_player"));
            return;
        }
        Player target = op.getPlayer();
        if (target == null) {
            sender.sendMessage(lang.msg("invalid_player"));
            return;
        }
        if (target.getGameMode() == GameMode.SPECTATOR) {
            sender.sendMessage(lang.msg("hearts_spectator_mode"));
            return;
        }

        int delta;
        try {
            delta = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(lang.msg("invalid_number"));
            return;
        }

        UUID id = target.getUniqueId();
        int before = service.getHearts(id);
        int after  = before - delta;

        if (after <= 0) {
            service.setHearts(id, 0);
            target.setHealth(0.0);
            sender.sendMessage(lang.msg("hearts_set", "hearts", 0));
        } else {
            service.removeHearts(id, delta);
            sender.sendMessage(lang.msg("hearts_set", "hearts", after));
        }
    }
}
