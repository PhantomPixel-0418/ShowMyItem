package com.PhantomPixel0418.showmyitem;

import net.minecraft.item.ItemStack;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InventorySnapshotManager {
    private static final Map<UUID, InventorySnapshot> SNAPSHOTS = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<UUID> CREATION_ORDER = new ConcurrentLinkedQueue<>();

    public static UUID storeSnapshot(ItemStack[] inventory, ItemStack[] armor, ItemStack offhand,
                                     String playerName, UUID creatorUUID) {
        ModConfig config = ModConfig.getInstance();
        synchronized (SNAPSHOTS) {
            if (SNAPSHOTS.size() >= config.maxSnapshots) {
                // Find oldest snapshot using creation order queue
                UUID oldestId = null;
                for (UUID id : CREATION_ORDER) {
                    if (SNAPSHOTS.containsKey(id)) {
                        oldestId = id;
                        break;
                    }
                }
                if (oldestId != null) {
                    SNAPSHOTS.remove(oldestId);
                    CREATION_ORDER.remove(oldestId);
                }
            }
            InventorySnapshot snapshot = new InventorySnapshot(inventory, armor, offhand, playerName, creatorUUID);
            SNAPSHOTS.put(snapshot.getId(), snapshot);
            CREATION_ORDER.add(snapshot.getId());
            return snapshot.getId();
        }
    }

    public static UUID storeShulkerBoxSnapshot(ItemStack[] items, String playerName, UUID creatorUUID) {
        ModConfig config = ModConfig.getInstance();
        synchronized (SNAPSHOTS) {
            if (SNAPSHOTS.size() >= config.maxSnapshots) {
                UUID oldestId = null;
                for (UUID id : CREATION_ORDER) {
                    if (SNAPSHOTS.containsKey(id)) {
                        oldestId = id;
                        break;
                    }
                }
                if (oldestId != null) {
                    SNAPSHOTS.remove(oldestId);
                    CREATION_ORDER.remove(oldestId);
                }
            }
            InventorySnapshot snapshot = new InventorySnapshot(items, InventorySnapshot.Type.SHULKER_BOX, playerName, creatorUUID);
            SNAPSHOTS.put(snapshot.getId(), snapshot);
            CREATION_ORDER.add(snapshot.getId());
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
        SNAPSHOTS.entrySet().removeIf(entry -> {
            boolean expired = now - entry.getValue().getTimestamp() > config.snapshotExpiryMs;
            if (expired) {
                CREATION_ORDER.remove(entry.getKey());
            }
            return expired;
        });
    }
}