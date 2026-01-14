package id.destaria.smartspawner.filter;

import github.nighter.smartspawner.api.SmartSpawnerAPI;
import github.nighter.smartspawner.api.SmartSpawnerProvider;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class SmartSpawnerFilterAddon extends JavaPlugin {
  private SmartSpawnerAPI smartSpawnerAPI;
  private FilterConfiguration configuration;

  @Override
  public void onEnable() {
    saveDefaultConfig();
    smartSpawnerAPI = SmartSpawnerProvider.getAPI();
    if (smartSpawnerAPI == null) {
      getLogger().warning("SmartSpawner API not detected; disabling");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    reloadFilterConfig();
    getServer().getPluginManager().registerEvents(new FilterListener(this), this);
    registerCommands();
  }

  @Override
  public void reloadConfig() {
    super.reloadConfig();
    reloadFilterConfig();
  }

  void reloadFilterConfig() {
    configuration = FilterConfiguration.load(getConfig());
  }

  private void registerCommands() {
    if (getCommand("smartspawnerfilter") != null) {
      getCommand("smartspawnerfilter").setExecutor(new ReloadCommand(this));
    }
  }

  public FilterConfiguration getFilterConfiguration() {
    return configuration;
  }

  public SmartSpawnerAPI getSmartSpawnerAPI() {
    return smartSpawnerAPI;
  }

  public ItemStack createSmartSpawnerItem(EntityType entityType) {
    if (entityType == null || smartSpawnerAPI == null) {
      return null;
    }
    return smartSpawnerAPI.createSpawnerItem(entityType);
  }
}
