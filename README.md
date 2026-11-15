# Cryonite — Minecraft 1.21.1 / NeoForge

NeoForge mod project targeting Java 21 and Minecraft 1.21.1. It introduces the Primordial Smelter and progression content for four post-Netherite tiers.

## Build locally

Requires a Java 21 JDK and Gradle 8.10.2 or newer:

```sh
gradle --no-daemon build
```

The GitHub Actions workflow builds on pushes, pull requests, and manual dispatch. Successful runs publish the compiled mod JAR as a downloadable `cryonite-<commit>` artifact for 30 days. Open **Actions → Build Minecraft mod → latest successful run → Artifacts** to download it.

## Build output and screenshots

Gradle outputs are ignored by Git (`build/`, `.gradle/`). Optional screenshots or visual previews belong in `docs/screenshots/`, outside Java/resource source sets; they are not inputs to the build and are not bundled into the mod JAR.

## Project status

The source currently contains the registry/bootstrap layer, item data components and attachments, ore and material identifiers, a five-slot Primordial Smelter with custom recipe input/serializer/menu/screen, authored recipes and worldgen resources, and English/Russian localization. This is an active implementation; see the current branch and workflow status before treating an artifact as a complete release.
