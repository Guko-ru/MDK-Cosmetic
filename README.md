# MDK Cosmetic — Source Code

This repository contains the source code, game assets, and editable Blockbench models for the Minecraft mod **MDK Cosmetic**.

**[Read the complete license](LICENSE.md)**

MDK Cosmetic is free to play. Ordinary gameplay, free modpacks containing unmodified official files, and gameplay content creation (including monetized videos and streams) are permitted under the terms of the License. Commercial distribution of the mod itself and public distribution of modified versions require prior written permission from MDK.

**Version:** 1.18.78 · **Minecraft:** 1.21.9 · **Loader:** Fabric Loader 0.20.1 · **Fabric API:** 0.134.1+1.21.9.

The source code is publicly available under the custom **MDK Cosmetic Non-Commercial License 1.1**. Public source availability does not remove the restrictions in the License; this is not an open-source license.

## Build

Use **JDK 21**. On Windows, run `build_mod.bat` or `OPEN_BUILD_CONSOLE.cmd`. With Gradle available, run `gradle clean prepareRelease`. The release JAR is written to `build/release/` after a successful build.

The `src/` directory contains the mod code and runtime resources. The `blockbench/` directory contains editable model sources and is not included in the runtime JAR.

To request special or commercial permission, use the contact details published on the official MDK Cosmetic project page.

The two legacy vanilla spawn egg sprites retained for compatibility with the original icons are Minecraft artwork by Mojang/Microsoft; their rights remain with their owners.
