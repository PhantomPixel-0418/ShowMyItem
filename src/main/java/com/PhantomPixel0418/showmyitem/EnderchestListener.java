package com.PhantomPixel0418.showmyitem;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;

import java.util.UUID;

public class EnderchestListener {
    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (hitResult instanceof BlockHitResult bhr) {
                if (world.getBlockState(bhr.getBlockPos()).isOf(Blocks.ENDER_CHEST)) {
                    SharedEnderChestManager manager = SharedEnderChestManager.getInstance();
                    UUID playerId = player.getUuid();
                    UUID inviterId = manager.findInviter(playerId);
                    if (inviterId != null) {
                        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                        ServerPlayerEntity inviter = serverPlayer.getServer().getPlayerManager().getPlayer(inviterId);
                        if (inviter != null) {
                            openEnderChestForPlayer(serverPlayer, inviter);
                        } else {
                            serverPlayer.sendMessage(Text.literal("The player who invited you is offline. Cannot open their ender chest."), false);
                        }
                        return ActionResult.SUCCESS;
                    }
                }
            }
            return ActionResult.PASS;
        });
    }

    private static void openEnderChestForPlayer(ServerPlayerEntity viewer, ServerPlayerEntity owner) {
        EnderChestInventory enderInv = owner.getEnderChestInventory();
        int size = enderInv.size();
        SimpleInventory tempInv = new SimpleInventory(size);
        for (int i = 0; i < size; i++) {
            tempInv.setStack(i, enderInv.getStack(i).copy());
        }
        Text title = Text.literal(owner.getName().getString() + "'s Ender Chest");
        viewer.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInv, player) -> new CustomInventoryScreenHandler(syncId, playerInv, tempInv),
                title
        ));
    }
}