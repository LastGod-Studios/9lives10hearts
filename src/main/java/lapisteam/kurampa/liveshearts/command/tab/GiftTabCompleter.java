package lapisteam.kurampa.liveshearts.command.tab;

import org.bukkit.Bukkit;
import org.bukkit.command.*;

import java.util.List;
import java.util.stream.Collectors;

public class GiftTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender,
                                      Command command,
                                      String alias,
                                      String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(p -> p.getName())
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
