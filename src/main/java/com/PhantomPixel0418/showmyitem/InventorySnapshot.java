package com.PhantomPixel0418.showmyitem;

import net.minecraft.item.ItemStack;
import java.util.UUID;

public class InventorySnapshot {
    private final UUID id;
    private final ItemStack[] items;
    private final long timestamp;
    private final String playerName;
    private final UUID creatorUUID;  // 新增：快照创建者

    public InventorySnapshot(ItemStack[] items, String playerName, UUID creatorUUID) {
        this.id = UUID.randomUUID();
        this.items = items;
        this.timestamp = System.currentTimeMillis();
        this.playerName = playerName;
        this.creatorUUID = creatorUUID;
    }

    public UUID getId() { return id; }
    public ItemStack[] getItems() { return items; }
    public long getTimestamp() { return timestamp; }
    public String getPlayerName() { return playerName; }
    public UUID getCreatorUUID() { return creatorUUID; }
}