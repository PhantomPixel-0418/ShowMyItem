package com.PhantomPixel0418.showmyitem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class ReadOnlyInventory implements Inventory {
    private final ItemStack[] stacks;

    public ReadOnlyInventory(ItemStack[] stacks) {
        this.stacks = new ItemStack[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            this.stacks[i] = stacks[i].copy();
        }
    }

    @Override
    public int size() { return stacks.length; }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) { return stacks[slot]; }

    @Override
    public ItemStack removeStack(int slot, int amount) { return ItemStack.EMPTY; }

    @Override
    public ItemStack removeStack(int slot) { return ItemStack.EMPTY; }

    @Override
    public void setStack(int slot, ItemStack stack) {}

    @Override
    public void markDirty() {}

    @Override
    public boolean canPlayerUse(PlayerEntity player) { return true; }

    @Override
    public void clear() {}
}