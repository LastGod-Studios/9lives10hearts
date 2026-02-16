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

public final class GiftCommand implements BaseCommand {

    private final HeartService service;

    public GiftCommand(HeartService service) {
        this.service = service;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        var lang = Lang.get();
        if (!(sender instanceof Player giver)) {
            sender.sendMessage(lang.msg("gift_usage"));
            return;
        }
        if (args.length != 2) {
            giver.sendMessage(lang.msg("command_usage"));
            return;
        }

        OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
        if (!op.isOnline() || !op.hasPlayedBefore()) {
            giver.sendMessage(lang.msg("invalid_player"));
            return;
        }
        Player recipient = op.getPlayer();
        if (giver.equals(recipient)) {
            giver.sendMessage(lang.msg("cannot_gift_self"));
            return;
        }
        if (giver.getGameMode() == GameMode.SPECTATOR || recipient.getGameMode() == GameMode.SPECTATOR) {
            giver.sendMessage(lang.msg("hearts_spectator_mode"));
            return;
        }

        UUID giverId     = giver.getUniqueId();
        UUID recId       = recipient.getUniqueId();
        int giverHearts  = service.getHearts(giverId);
        int recHearts    = service.getHearts(recId);
        int max          = service.getMaxHearts();

        if (giverHearts <= 1) {
            giver.sendMessage(lang.msg("hearts_not_enough"));
            return;
        }
        if (recHearts >= max) {
            giver.sendMessage(lang.msg("recipient_max_hearts", "max", max));
            return;
        }

        service.removeHearts(giverId, 1);
        service.addHearts(recId, 1);

        giver.sendMessage(lang.msg("hearts_gifted", "player", recipient.getName(), "hearts", giverHearts - 1));
        recipient.sendMessage(lang.msg("hearts_received", "player", giver.getName(), "hearts", recHearts + 1));
    }
}
