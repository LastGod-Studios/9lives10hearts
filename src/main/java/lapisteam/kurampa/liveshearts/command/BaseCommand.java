package lapisteam.kurampa.liveshearts.command;

import org.bukkit.command.CommandSender;

public interface BaseCommand {
    void execute(CommandSender sender, String[] args);
}
