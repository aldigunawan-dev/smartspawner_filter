package id.destaria.smartspawner.filter;

import java.util.Locale;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

final class FilterConfiguration {
  private final String placePermissionFormat;
  private final String breakPermissionFormat;
  private final String naturalDropPermissionFormat;
  private final String stackPermissionFormat;
  private final boolean naturalDropPermissionRequired;
  private final boolean naturalSilkEnabled;
  private final int naturalSilkLevel;
  private final double naturalSilkChance;
  private final String placeMessage;
  private final String breakMessage;
  private final String naturalDropMessage;
  private final String stackMessage;
  private final boolean stackFilterEnabled;

    private FilterConfiguration(
      String placePermissionFormat,
      String breakPermissionFormat,
      String naturalDropPermissionFormat,
      String stackPermissionFormat,
      boolean naturalDropPermissionRequired,
      boolean naturalSilkEnabled,
      int naturalSilkLevel,
      double naturalSilkChance,
      String placeMessage,
      String breakMessage,
      String naturalDropMessage,
      String stackMessage,
      boolean stackFilterEnabled) {
    this.placePermissionFormat = placePermissionFormat;
    this.breakPermissionFormat = breakPermissionFormat;
    this.naturalDropPermissionFormat = naturalDropPermissionFormat;
    this.naturalDropPermissionRequired = naturalDropPermissionRequired;
    this.naturalSilkEnabled = naturalSilkEnabled;
    this.naturalSilkLevel = naturalSilkLevel;
    this.naturalSilkChance = naturalSilkChance;
    this.placeMessage = placeMessage;
    this.breakMessage = breakMessage;
    this.naturalDropMessage = naturalDropMessage;
    this.stackPermissionFormat = stackPermissionFormat;
    this.stackMessage = stackMessage;
    this.stackFilterEnabled = stackFilterEnabled;
  }

  static FilterConfiguration load(FileConfiguration config) {
    String placePermissionFormat = config.getString("permissions.place-format");
    if (placePermissionFormat == null || placePermissionFormat.isBlank()) {
      placePermissionFormat = config.getString("permissions.place", "smartspawnerfilter.place.%entity%");
    }
    String breakPermissionFormat = config.getString("permissions.break-format");
    if (breakPermissionFormat == null || breakPermissionFormat.isBlank()) {
      breakPermissionFormat = config.getString("permissions.break", "smartspawnerfilter.break.%entity%");
    }
    String naturalPermissionFormat = config.getString("permissions.natural-format");
    if (naturalPermissionFormat == null || naturalPermissionFormat.isBlank()) {
      naturalPermissionFormat = config.getString("permissions.natural", "smartspawnerfilter.natural.%world%.%entity%");
    }
    String stackPermissionFormat = config.getString("permissions.stack-format", "smartspawnerfilter.stack.%world%");
    boolean naturalPermissionRequired = config.getBoolean("natural.silk-touch.require-permission", true);
    boolean naturalEnabled = config.getBoolean("natural.silk-touch.enabled", true);
    int silkLevel = Math.max(0, config.getInt("natural.silk-touch.level", 1));
    double chance = config.getDouble("natural.silk-touch.chance", 0.25);
    if (chance < 0) {
      chance = 0;
    } else if (chance > 1) {
      chance = 1;
    }

    String placeMessage = config.getString("messages.place", "&cYou cannot place spawners here.");
    String breakMessage = config.getString("messages.break", "&cYou cannot break spawners here.");
    String naturalMessage = config.getString(
        "messages.natural-drop", "&aSilk Touch granted you a SmartSpawner (%entity%).");

    boolean stackEnabled = config.getBoolean("stack-filter.enabled", true);
    String stackMessage = config.getString("messages.stack", "&cStacking spawners is not allowed in this world.");
    return new FilterConfiguration(
      placePermissionFormat,
      breakPermissionFormat,
      naturalPermissionFormat,
      stackPermissionFormat,
      naturalPermissionRequired,
      naturalEnabled,
      silkLevel,
      chance,
      placeMessage,
      breakMessage,
      naturalMessage,
      stackMessage,
      stackEnabled);
  }

  String getNaturalDropPermission(EntityType entityType, String world) {
    return formatPermission(naturalDropPermissionFormat, entityType, world);
  }

  boolean isNaturalDropPermissionRequired() {
    return naturalDropPermissionRequired;
  }

  boolean isNaturalSilkEnabled() {
    return naturalSilkEnabled;
  }

  int getNaturalSilkLevel() {
    return naturalSilkLevel;
  }

  double getNaturalSilkChance() {
    return naturalSilkChance;
  }

  String getPlaceMessage() {
    return placeMessage;
  }

  String getBreakMessage() {
    return breakMessage;
  }

  String getNaturalDropMessage() {
    return naturalDropMessage;
  }

  String getPlacePermission(EntityType entityType, String world) {
    return formatPermission(placePermissionFormat, entityType, world);
  }

  String getBreakPermission(EntityType entityType, String world) {
    return formatPermission(breakPermissionFormat, entityType, world);
  }

  String getStackPermission(String world) {
    return formatPermission(stackPermissionFormat, null, world);
  }

  String getStackMessage() {
    return stackMessage;
  }

  boolean isStackFilterEnabled() {
    return stackFilterEnabled;
  }

  private static String formatPermission(String template, EntityType entityType, String world) {
    if (template == null || template.isBlank()) {
      return null;
    }

    String formatted = template;
    if (entityType != null) {
      String entityKey = entityType.name().toLowerCase(Locale.ROOT);
      formatted = formatted.replace("%entity%", entityKey);
    }
    if (world != null && !world.isBlank()) {
      String worldKey = world.toLowerCase(Locale.ROOT);
      formatted = formatted.replace("%world%", worldKey);
    }
    return formatted;
  }
}
