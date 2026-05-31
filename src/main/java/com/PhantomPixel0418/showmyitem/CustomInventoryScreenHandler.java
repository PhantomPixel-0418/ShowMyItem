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

        // 容器槽位（只读）
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                int index = row * 9 + col;
                if (index < invSize) {
                    this.addSlot(new ReadOnlySlot(inventory, index, 8 + col * 18, 18 + row * 18));
                } else {
                    this.addSlot(new ReadOnlySlot(new SimpleInventory(1), 0, 8 + col * 18, 18 + row * 18));
                }
            }
        }

        // 玩家物品栏
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
        return ItemStack.EMPTY;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        inventory.onClose(player);
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        ItemStack offhandBefore = player.getOffHandStack().copy();
        ItemStack cursorBefore = this.getCursorStack().copy();

        boolean shouldBlock = false;
        if (slotIndex >= 0 && slotIndex < this.slots.size()) {
            if (this.slots.get(slotIndex) instanceof ReadOnlySlot) {
                shouldBlock = true;
            }
        } else if (actionType == SlotActionType.SWAP) {
            shouldBlock = true;
        }

        if (shouldBlock) {
            // 不执行任何物品移动，恢复副手和光标，然后同步
            this.setCursorStack(cursorBefore);
            player.getInventory().offHand.set(0, offhandBefore);
            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.currentScreenHandler.sendContentUpdates();
                serverPlayer.networkHandler.sendPacket(
                        new EntityEquipmentUpdateS2CPacket(serverPlayer.getId(),
                                List.of(Pair.of(EquipmentSlot.OFFHAND, offhandBefore)))
                );
            }
            return;
        }

        super.onSlotClick(slotIndex, button, actionType, player);
    }

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
}