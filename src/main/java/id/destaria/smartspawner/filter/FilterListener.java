package id.destaria.smartspawner.filter;

import github.nighter.smartspawner.api.SmartSpawnerAPI;
import github.nighter.smartspawner.api.data.SpawnerDataDTO;
import github.nighter.smartspawner.api.events.SpawnerPlayerBreakEvent;
import github.nighter.smartspawner.api.events.SpawnerPlaceEvent;
import github.nighter.smartspawner.api.events.SpawnerStackEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

final class FilterListener implements Listener {
  private final SmartSpawnerFilterAddon plugin;
  // Removed unused entityTypeKey

  FilterListener(SmartSpawnerFilterAddon plugin) {
    this.plugin = plugin;
    // Removed unused entityTypeKey initialization
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onSpawnerPlace(SpawnerPlaceEvent event) {
    Player player = event.getPlayer();
    if (player == null) return;
    FilterConfiguration config = plugin.getFilterConfiguration();
    EntityType entityType = event.getEntityType() != null ? event.getEntityType() : EntityType.PIG;
    String world = locationWorld(event.getLocation());
    if (!hasEntityPermission(player, config, entityType, world, true)) {
      event.setCancelled(true);
      sendMessage(player, config.getPlaceMessage(), entityPlaceholder(entityType, world));
    }
  }
  // Handler for vanilla spawner placement removed: SmartSpawner will convert all placed spawners.

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onSpawnerBreak(SpawnerPlayerBreakEvent event) {
    Player player = event.getPlayer();
    if (player == null) return;
    FilterConfiguration config = plugin.getFilterConfiguration();
    SmartSpawnerAPI api = plugin.getSmartSpawnerAPI();
    EntityType entityType = null;
    Location spawnerLocation = event.getLocation();
    boolean isSmartSpawner = false;
    if (api != null) {
      SpawnerDataDTO dto = api.getSpawnerByLocation(spawnerLocation);
      if (dto != null) {
        entityType = dto.getEntityType();
        isSmartSpawner = true;
      } else {
        Block block = spawnerLocation.getWorld().getBlockAt(spawnerLocation);
        BlockState state = block.getState(true);
        if (state instanceof CreatureSpawner spawner) {
          entityType = spawner.getSpawnedType();
        }
      }
    }
    if (entityType == null) {
      entityType = EntityType.PIG;
    }
    String world = locationWorld(spawnerLocation);
    
    //Bypass if in creative mode
    if (player.getGameMode() == GameMode.CREATIVE) {
      return;
    }

    ItemStack tool = player.getInventory().getItemInMainHand();
    if (tool == null || tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) < config.getNaturalSilkLevel()) {
      // Destroy spawner without drop and without message
      event.setCancelled(true);
      destroySpawnerBlock(spawnerLocation);
      return;
    }
    if (isSmartSpawner) {
      if (!hasEntityPermission(player, config, entityType, world, false)) {
        event.setCancelled(true);
        sendMessage(player, config.getBreakMessage(), entityPlaceholder(entityType, world));
        return;
      }
    } else {
      if (config.isNaturalDropPermissionRequired()) {
        String perm = config.getNaturalDropPermission(entityType, world);
        if (perm == null || !player.hasPermission(perm)) {
          event.setCancelled(true);
          sendMessage(player, config.getBreakMessage(), entityPlaceholder(entityType, world));
          return;
        }
      }
      double chance = config.getNaturalSilkChance();
      if (chance <= 0 || ThreadLocalRandom.current().nextDouble() >= chance) {
        event.setCancelled(true);
        destroySpawnerBlock(spawnerLocation);
        return;
      }
    }
  }

  // No vanilla BlockBreakEvent handler needed: all logic handled in SpawnerPlayerBreakEvent

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onSpawnerStack(SpawnerStackEvent event) {
    FilterConfiguration config = plugin.getFilterConfiguration();
    if (!config.isStackFilterEnabled()) return;
    Player player = event.getPlayer();
    if (player == null) return;
    String world = locationWorld(event.getLocation());
    String stackPermission = config.getStackPermission(world);
    if (stackPermission != null && player.hasPermission(stackPermission)) return;
    event.setCancelled(true);
    sendMessage(player, config.getStackMessage(), worldPlaceholder(world));
  }

  private void sendMessage(Player player, String template, Map<String, String> replacements) {
    if (player == null || template == null || template.isEmpty()) return;
    String text = template.replace("%player%", player.getName());
    if (replacements != null) {
      for (Map.Entry<String, String> placeholder : replacements.entrySet()) {
        text = text.replace(placeholder.getKey(), placeholder.getValue());
      }
    }
    player.sendMessage(MiniMessage.miniMessage().deserialize(text));
  }

  /**
   * Destroy the spawner block at the given location (set to AIR).
   */
  private void destroySpawnerBlock(Location location) {
    if (location != null && location.getWorld() != null) {
      Block block = location.getWorld().getBlockAt(location);
      block.setType(Material.AIR);
    }
  }

  private static String locationWorld(Location location) {
    if (location == null || location.getWorld() == null) return null;
    return location.getWorld().getName();
  }

  private boolean hasEntityPermission(Player player,
      FilterConfiguration config,
      EntityType entityType,
      String world,
      boolean forPlace) {
    if (player == null || config == null || entityType == null || world == null) return false;
    String permission = forPlace
        ? config.getPlacePermission(entityType, world)
        : config.getBreakPermission(entityType, world);
    if (permission == null) return false;
    return player.hasPermission(permission);
  }

  private Map<String, String> entityPlaceholder(EntityType entityType, String worldName) {
    Map<String, String> replacements = new HashMap<>();
    replacements.put("%entity%", entityType != null ? entityType.name() : "PIG");
    replacements.put("%world%", worldName != null ? worldName : "unknown");
    return replacements;
  }

  private Map<String, String> worldPlaceholder(String worldName) {
    return Map.of("%world%", worldName != null ? worldName : "unknown");
  }
}
