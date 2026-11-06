# MDK Cosmetic — Forge 1.21.10

**Project version:** 1.18.79  
**Forge target and minimum required version:** 60.1.18  
**Minecraft:** 1.21.10  
**Java:** JDK 21

MDK Cosmetic adds collectible cosmetics, a multiblock Collector progression system, equipment, backpacks, bosses, custom structures and the VOID dimension.

## Build

On Windows, extract the entire ZIP and run `build_mod.bat`. The console remains open and writes `BUILD_LOG.txt` upon running. Gradle 9.8.2 is downloaded automatically if it is not installed. The initial build requires Internet access.

Alternatively, with Gradle installed, run:

```sh
gradle --no-daemon clean prepareRelease
```

The release-ready mod JAR is copied to `build/release/` on a successful build. The `blockbench/` folder holds editable development assets and is **not packed into the JAR**.

## Compatibility

Keep the existing registry IDs intact when upgrading existing worlds; in particular, the Collector's Multitool still uses the historical ID `mdkcosmetic:collector_pickaxe` to preserve saved inventories and references. This project specifically targets Forge 60.1.18. Use only the Minecraft and Forge versions listed above.

## License

See `LICENSE.md` for the **MDK Cosmetic Non-Commercial License 1.1**. Commercial exploitation of covered MDK Cosmetic materials requires prior written permission, subject to the express exceptions in that license. Applicable third-party component licenses remain unaffected.

## Testing status

Built and tested with the native Forge client and server. 9 required GameTests passed; 1208 item models loaded. Client checks covered two machine tiers, four window/GUI configurations, inventory and memory slot clicks, both creative tabs, and both boss phases.
