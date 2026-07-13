package com.PhantomPixel0418.showmyitem;

import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class InventoryUtils {
    private InventoryUtils() {}

    /**
     * Copies an ender chest inventory into a new SimpleInventory.
     */
    public static SimpleInventory copyEnderChest(EnderChestInventory enderInv) {
        int size = enderInv.size();
        SimpleInventory tempInv = new SimpleInventory(size);
        for (int i = 0; i < size; i++) {
            tempInv.setStack(i, enderInv.getStack(i).copy());
        }
        return tempInv;
    }

    /**
     * Opens a custom read-only inventory screen for a player.
     */
    public static void openCustomInventoryScreen(
            ServerPlayerEntity player, Inventory targetInventory, Text title) {
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
            (syncId, playerInv, p) -> new CustomInventoryScreenHandler(syncId, playerInv, targetInventory),
            title
        ));
    }
}
