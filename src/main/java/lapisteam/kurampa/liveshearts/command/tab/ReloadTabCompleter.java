package lapisteam.kurampa.liveshearts.command.tab;

import org.bukkit.command.*;

import java.util.List;

public class ReloadTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender,
                                      Command command,
                                      String alias,
                                      String[] args) {
        return List.of();
    }
}
