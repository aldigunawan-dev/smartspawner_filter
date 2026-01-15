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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

final class FilterListener implements Listener {
  private final SmartSpawnerFilterAddon plugin;

  // Key for storing entity type in spawner item
  private final NamespacedKey entityTypeKey;

  FilterListener(SmartSpawnerFilterAddon plugin) {
    this.plugin = plugin;
    this.entityTypeKey = new NamespacedKey(plugin, "spawner_entity_type");
  }



  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onSpawnerPlace(SpawnerPlaceEvent event) {
    Player player = event.getPlayer();
    FilterConfiguration config = plugin.getFilterConfiguration();
    EntityType entityType = event.getEntityType();
    if (entityType == null) {
      entityType = EntityType.PIG;
    }
    String world = locationWorld(event.getLocation());
    if (hasEntityPermission(player, config, entityType, world, true)) {
      return;
    }
    event.setCancelled(true);
    sendMessage(player, config.getPlaceMessage(), entityPlaceholder(entityType, world));
  }

  // Handler for vanilla spawner placement (natural)
  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onNaturalSpawnerPlace(BlockPlaceEvent event) {
    Block block = event.getBlockPlaced();
    if (block.getType() != Material.SPAWNER) {
      return;
    }
    Player player = event.getPlayer();
    ItemStack item = event.getItemInHand();
    EntityType entityType = null;
    if (item != null && item.hasItemMeta()) {
      PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
      String entityTypeName = pdc.get(entityTypeKey, PersistentDataType.STRING);
      if (entityTypeName != null) {
        try {
          entityType = EntityType.valueOf(entityTypeName);
        } catch (IllegalArgumentException ex) {
          // fallback below
        }
      }
    }
    if (entityType == null) {
      entityType = EntityType.PIG;
    }
    FilterConfiguration config = plugin.getFilterConfiguration();
    String world = block.getWorld() != null ? block.getWorld().getName() : null;
    if (!hasEntityPermission(player, config, entityType, world, true)) {
      event.setCancelled(true);
      sendMessage(player, config.getPlaceMessage(), entityPlaceholder(entityType, world));
      return;
    }
    // Set spawned type on the placed spawner
    BlockState state = block.getState();
    if (state instanceof CreatureSpawner spawner) {
      spawner.setSpawnedType(entityType);
      spawner.update(true, false);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onSpawnerBreak(SpawnerPlayerBreakEvent event) {
    Player player = event.getPlayer();
    FilterConfiguration config = plugin.getFilterConfiguration();
    SmartSpawnerAPI api = plugin.getSmartSpawnerAPI();
    EntityType entityType = null;
    Location spawnerLocation = null;
    // Prefer block location if available
    try {
      Location loc = event.getLocation();
      if (loc != null && loc.getWorld() != null) {
        Block block = loc.getWorld().getBlockAt(loc);
        if (block.getType() == Material.SPAWNER) {
          spawnerLocation = block.getLocation();
        }
      }
    } catch (Throwable t) {
      // fallback to event.getLocation()
    }
    if (spawnerLocation == null) {
      spawnerLocation = event.getLocation();
    }
    if (api != null) {
      SpawnerDataDTO dto = api.getSpawnerByLocation(spawnerLocation);
      if (dto != null) {
        entityType = dto.getEntityType();
      } else {
        // Coba ambil entity type dari block state spawner vanilla
        Block block = spawnerLocation.getWorld().getBlockAt(spawnerLocation);
        BlockState state = block.getState(true);
        if (state instanceof CreatureSpawner spawner) {
          entityType = spawner.getSpawnedType();
        }
      }
    }
    String world = locationWorld(spawnerLocation);
    if (!hasEntityPermission(player, config, entityType, world, false)) {
      event.setCancelled(true);
      sendMessage(player, config.getBreakMessage(), entityPlaceholder(entityType, world));
      return;
    }
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

    BlockState stateBlock = block.getState(true); // force update
    if (!(stateBlock instanceof CreatureSpawner spawner)) {
      return;
    }

    EntityType entityType = spawner.getSpawnedType();
    if (entityType == null) {
      entityType = EntityType.PIG; // fallback default vanilla
      // Optional: debug log
      plugin.getLogger().warning("Spawner entity type not detected, fallback to PIG at " + block.getLocation());
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

    // Buat item spawner dan simpan entity type ke PDC
    ItemStack drop = plugin.createSmartSpawnerItem(entityType);
    if (drop == null) {
      return;
    }
    if (drop.hasItemMeta()) {
      var meta = drop.getItemMeta();
      meta.getPersistentDataContainer().set(entityTypeKey, PersistentDataType.STRING, entityType.name());
      drop.setItemMeta(meta);
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

  @SuppressWarnings("deprecation")
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
