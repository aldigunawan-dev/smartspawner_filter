package id.destaria.smartspawner.filter;

import org.bukkit.ChatColor;
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
      sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " reload");
      return true;
    }

    if (!sender.hasPermission("sff.reload")) {
      sender.sendMessage(ChatColor.RED + "You do not have permission to reload SmartSpawner Filter.");
      return true;
    }

    plugin.reloadConfig();
    sender.sendMessage(ChatColor.GREEN + "SmartSpawner Filter configuration reloaded.");
    return true;
  }
}
