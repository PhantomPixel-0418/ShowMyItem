package com.PhantomPixel0418.showmyitem;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public class CustomInventoryScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public CustomInventoryScreenHandler(int syncId, PlayerInventory playerInventory, Inventory targetInventory) {
        super(getScreenHandlerType(targetInventory.size()), syncId);
        this.inventory = targetInventory;
        inventory.onOpen(playerInventory.player);

        int invSize = inventory.size();
        int rows = (invSize + 8) / 9;

        // Container slots (read-only)
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                int index = row * 9 + col;
                if (index < invSize) {
                    this.addSlot(new ReadOnlySlot(inventory, index, 8 + col * 18, 18 + row * 18));
                } else {
                    // Use EmptyInventory singleton to avoid sharing mutable instances
                    this.addSlot(new ReadOnlySlot(EmptyInventory.INSTANCE, 0, 8 + col * 18, 18 + row * 18));
                }
            }
        }

        // Player inventory
        int playerInvY = 18 + rows * 18 + 4;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }
        int hotbarY = playerInvY + 58;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, hotbarY));
        }
    }

    private static ScreenHandlerType<?> getScreenHandlerType(int size) {
        if (size <= 9) return ScreenHandlerType.GENERIC_9X1;
        else if (size <= 18) return ScreenHandlerType.GENERIC_9X2;
        else if (size <= 27) return ScreenHandlerType.GENERIC_9X3;
        else if (size <= 36) return ScreenHandlerType.GENERIC_9X4;
        else if (size <= 45) return ScreenHandlerType.GENERIC_9X5;
        else return ScreenHandlerType.GENERIC_9X6;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        // Completely disable shift-click moving
        return ItemStack.EMPTY;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        inventory.onClose(player);
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        // Determine if the action should be blocked
        boolean block = false;

        // Block if clicking on a read-only slot
        if (slotIndex >= 0 && slotIndex < this.slots.size()) {
            Slot slot = this.slots.get(slotIndex);
            if (slot instanceof ReadOnlySlot) {
                block = true;
            }
        }

        // Also block SWAP action (number keys) because it can target read-only slots
        if (actionType == SlotActionType.SWAP) {
            block = true;
        }

        if (block) {
            // Save current cursor and offhand before any changes
            ItemStack cursorBefore = this.getCursorStack().copy();
            ItemStack offhandBefore = player.getOffHandStack().copy();

            // Do NOT call super; no action occurs

            // Restore client side state
            this.setCursorStack(cursorBefore);
            player.getInventory().offHand.set(0, offhandBefore);

            if (player instanceof ServerPlayerEntity serverPlayer) {
                // Send offhand equipment update to client
                Pair<EquipmentSlot, ItemStack> offhandPair = Pair.of(EquipmentSlot.OFFHAND, offhandBefore);
                serverPlayer.networkHandler.sendPacket(
                        new EntityEquipmentUpdateS2CPacket(serverPlayer.getId(), List.of(offhandPair))
                );
                // Also resend the entire screen contents (including cursor stack)
                serverPlayer.currentScreenHandler.sendContentUpdates();
            }
            return;
        }

        // Allow normal behavior for player inventory slots
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    // ---------- Inner Classes ----------

    private static class ReadOnlySlot extends Slot {
        public ReadOnlySlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return false;
        }
    }

    /**
     * A singleton empty inventory used for dummy slots to avoid sharing mutable instances.
     * All methods are no-ops or return empty stacks.
     */
    private static class EmptyInventory implements Inventory {
        public static final EmptyInventory INSTANCE = new EmptyInventory();

        private EmptyInventory() {}

        @Override
        public int size() { return 1; }

        @Override
        public boolean isEmpty() { return true; }

        @Override
        public ItemStack getStack(int slot) { return ItemStack.EMPTY; }

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
}