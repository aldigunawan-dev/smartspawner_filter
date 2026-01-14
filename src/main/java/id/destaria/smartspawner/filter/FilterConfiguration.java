package id.destaria.smartspawner.filter;

import java.util.Locale;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

final class FilterConfiguration {
  private final String placePermissionFormat;
  private final String breakPermissionFormat;
  private final String naturalDropPermission;
  private final boolean naturalDropPermissionRequired;
  private final boolean naturalSilkEnabled;
  private final int naturalSilkLevel;
  private final double naturalSilkChance;
  private final String placeMessage;
  private final String breakMessage;
  private final String naturalDropMessage;

  private FilterConfiguration(
      String placePermissionFormat,
      String breakPermissionFormat,
      String naturalDropPermission,
      boolean naturalDropPermissionRequired,
      boolean naturalSilkEnabled,
      int naturalSilkLevel,
      double naturalSilkChance,
      String placeMessage,
      String breakMessage,
      String naturalDropMessage) {
    this.placePermissionFormat = placePermissionFormat;
    this.breakPermissionFormat = breakPermissionFormat;
    this.naturalDropPermission = naturalDropPermission;
    this.naturalDropPermissionRequired = naturalDropPermissionRequired;
    this.naturalSilkEnabled = naturalSilkEnabled;
    this.naturalSilkLevel = naturalSilkLevel;
    this.naturalSilkChance = naturalSilkChance;
    this.placeMessage = placeMessage;
    this.breakMessage = breakMessage;
    this.naturalDropMessage = naturalDropMessage;
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
    String naturalPermission = config.getString("permissions.natural-drop", "smartspawnerfilter.natural");
    boolean naturalPermissionRequired = config.getBoolean("natural.silk-touch.require-permission", false);
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

    return new FilterConfiguration(
        placePermissionFormat,
        breakPermissionFormat,
        naturalPermission,
        naturalPermissionRequired,
        naturalEnabled,
        silkLevel,
        chance,
        placeMessage,
        breakMessage,
        naturalMessage);
  }

  String getNaturalDropPermission() {
    return naturalDropPermission;
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

  String getPlacePermission(EntityType entityType) {
    return formatPermission(placePermissionFormat, entityType);
  }

  String getBreakPermission(EntityType entityType) {
    return formatPermission(breakPermissionFormat, entityType);
  }

  private static String formatPermission(String template, EntityType entityType) {
    if (template == null || template.isBlank()) {
      return null;
    }

    if (entityType == null) {
      return template;
    }

    String entityKey = entityType.name().toLowerCase(Locale.ROOT);
    return template.replace("%entity%", entityKey);
  }
}
