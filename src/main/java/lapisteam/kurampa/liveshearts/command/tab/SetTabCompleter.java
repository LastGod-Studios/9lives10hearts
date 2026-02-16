package lapisteam.kurampa.liveshearts.command.tab;

import org.bukkit.Bukkit;
import org.bukkit.command.*;

import java.util.List;
import java.util.stream.Collectors;

public class SetTabCompleter implements TabCompleter {
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
        if (args.length == 2) {
            return List.of("1","2","3","4","5","6","7","8","9","10")
                    .stream()
                    .filter(s -> s.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
