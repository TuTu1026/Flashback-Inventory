# Flashback Inventory

A client-side **Flashback addon** for Minecraft **26.2 / Fabric** that records the player's
inventory, open container menus and crafting-table layouts, then **redraws the real Minecraft GUI**
in the centre of the screen during replay playback — but only while you are spectating a player's
first-person view and that player had an interface open.

## What it does

- **Records, per replay tick:**
  - the local player's **full inventory** (all 41 slots: 36 hotbar+main, 4 armour, offhand);
  - the contents of nearby **container block entities** (chests, barrels, furnaces, shulker boxes,
    reward chests, …) within a scan radius;
  - the exact **open menu** (inventory / chest / crafting table / furnace / brewing stand / …):
    its vanilla GUI texture, window size, title, and **every slot's on-screen position + item** —
    which is what makes crafting-table recipes replay in the correct order.
- **Redraws the interface during playback** with the vanilla GUI textures
  (`minecraft:textures/gui/container/*`), item icons and stack counts, centred on screen,
  only when:
  1. a replay is playing back, **and**
  2. the viewer is spectating a player's **first-person** view
     (`cameraEntity instanceof Player` + first-person camera), **and**
  3. that player had a menu open at the current replay tick.

## How it integrates with Flashback

Flashback is a packet-based recorder. Its built-in Simple Voice Chat integration is the reference
addon pattern this mod follows:

| Phase | Mechanism |
| --- | --- |
| **Recording** | A client-tick handler checks `Flashback.RECORDER.readyToWrite()`, snapshots changed player inventories, nearby containers and the currently open menu, and calls `RECORDER.submitCustomTask(...)`, writing `ActionInventorySnapshot` / `ActionMenuSnapshot` (custom `com.moulberry.flashback.action.Action`s) into the `.flashback` file, each tagged with the recording tick. |
| **Replay** | The actions are registered in `ActionRegistry`. While Flashback's `ReplayServer` applies replay data it calls `handle(...)`, decoding each snapshot into the shared `ReplayInventoryStore` keyed by `getReplayTick()`. |
| **Rendering** | A Fabric `HudElement` reads the menu snapshot whose recorded tick is the latest one not after the current replay tick, and draws the redrawn GUI only when the first-person-spectate condition above holds. |

No Minecraft or Flashback classes are modified — the addon only uses Flashback's public addon
surface plus two small Fabric mixins (`HudMixin` for the overlay, `MouseWheelMixin` to record the
scroll wheel). No access widener.

## Project layout

```
src/main/java/com/flashbackinventory/
├── FlashbackInventory.java                entrypoints + registration
├── action/ActionInventorySnapshot.java    custom Flashback replay Action (player/container)
├── action/ActionMenuSnapshot.java         custom Flashback replay Action (open menu GUI)
├── record/InventorySnapshot.java          player/container payload + stream codec
├── record/MenuSnapshot.java               menu GUI payload (texture + slots) + stream codec
├── record/InventoryRecorder.java          recording capture logic (player/container/menu)
├── store/ReplayInventoryStore.java        time-series snapshot registry (per replay tick)
└── render/ReplayInventoryOverlay.java     in-replay redrawn GUI overlay
src/main/resources/fabric.mod.json
```

## Requirements

- **JDK 25** — Minecraft 26.2 compiles/runs on Java 25. `gradle.properties` pins
  `org.gradle.java.home` to a local JDK 25 install; **before building, change that line to the
  path of your own JDK 25** (or remove it and make sure Gradle uses a JDK 25).
- **Gradle 9.5.1** (wrapper) — required for Loom 1.17.
- **Loom 1.17-SNAPSHOT** (`net.fabricmc.fabric-loom`). Minecraft 26.2 ships with Mojang-mapped
  (de-obfuscated) class names, so **no `mappings` line and no remap-to-intermediary step is
  needed** — the built jar is already the production artifact.
- **Fabric Loader 0.19.5+, Fabric API 0.161.0+26.2**
- **Flashback 0.43.6+** for MC 26.2. The 192 MB jar is **not** fetched from the Modrinth maven
  (too slow); it is a local `compileOnly` file dependency. Download
  `Flashback-0.43.6-for-MC26.2.jar` and place it at `libs/Flashback-0.43.6-for-MC26.2.jar`
  before building.
- **Mod Menu 20.0.3** (optional, for the in-game settings screen; compile-only).

## Building

```bash
gradlew.bat build
```

First make sure the Flashback jar is in `libs/` (see Requirements). The compiled mod lands in
`build/libs/flashback-inventory-<version>.jar`. Drop it (together with Fabric API and Flashback)
into your `.minecraft/mods` folder.

## Usage

1. Start recording with Flashback as usual.
2. Play the recording back in Flashback, and **spectate a player** (switch to their first-person view).
3. Whenever that player had opened an inventory/container/table, the vanilla GUI is redrawn in the
   centre of your screen for as long as it was open during recording.
4. Press the **Flashback Inventory → Toggle replay inventory overlay** keybind to hide/show the
   redrawn GUI.

## Verification

`gradlew.bat build` succeeds against Minecraft 26.2 with Fabric Loader 0.19.5 / Fabric API
0.161.0+26.2 / Flashback 0.43.6, and produces `build/libs/flashback-inventory-1.0.3.jar`.
