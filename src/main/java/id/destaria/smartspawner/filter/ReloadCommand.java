package id.destaria.smartspawner.filter;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

final class ReloadCommand implements CommandExecutor {
  private final SmartSpawnerFilterAddon plugin;

  ReloadCommand(SmartSpawnerFilterAddon plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length != 1 || !"reload".equalsIgnoreCase(args[0])) {
      sender.sendMessage(Component.text("Usage: /" + label + " reload").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
      return true;
    }

    if (!sender.hasPermission("sff.reload")) {
      sender.sendMessage(Component.text("You do not have permission to reload SmartSpawner Filter.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
      return true;
    }

    plugin.reloadConfig();
    sender.sendMessage(Component.text("SmartSpawner Filter configuration reloaded.").color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
    return true;
  }
}
