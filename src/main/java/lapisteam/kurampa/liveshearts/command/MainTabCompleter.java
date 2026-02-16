package lapisteam.kurampa.liveshearts.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import lapisteam.kurampa.liveshearts.command.tab.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MainTabCompleter implements TabCompleter {

    private final Map<String, TabCompleter> subs = Map.of(
            "add", new AddTabCompleter(),
            "remove", new RemoveTabCompleter(),
            "set", new SetTabCompleter(),
            "look", new LookTabCompleter(),
            "gift", new GiftTabCompleter(),
            "resurrect", new ResurrectTabCompleter(),
            "totem", new TotemTabCompleter(),
            "reload", new ReloadTabCompleter()
    );

    private final List<String> subNames = new ArrayList<>(subs.keySet());

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      String[] args) {
        if (args.length == 0) return subNames;
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> out = new ArrayList<>();
            for (String name : subNames) {
                if (name.startsWith(partial)) out.add(name);
            }
            return out;
        }
        TabCompleter completer = subs.get(args[0].toLowerCase());
        if (completer != null) {
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            List<String> result = completer.onTabComplete(sender, command, alias, subArgs);
            return result == null ? Collections.emptyList() : result;
        }
        return Collections.emptyList();
    }
}
