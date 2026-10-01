# DEV PLAN — Flashback Inventory v0.2.0（GUI 还原 + 时序回放）

## 需求（用户确认）
1. **仅"旁观玩家第一人称"时显示**：Flashback 回放中进入某玩家第一人称（`cameraEntity instanceof Player` 且 `options.getCameraType().isFirstPerson()`），或旁观者模式进入实体第一人称。
2. **还原真实 MC GUI**：用 MC 26.3 内置纹理 `minecraft:textures/gui/container/*`（忽略旧资源包），居中渲染玩家当时打开的背包/容器/工作台界面（含槽位、物品、标题）。
3. **只在"打开界面时"显示**：录制时记录"玩家打开了哪个菜单"，回放对应时刻才渲染。
4. 一并解决：1.0 工作台配方（菜单槽位记录）、1.1 背包按时间回放、2.0 奖励箱。

## 核心设计：通用 Slot 坐标引擎
不硬编码每种菜单布局，而是**录制时从打开的 `AbstractContainerMenu` 提取每个 `Slot` 的 GUI 内绝对坐标 (x,y) + 内容**，回放时统一渲染：
- 任意菜单（背包/箱子/工作台/熔炉/酿造/附魔…）通用，后续只加"菜单类型→纹理"映射即可扩展。

## 数据层（新增 MenuSnapshot）
```
record MenuSnapshot(
  Target target,                  // PLAYER / CONTAINER
  ResourceKey<Level> dimension,
  UUID playerUuid,
  BlockPos blockPos,              // 容器时
  Identifier textureId,           // 如 minecraft:textures/gui/container/inventory.png
  String title,
  int menuWidth, int menuHeight,  // 窗口像素尺寸（如 176x166）
  int slotWidth, int slotHeight,  // 通常 18x18
  List<MenuSlot> slots)           // { int x, int y, ItemStack stack }
record MenuSlot(int x, int y, ItemStack stack)
```
- 新 Action `ActionMenuSnapshot` 写 .flashback；回放解码进 store。
- **按时间序列存储**：store 每个 key 存 `List<RecordedSnapshot>`（含 recordedTick），回放按当前进度取"最近不晚于当前 tick"的快照 → 解决 1.1 时序。
- 录制触发：每 tick 若 `minecraft.screen != null`（打开了界面）且为容器/背包 Menu，则记录当前 menu 快照（去重：内容/布局变化才写）。

## 渲染层（重写 ReplayInventoryOverlay）
- 触发：`isInReplay()` 且 cameraEntity instanceof Player 且第一人称。
- 居中：窗口宽高从记录中取，屏幕居中绘制背景 + 标题 + 槽位。
- 背景：`extractor.blit(textureId, ...)` 直接贴纹理（9 切片缩放或用 blitSprite）；槽位底 `fill`；物品 `item` + `itemDecorations`。
- 工作台配方（1.0）：crafting_menu 的槽位含合成格 3x3 + 结果，天然被 slot 坐标覆盖。

## 菜单类型 → 纹理映射（录制时写入 textureId）
- InventoryScreen → inventory.png
- ChestMenu/Generic → generic_54.png（按行数）
- CraftingMenu → crafting_table.png
- FurnaceMenu/SmokerMenu/BlastFurnaceMenu → furnace/smoker/blast_furnace.png
- BrewingStandMenu → brewing_stand.png, EnchantmentMenu → enchanting_table.png
- AnvilMenu → anvil.png, BeaconMenu → beacon.png, GrindstoneMenu → grindstone.png
- CartographyTableMenu → cartography_table.png, LoomMenu → loom.png
- SmithingMenu → smithing.png, StonecutterMenu → stonecutter.png
- DispenserMenu → dispenser.png, HopperMenu → hopper.png, ShulkerBoxMenu → shulker_box.png
- HorseInventoryMenu → horse.png, MerchantMenu → villager.png
（回放时通过记录的 textureId 直接贴图，无需回放端再推断菜单类型）

## 待核实 26.3 API
- AbstractContainerMenu: getType() / slots / Slot.x / Slot.y / title()
- Slot: x(), y(), getItem() 或 x/y 字段
- Minecraft.options.getCameraType().isFirstPerson()
- GuiGraphicsExtractor.blit(textureId, ...) 9 切片用法
- Flashback ReplayServer 当前 tick 来源（用于按时间取帧）

## 交付
- 构建 `gradlew.bat build` 通过，`build/libs/flashback-inventory-0.2.0.jar`
- README 更新
