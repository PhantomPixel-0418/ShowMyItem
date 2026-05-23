package com.PhantomPixel0418.showmyitem;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.screen.ScreenHandler;

public class SimpleNamedScreenFactoryWithInventory implements NamedScreenHandlerFactory {
    private final Inventory inventory;
    private final String title;

    public SimpleNamedScreenFactoryWithInventory(Inventory inventory, String title) {
        this.inventory = inventory;
        this.title = title;
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Ender Chest - " + title);
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, ServerPlayerEntity player) {
        return new CustomInventoryScreenHandler(syncId, playerInventory, inventory);
    }
}
