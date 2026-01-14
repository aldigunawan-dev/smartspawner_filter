package id.destaria.smartspawner.filter;

import github.nighter.smartspawner.api.SmartSpawnerAPI;
import github.nighter.smartspawner.api.data.SpawnerDataDTO;
import github.nighter.smartspawner.api.events.SpawnerPlayerBreakEvent;
import github.nighter.smartspawner.api.events.SpawnerPlaceEvent;
import github.nighter.smartspawner.api.events.SpawnerStackEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

final class FilterListener implements Listener {
  private final SmartSpawnerFilterAddon plugin;

  FilterListener(SmartSpawnerFilterAddon plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onSpawnerPlace(SpawnerPlaceEvent event) {
    Player player = event.getPlayer();
    FilterConfiguration config = plugin.getFilterConfiguration();
    EntityType entityType = event.getEntityType();
    String world = locationWorld(event.getLocation());
    if (hasEntityPermission(player, config, entityType, world, true)) {
      return;
    }

    event.setCancelled(true);
    sendMessage(player, config.getPlaceMessage(), entityPlaceholder(entityType, world));
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onSpawnerBreak(SpawnerPlayerBreakEvent event) {
    Player player = event.getPlayer();
    FilterConfiguration config = plugin.getFilterConfiguration();
    SmartSpawnerAPI api = plugin.getSmartSpawnerAPI();
    EntityType entityType = null;
    if (api != null) {
      SpawnerDataDTO dto = api.getSpawnerByLocation(event.getLocation());
      if (dto != null) {
        entityType = dto.getEntityType();
      }
    }

    String world = locationWorld(event.getLocation());
    if (hasEntityPermission(player, config, entityType, world, false)) {
      return;
    }

    event.setCancelled(true);
    sendMessage(player, config.getBreakMessage(), entityPlaceholder(entityType, world));
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onNaturalSpawnerBreak(BlockBreakEvent event) {
    FilterConfiguration config = plugin.getFilterConfiguration();
    if (!config.isNaturalSilkEnabled()) {
      return;
    }

    Player player = event.getPlayer();
    if (player.getGameMode() == GameMode.CREATIVE) {
      return;
    }

    Block block = event.getBlock();
    if (block.getType() != Material.SPAWNER) {
      return;
    }

    SmartSpawnerAPI api = plugin.getSmartSpawnerAPI();
    if (api != null && api.getSpawnerByLocation(block.getLocation()) != null) {
      return;
    }

    BlockState stateBlock = block.getState(false);
    if (!(stateBlock instanceof CreatureSpawner spawner)) {
      return;
    }

    EntityType entityType = spawner.getSpawnedType();
    if (entityType == null) {
      return;
    }

    String world = block.getWorld() != null ? block.getWorld().getName() : null;
    if (!hasEntityPermission(player, config, entityType, world, false)) {
      event.setCancelled(true);
      sendMessage(player, config.getBreakMessage(), entityPlaceholder(entityType, world));
      return;
    }

    ItemStack tool = player.getInventory().getItemInMainHand();
    if (tool == null) {
      return;
    }

    if (tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) < config.getNaturalSilkLevel()) {
      return;
    }

    if (config.isNaturalDropPermissionRequired()
        && !player.hasPermission(config.getNaturalDropPermission(entityType, world))) {
      return;
    }

    double chance = config.getNaturalSilkChance();
    if (chance <= 0 || ThreadLocalRandom.current().nextDouble() >= chance) {
      return;
    }

    ItemStack drop = plugin.createSmartSpawnerItem(entityType);
    if (drop == null) {
      return;
    }

    event.setDropItems(false);
    event.setExpToDrop(0);
    block.getWorld().dropItemNaturally(block.getLocation().toCenterLocation(), drop);

    sendMessage(player, config.getNaturalDropMessage(), entityPlaceholder(entityType, world));
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onSpawnerStack(SpawnerStackEvent event) {
    FilterConfiguration config = plugin.getFilterConfiguration();
    if (!config.isStackFilterEnabled()) {
      return;
    }

    Player player = event.getPlayer();
    if (player == null) {
      return;
    }

    String world = locationWorld(event.getLocation());
    String stackPermission = config.getStackPermission(world);
    if (stackPermission != null && player.hasPermission(stackPermission)) {
      return;
    }

    event.setCancelled(true);
    sendMessage(player, config.getStackMessage(), worldPlaceholder(world));
  }

  private void sendMessage(Player player, String template, Map<String, String> replacements) {
    if (player == null || template == null || template.isEmpty()) {
      return;
    }

    String text = template.replace("%player%", player.getName());
    if (replacements != null) {
      for (Map.Entry<String, String> placeholder : replacements.entrySet()) {
        text = text.replace(placeholder.getKey(), placeholder.getValue());
      }
    }

    player.sendMessage(ChatColor.translateAlternateColorCodes('&', text));
  }

  private static String locationWorld(Location location) {
    if (location == null) {
      return null;
    }
    World world = location.getWorld();
    return world != null ? world.getName() : null;
  }

  private boolean hasEntityPermission(Player player,
      FilterConfiguration config,
      EntityType entityType,
      String world,
      boolean forPlace) {
    String permission = forPlace
        ? config.getPlacePermission(entityType, world)
        : config.getBreakPermission(entityType, world);
    return permission != null && player.hasPermission(permission);
  }

  private Map<String, String> entityPlaceholder(EntityType entityType, String worldName) {
    Map<String, String> replacements = new HashMap<>();
    replacements.put("%entity%", entityType != null ? entityType.name() : "unknown");
    if (worldName != null) {
      replacements.put("%world%", worldName);
    }
    return replacements;
  }

  private Map<String, String> worldPlaceholder(String worldName) {
    return Map.of("%world%", worldName != null ? worldName : "unknown");
  }
}
