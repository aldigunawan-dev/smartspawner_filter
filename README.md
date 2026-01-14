# SmartSpawner Filter Addon

SmartSpawner Filter injects lightweight controls around the SmartSpawner placement/break workflow and gives natural spawners a Silk Touch chance to reward a SmartSpawner item.

## Features

- Enforces `smartspawnerfilter.break.<entity>` and `smartspawnerfilter.place.<entity>` before SmartSpawner API events are allowed to proceed.
- Adds an optional Silk Touch-based drop for vanilla spawners with a configurable probability and permission.
- Provides configurable feedback messages so operators can tell players why an action failed or succeeded.

## Requirements

- Minecraft Paper/Folia 1.21+ (API version 1.20 is declared).
- [SmartSpawner](https://www.spigotmc.org/resources/smart-spawner-plugin.120743/) installed alongside this addon.
- The SmartSpawner API JAR must be present in your build path to compile (use the same version as the running plugin).

## Configuration

The defaults live in `config.yml`. Key options:

```yaml
permissions:
  place-format: smartspawnerfilter.place.%entity%      # append the lowercase EntityType (e.g. "zombie")
  break-format: smartspawnerfilter.break.%entity%      # e.g. smartspawnerfilter.break.zombie
  natural-drop: smartspawnerfilter.natural

natural:
  silk-touch:
    enabled: true                     # toggle the natural drop feature
    level: 1                          # minimum Silk Touch level needed
    chance: 0.35                      # probability per break
    require-permission: false         # gate the drop behind the permission above

messages:
  break: "&cYou are not permitted to break SmartSpawner blocks."
  place: "&cYou are not permitted to place SmartSpawner blocks."
  natural-drop: "&aSilk Touch rewarded you with a SmartSpawner for %entity%."
```

When the addon checks permissions it replaces `%entity%` with `EntityType.name().toLowerCase(Locale.ROOT)`, so `smartspawnerfilter.break.zombie` covers zombies and `smartspawnerfilter.place.creeper` covers creepers. You can still override the `place-format`/`break-format` templates with your own prefix (even without `%entity%`) if you prefer a flatter permission tree.

You can edit the messages to include `%player%`/`%entity%` placeholders and add color codes with `&`.

## Build & Install

1. Install the SmartSpawner API dependency that matches your server.
2. Run `mvn package` (Java 21+).
3. Drop the built JAR into `plugins/` alongside SmartSpawner.
4. Restart or reload the server.

## Development Notes

- This project uses Maven with the `spigot-api` and the `smartspawner-api` declared as provided dependencies.
- The silk-touch reward only triggers for vanilla spawners that SmartSpawner does not already handle.
