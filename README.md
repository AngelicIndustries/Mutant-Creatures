# Mutant Creatures

Maintained fork of Mutant Creatures for Minecraft 1.7.10, adding mutant versions of familiar mobs and their equipment.

[![hub](images/badges/github.png)](https://github.com/AngelicIndustries/Mutant-Creatures/releases)
![forge](images/badges/forge.png)
[![cord](images/badges/cord.png)](https://discord.gg/xAWCqGrguG)

## Features

- Mutant creepers, zombies, skeletons, endermen, snow golems, and spider pigs.
- Chemical X mutations and creature-specific drops, weapons, and armor.
- Configurable mutant spawning.

## Dependencies

Install Minecraft 1.7.10, Forge 10.13.4.1614, and [AnimationAPI 1.2.4](https://www.curseforge.com/minecraft/mc-mods/animationapi/files/2221721) on both client and server.

AnimationAPI is a separate required mod. Gradle resolves it from CurseMaven for development; its code is not bundled in this JAR.

## Building

```sh
./gradlew build
```

The GTNH buildscript provisions its Java toolchains. Output JARs target Java 8 and are written to `build/libs/`.
Versions come from Git; use `VERSION=1.1.1-dev ./gradlew build` to override the version locally.

For development, use `./gradlew runClient25` or `./gradlew runServer25`. The standard Angelic development mods are runtime-only and are not published as dependencies. Local game data is under `run/`.

Maven publishing targets the Angelic Maven. Modrinth, CurseForge, and 67minecraft publishing can be enabled by filling in their project IDs in `gradle.properties` and configuring the corresponding CI secrets.

## Credits

- thehippomaster21 — original Mutant Creatures and AnimationAPI.
- [astryxion](https://github.com/astryxion/Mutant-Creatures) and [Omgise](https://github.com/Omgise/Mutant-Creatures) — previous forks and fixes.
- jack / [Angelic Industries](https://angelic.industries/) — maintenance.
- Forge and MCP teams; [GTNewHorizons](https://github.com/GTNewHorizons/ExampleMod1.7.10) — modern build tooling.

## License

The inherited source tree does not include a license file. This build migration does not assign a new license to the original code or assets.
