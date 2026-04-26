package com.PhantomPixel0418.showmyitem;

import net.minecraft.item.ItemStack;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InventorySnapshotManager {
    private static final Map<UUID, InventorySnapshot> SNAPSHOTS = new ConcurrentHashMap<>();

    public static UUID storeSnapshot(ItemStack[] inventory, String playerName, UUID creatorUUID) {
        ModConfig config = ModConfig.getInstance();
        synchronized (SNAPSHOTS) {
            if (SNAPSHOTS.size() >= config.maxSnapshots) {
                SNAPSHOTS.entrySet().stream()
                        .min(Map.Entry.comparingByValue((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp())))
                        .ifPresent(entry -> SNAPSHOTS.remove(entry.getKey()));
            }
            InventorySnapshot snapshot = new InventorySnapshot(inventory, playerName, creatorUUID);
            SNAPSHOTS.put(snapshot.getId(), snapshot);
            return snapshot.getId();
        }
    }

    public static ItemStack[] getSnapshot(UUID id) {
        InventorySnapshot snapshot = getSnapshotObject(id);
        return snapshot != null ? snapshot.getItems() : null;
    }

    public static InventorySnapshot getSnapshotObject(UUID id) {
        ModConfig config = ModConfig.getInstance();
        InventorySnapshot snapshot = SNAPSHOTS.get(id);
        if (snapshot == null) return null;
        if (System.currentTimeMillis() - snapshot.getTimestamp() > config.snapshotExpiryMs) {
            SNAPSHOTS.remove(id);
            return null;
        }
        return snapshot;
    }

    public static void cleanExpired() {
        ModConfig config = ModConfig.getInstance();
        long now = System.currentTimeMillis();
        SNAPSHOTS.entrySet().removeIf(entry ->
                now - entry.getValue().getTimestamp() > config.snapshotExpiryMs
        );
    }
}