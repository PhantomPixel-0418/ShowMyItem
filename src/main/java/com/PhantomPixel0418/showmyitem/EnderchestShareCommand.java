package com.PhantomPixel0418.showmyitem;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.UUID;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class EnderchestShareCommand {
    public static void register(com.mojang.brigadier.CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("showmyitem")
                .then(CommandManager.literal("enderchest")
                        .then(CommandManager.literal("invite")
                                .then(CommandManager.argument("player", StringArgumentType.string())
                                        .executes(ctx -> invite(ctx))))
                        .then(CommandManager.literal("revoke")
                                .then(CommandManager.argument("player", StringArgumentType.string())
                                        .executes(ctx -> revoke(ctx))))
                        .then(CommandManager.literal("list")
                                .executes(ctx -> list(ctx)))
                        .then(CommandManager.literal("open")
                                .then(CommandManager.argument("player", StringArgumentType.string())
                                        .executes(ctx -> open(ctx))))
                )
        );
    }

    private static int invite(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        String targetName = StringArgumentType.getString(ctx, "player");
        ServerPlayerEntity owner = src.getPlayer();
        try {
            ServerPlayerEntity target = src.getMinecraftServer().getPlayerManager().getPlayer(targetName);
            SharedEnderChestManager.getInstance().invite(owner.getUuid(), target.getUuid());
            src.sendFeedback(new LiteralText("Invited " + targetName), false);
        } catch (Exception ex) {
            src.sendError(new LiteralText("Player not found: " + targetName));
        }
        return 1;
    }

    private static int revoke(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        String targetName = StringArgumentType.getString(ctx, "player");
        ServerPlayerEntity owner = src.getPlayer();
        try {
            ServerPlayerEntity target = src.getMinecraftServer().getPlayerManager().getPlayer(targetName);
            SharedEnderChestManager.getInstance().revoke(owner.getUuid(), target.getUuid());
            src.sendFeedback(new LiteralText("Revoked " + targetName), false);
        } catch (Exception ex) {
            src.sendError(new LiteralText("Player not found: " + targetName));
        }
        return 1;
    }

    private static int list(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity owner = src.getPlayer();
        var members = SharedEnderChestManager.getInstance().listMembers(owner.getUuid());
        if (members.isEmpty()) {
            src.sendFeedback(new LiteralText("No members"), false);
        } else {
            StringBuilder sb = new StringBuilder();
            for (UUID u : members) sb.append(u.toString()).append(" ");
            src.sendFeedback(new LiteralText("Members: " + sb.toString()), false);
        }
        return 1;
    }

    private static int open(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        String targetName = StringArgumentType.getString(ctx, "player");
        ServerPlayerEntity opener = src.getPlayer();
        try {
            ServerPlayerEntity owner = src.getMinecraftServer().getPlayerManager().getPlayer(targetName);
            if (!SharedEnderChestManager.getInstance().isSharedWith(owner.getUuid(), opener.getUuid())) {
                src.sendError(new LiteralText("Not shared with you"));
                return 0;
            }
            // create a simple inventory and copy owner's ender chest contents
            var enderChest = owner.getEnderChestInventory();
            SimpleInventory temp = new SimpleInventory(enderChest.size());
            for (int i = 0; i < enderChest.size(); i++) {
                ItemStack s = enderChest.getStack(i).copy();
                temp.setStack(i, s);
            }
            // open custom screen handler
            opener.openHandledScreen(new SimpleNamedScreenFactoryWithInventory(temp, owner.getName().asString()));
            src.sendFeedback(new LiteralText("Opened enderchest of " + targetName), false);
        } catch (Exception ex) {
            src.sendError(new LiteralText("Player not found: " + targetName));
        }
        return 1;
    }
}

