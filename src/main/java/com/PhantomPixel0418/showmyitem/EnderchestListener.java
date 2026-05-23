package com.PhantomPixel0418.showmyitem;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;

import java.util.UUID;

/** Minimal listener that intercepts ender chest block use and allows access when shared. */
public class EnderchestListener {
    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (hitResult instanceof BlockHitResult bhr) {
                if (world.getBlockState(bhr.getBlockPos()).isOf(Blocks.ENDER_CHEST)) {
                    UUID owner = player.getUuid();
                    // For initial version, do nothing special—real implementation would open owner's ender chest if shared.
                    // This placeholder prevents breaking existing behaviour.
                }
            }
            return ActionResult.PASS;
        });
    }
}
