GabousLibs — Multi‑loader Utility Library

Overview
- Reusable library mod with common utilities for Fabric and Forge.
- Built from a multi‑project Gradle setup (common, fabric, forge) using Architectury Loom.

Build & Publish Locally
- Build all: `./gradlew build`
- Publish to your local Maven: `./gradlew publishAllToMavenLocal`
- Alternatively, publish to a local folder: artifacts are also placed in `build/maven-repo`.

Coordinates
- Group: `net.Gabou`
- Artifacts:
  - Fabric: `net.Gabou:gaboulibs-fabric:${version}`
  - Forge:  `net.Gabou:gaboulibs-forge:${version}`

Using in Another Mod
1) Add a repository to resolve the dependency:
   - `mavenLocal()` (for local development)
   - Or `maven { url = uri("<path-to-gaboulibs>/build/maven-repo") }`

2) Add dependency (Architectury/Loom projects):
   - Fabric: `modApi("net.Gabou:gaboulibs-fabric:${version}")`
   - Forge:  `modApi("net.Gabou:gaboulibs-forge:${version}")`

3) Declare runtime dependency on the library in your mod metadata:
   - Fabric `fabric.mod.json` → under `depends`: `"gaboulibs": "*"`
   - Forge `mods.toml` → add a dependency block for `gaboulibs` with a version range, e.g. `[1.0,)`.

Composite Build (optional, great during development)
- In the consuming mod's `settings.gradle`: `includeBuild("../GabousLibs")`
- Keep the same coordinates as above; Gradle will substitute from the included build.

Notes
- The published jars are remapped and include sources.
- Each platform jar bundles the common module.
