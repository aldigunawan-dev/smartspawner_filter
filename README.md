# SmartSpawner Filter Addon

SmartSpawner Filter injects lightweight controls around the SmartSpawner placement/break workflow and gives natural spawners a Silk Touch chance to reward a SmartSpawner item.

## Features

- Enforces per-world and per-mob permissions (e.g. `sff.break.world.zombie`) before SmartSpawner placement/break actions are allowed.
- Grants natural Silk Touch drops only when players hold the matching world-aware break permission and the optional natural permission node.
- Cancels SmartSpawner stacking attempts in worlds where the `sff.stack.%world%` node is missing while still letting other events proceed.
- Provides configurable feedback messages that include `%player%`, `%world%`, and `%entity%` placeholders.

## Requirements

- Minecraft Paper/Folia 1.21+ (API version 1.20 is declared).
- [SmartSpawner](https://www.spigotmc.org/resources/smart-spawner-plugin.120743/) installed alongside this addon.
- The SmartSpawner API JAR must be present in your build path to compile (use the same version as the running plugin).

## Configuration

The defaults live in `config.yml`. Key options:

```yaml
permissions:
  place-format: sff.place.%world%.%entity%
  break-format: sff.break.%world%.%entity%
  natural-format: sff.natural.%world%.%entity%
  stack-format: sff.stack.%world%

natural:
  silk-touch:
    enabled: true
    level: 1
    chance: 0.35
    require-permission: true        # drop follows the natural-format node per world and mob

stack-filter:
  enabled: true

messages:
  break: "&cYou are not permitted to break %entity% spawners in %world%."
  place: "&cYou are not permitted to place %entity% spawners in %world%."
  natural-drop: "&aSilk Touch rewarded you with a %entity% spawner in %world%."
  stack: "&cStacking spawners is not permitted in %world%."
```

The addon replaces `%entity%` with `EntityType.name()` and `%world%` with the world name, so nodes such as `sff.break.world.zombie` and `sff.natural.world.zombie` are required for each context. Change the `stack-format` if you want a different node layout for stacking, but the default `sff.stack.<world>` only filters the world-level interaction.

Messages can include `%player%`, `%entity%`, and `%world%` placeholders along with Bukkit color codes (`&`).

## Build & Install

1. Install the SmartSpawner API dependency that matches your server.
2. Run `mvn package` (Java 21+).
3. Drop the built JAR into `plugins/` alongside SmartSpawner.
4. Restart or reload the server.

## Development Notes

- This project uses Maven with the `spigot-api` and the `smartspawner-api` declared as provided dependencies.
- The silk-touch reward only triggers for vanilla spawners that SmartSpawner does not already handle.
