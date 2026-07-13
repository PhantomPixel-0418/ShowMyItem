package com.PhantomPixel0418.showmyitem;

import net.minecraft.item.ItemStack;
import java.util.UUID;

public class InventorySnapshot {
    public static final int MAIN_INVENTORY_SLOTS = 36;
    public static final int ARMOR_SLOTS = 4;
    public static final int OFFHAND_SLOT_INDEX = 40;

    private final UUID id;
    private final ItemStack[] items;
    private final ItemStack[] armor;
    private final ItemStack offhand;
    private final long timestamp;
    private final String playerName;
    private final UUID creatorUUID;

    public InventorySnapshot(ItemStack[] items, ItemStack[] armor, ItemStack offhand,
                             String playerName, UUID creatorUUID) {
        this.id = UUID.randomUUID();
        this.items = items;
        this.armor = armor;
        this.offhand = offhand;
        this.timestamp = System.currentTimeMillis();
        this.playerName = playerName;
        this.creatorUUID = creatorUUID;
    }

    public UUID getId() { return id; }
    public ItemStack[] getItems() { return items; }
    public ItemStack[] getArmor() { return armor; }
    public ItemStack getOffhand() { return offhand; }
    public long getTimestamp() { return timestamp; }
    public String getPlayerName() { return playerName; }
    public UUID getCreatorUUID() { return creatorUUID; }
}