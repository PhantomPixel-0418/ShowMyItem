package com.PhantomPixel0418.showmyitem;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.*;

public class EnderchestShareCommand {
    private static final Map<UUID, Set<UUID>> SHARES = new HashMap<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("showmyitem")
                        .then(CommandManager.literal("enderchest")
                                .then(CommandManager.literal("invite")
                                        .then(CommandManager.argument("player", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    ServerCommandSource src = ctx.getSource();
                                                    ServerPlayerEntity inviter = src.getPlayerOrThrow();
                                                    String targetName = StringArgumentType.getString(ctx, "player");
                                                    ServerPlayerEntity target = src.getServer().getPlayerManager().getPlayer(targetName);
                                                    if (target == null) {
                                                        src.sendError(Text.literal("Player not found: " + targetName));
                                                        return 0;
                                                    }
                                                    SHARES.computeIfAbsent(inviter.getUuid(), k -> new HashSet<>()).add(target.getUuid());
                                                    src.sendFeedback(() -> Text.literal("Invited " + targetName + " to view your ender chest"), false);
                                                    return 1;
                                                })
                                        )
                                )
                                .then(CommandManager.literal("revoke")
                                        .then(CommandManager.argument("player", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    ServerCommandSource src = ctx.getSource();
                                                    ServerPlayerEntity inviter = src.getPlayerOrThrow();
                                                    String targetName = StringArgumentType.getString(ctx, "player");
                                                    ServerPlayerEntity target = src.getServer().getPlayerManager().getPlayer(targetName);
                                                    if (target == null) {
                                                        src.sendError(Text.literal("Player not found: " + targetName));
                                                        return 0;
                                                    }
                                                    Set<UUID> set = SHARES.get(inviter.getUuid());
                                                    if (set != null) {
                                                        set.remove(target.getUuid());
                                                        if (set.isEmpty()) SHARES.remove(inviter.getUuid());
                                                    }
                                                    src.sendFeedback(() -> Text.literal("Revoked access for " + targetName), false);
                                                    return 1;
                                                })
                                        )
                                )
                                .then(CommandManager.literal("list")
                                        .executes(ctx -> {
                                            ServerCommandSource src = ctx.getSource();
                                            ServerPlayerEntity inviter = src.getPlayerOrThrow();
                                            Set<UUID> set = SHARES.get(inviter.getUuid());
                                            if (set == null || set.isEmpty()) {
                                                src.sendFeedback(() -> Text.literal("No members"), false);
                                            } else {
                                                StringBuilder sb = new StringBuilder();
                                                for (UUID id : set) {
                                                    ServerPlayerEntity p = src.getServer().getPlayerManager().getPlayer(id);
                                                    sb.append(p != null ? p.getName().getString() : id.toString()).append(", ");
                                                }
                                                sb.setLength(sb.length() - 2);
                                                src.sendFeedback(() -> Text.literal("Members: " + sb.toString()), false);
                                            }
                                            return 1;
                                        })
                                )
                                .then(CommandManager.literal("open")
                                        .then(CommandManager.argument("player", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    ServerCommandSource src = ctx.getSource();
                                                    ServerPlayerEntity opener = src.getPlayerOrThrow();
                                                    String targetName = StringArgumentType.getString(ctx, "player");
                                                    ServerPlayerEntity owner = src.getServer().getPlayerManager().getPlayer(targetName);
                                                    if (owner == null) {
                                                        src.sendError(Text.literal("Player not found: " + targetName));
                                                        return 0;
                                                    }
                                                    if (!opener.getUuid().equals(owner.getUuid())) {
                                                        Set<UUID> allowed = SHARES.get(owner.getUuid());
                                                        if (allowed == null || !allowed.contains(opener.getUuid())) {
                                                            src.sendError(Text.literal("You don't have permission to view this ender chest"));
                                                            return 0;
                                                        }
                                                    }
                                                    EnderChestInventory enderInv = owner.getEnderChestInventory();
                                                    int size = enderInv.size();
                                                    SimpleInventory tempInv = new SimpleInventory(size);
                                                    for (int i = 0; i < size; i++) {
                                                        tempInv.setStack(i, enderInv.getStack(i).copy());
                                                    }
                                                    Text title = Text.literal(owner.getName().getString() + " 的末影箱");
                                                    opener.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                                                            (syncId, playerInv, player) -> new CustomInventoryScreenHandler(syncId, playerInv, tempInv),
                                                            title
                                                    ));
                                                    src.sendFeedback(() -> Text.literal("Opened ender chest of " + targetName), false);
                                                    return 1;
                                                })
                                        )
                                )
                        )
        );
    }
}