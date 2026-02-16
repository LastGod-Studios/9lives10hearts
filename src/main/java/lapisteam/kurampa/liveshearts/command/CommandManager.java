package lapisteam.kurampa.liveshearts.command;

import lapisteam.kurampa.liveshearts.Main;
import lapisteam.kurampa.liveshearts.command.impl.*;
import lapisteam.kurampa.liveshearts.service.HeartService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.function.Supplier;

public class CommandManager implements CommandExecutor {

    private final Map<String, Supplier<BaseCommand>> registry;
    private final Main plugin;

    public CommandManager(Main plugin, HeartService heartService) {
        this.plugin = plugin;

        registry = Map.of(
                "add", () -> new AddCommand(heartService),
                "remove", () -> new RemoveCommand(heartService),
                "set", () -> new SetCommand(heartService),
                "look", () -> new LookCommand(heartService),
                "gift", () -> new GiftCommand(heartService),
                "resurrect", () -> new ResurrectCommand(heartService),
                "totem", () -> new TotemCommand(plugin),
                "reload", () -> new ReloadCommand(plugin)
        );
    }

    public void register() {
        plugin.getCommand("9l").setExecutor(this);
        plugin.getCommand("9l").setTabCompleter(new MainTabCompleter());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§c/9l <sub-command>");
            return false;
        }
        Supplier<BaseCommand> supplier = registry.get(args[0].toLowerCase());
        if (supplier == null) {
            sender.sendMessage("§cНеизвестная команда");
            return false;
        }
        supplier.get().execute(sender, args);
        return true;
    }
}
