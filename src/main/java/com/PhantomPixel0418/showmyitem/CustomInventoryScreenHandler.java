package com.PhantomPixel0418.showmyitem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public class CustomInventoryScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public CustomInventoryScreenHandler(int syncId, PlayerInventory playerInventory,
                                        Inventory targetInventory) {
        super(ScreenHandlerType.GENERIC_9X5, syncId); // 9行x5行容器
        this.inventory = targetInventory;
        inventory.onOpen(playerInventory.player);

        // 容器槽位 0..44（共45格）
        int slotIndex = 0;
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                // 只添加实际存在的物品（前41格），多余4格也添加空槽位
                if (slotIndex < inventory.size()) {
                    this.addSlot(new ReadOnlySlot(inventory, slotIndex, 8 + col * 18, 18 + row * 18));
                } else {
                    // 多余槽位，放置一个永远为空的只读槽位（使用虚拟库存）
                    this.addSlot(new ReadOnlySlot(new SimpleInventory(1), 0, 8 + col * 18, 18 + row * 18));
                }
                slotIndex++;
            }
        }

        // 玩家自身物品栏（由玩家背包自动提供）
        int playerInvY = 112;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }
        int hotbarY = 170;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, hotbarY));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY; // 禁止快速移动
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.inventory.onClose(player);
    }

    // 只读槽位
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