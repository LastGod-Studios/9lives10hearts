package lapisteam.kurampa.liveshearts.command.impl;

import lapisteam.kurampa.liveshearts.command.BaseCommand;
import lapisteam.kurampa.liveshearts.config.Lang;
import lapisteam.kurampa.liveshearts.service.HeartService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public final class SetCommand implements BaseCommand {

    private final HeartService service;

    public SetCommand(HeartService service) {
        this.service = service;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        var lang = Lang.get();
        if (!sender.hasPermission("9l.set")) {
            sender.sendMessage(lang.msg("no_permission"));
            return;
        }
        if (args.length != 3) {
            sender.sendMessage(lang.msg("command_usage"));
            return;
        }

        OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
        UUID id = op.getUniqueId();
        if (!op.hasPlayedBefore() && !op.isOnline()) {
            sender.sendMessage(lang.msg("invalid_player"));
            return;
        }

        int hearts;
        try {
            hearts = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(lang.msg("invalid_number"));
            return;
        }

        int max = service.getMaxHearts();
        if (hearts < 0 || hearts > max) {
            sender.sendMessage(lang.msg("invalid_number", "max", max));
            return;
        }

        service.setHearts(id, hearts);
        sender.sendMessage(lang.msg("hearts_set", "hearts", hearts));
    }
}
