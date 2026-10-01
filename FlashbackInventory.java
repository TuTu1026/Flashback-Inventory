package com.flashbackinventory;

import com.flashbackinventory.action.ActionInventorySnapshot;
import com.flashbackinventory.action.ActionKeySnapshot;
import com.flashbackinventory.action.ActionMenuSnapshot;
import com.flashbackinventory.action.ActionScreenSnapshot;
import com.flashbackinventory.action.ActionDeathSnapshot;
import com.flashbackinventory.config.FlashbackInventoryConfig;
import com.flashbackinventory.record.InventoryRecorder;
import com.flashbackinventory.record.InventorySnapshot;
import com.flashbackinventory.record.MenuSnapshot;
import com.flashbackinventory.render.ReplayInventoryOverlay;
import com.moulberry.flashback.action.ActionRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlashbackInventory implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "flashback_inventory";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static boolean lastToggleDown = false;

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        // Register our inventory snapshot as a clientbound custom payload.
        // It is written into the replay file via our custom Action and (optionally)
        // delivered to replay viewers while playing back.
        PayloadTypeRegistry.clientboundPlay().register(InventorySnapshot.TYPE, InventorySnapshot.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(MenuSnapshot.TYPE, MenuSnapshot.STREAM_CODEC);
        LOGGER.info("[Flashback Inventory] registered payloads {}", InventorySnapshot.TYPE.id());
    }

    @Override
    public void onInitializeClient() {
        // Register the custom replay Actions so Flashback's ReplayServer can apply
        // our inventory/menu snapshots when the recorded replay is played back.
        ActionRegistry.register(ActionInventorySnapshot.INSTANCE);
        ActionRegistry.register(ActionMenuSnapshot.INSTANCE);
        ActionRegistry.register(ActionScreenSnapshot.INSTANCE);
        ActionRegistry.register(ActionKeySnapshot.INSTANCE);
        ActionRegistry.register(ActionDeathSnapshot.INSTANCE);

        // Register the in-replay overlay HUD.
        HudElementRegistry.addFirst(ReplayInventoryOverlay.ID, new ReplayInventoryOverlay());

        // Record player inventory / container data while Flashback is recording.
        ClientTickEvents.END_CLIENT_TICK.register(client -> InventoryRecorder.onClientTick());

        // Overlay toggle key: configured in the settings screen (not the vanilla keybind list).
        // We poll the SDL keyboard state ourselves and toggle on the press edge.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean down = isScanDown(FlashbackInventoryConfig.overlayToggleScancode);
            if (down && !lastToggleDown) {
                ReplayInventoryOverlay.enabled = !ReplayInventoryOverlay.enabled;
            }
            lastToggleDown = down;
        });

        LOGGER.info("[Flashback Inventory] initialized");
    }

    /** Whether the given GLFW key code is currently held down. */
    public static boolean isScanDown(int sc) {
        return com.flashbackinventory.util.KeyboardUtil.isKeyDown(sc);
    }
}
