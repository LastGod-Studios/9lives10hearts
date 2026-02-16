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

public final class ResurrectCommand implements BaseCommand {

    private final HeartService service;

    public ResurrectCommand(HeartService service) {
        this.service = service;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        var lang = Lang.get();
        if (!sender.hasPermission("9l.resurrect")) {
            sender.sendMessage(lang.msg("no_permission"));
            return;
        }
        if (args.length != 3) {
            sender.sendMessage(lang.msg("command_usage"));
            return;
        }

        OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
        if (!op.isOnline() || !op.hasPlayedBefore()) {
            sender.sendMessage(lang.msg("invalid_player"));
            return;
        }
        Player target = op.getPlayer();
        if (target.getGameMode() != GameMode.SPECTATOR) {
            sender.sendMessage(lang.msg("not_in_spectator"));
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
        if (hearts < 1 || hearts > max) {
            sender.sendMessage(lang.msg("invalid_number", "max", max));
            return;
        }

        UUID id = target.getUniqueId();
        service.setHearts(id, hearts);
        target.setGameMode(GameMode.SURVIVAL);

        sender.sendMessage(lang.msg("resurrected", "player", target.getName(), "hearts", hearts));
        target.sendMessage(lang.msg("you_have_been_resurrected", "hearts", hearts));
    }
}
