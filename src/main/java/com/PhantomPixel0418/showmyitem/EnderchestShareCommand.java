package com.PhantomPixel0418.showmyitem;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EnderchestShareCommand {
    private static final SharedEnderChestManager MANAGER = SharedEnderChestManager.getInstance();

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

                                                    UUID targetId = getPlayerUuid(src, targetName);
                                                    if (targetId == null) {
                                                        src.sendError(Text.literal("Player not found: " + targetName));
                                                        return 0;
                                                    }

                                                    MANAGER.invite(inviter.getUuid(), targetId);
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

                                                    UUID targetId = getPlayerUuid(src, targetName);
                                                    if (targetId == null) {
                                                        src.sendError(Text.literal("Player not found: " + targetName));
                                                        return 0;
                                                    }

                                                    MANAGER.revoke(inviter.getUuid(), targetId);
                                                    src.sendFeedback(() -> Text.literal("Revoked access for " + targetName), false);
                                                    return 1;
                                                })
                                        )
                                )
                                .then(CommandManager.literal("list")
                                        .executes(ctx -> {
                                            ServerCommandSource src = ctx.getSource();
                                            ServerPlayerEntity inviter = src.getPlayerOrThrow();
                                            var members = MANAGER.listMembers(inviter.getUuid());

                                            if (members.isEmpty()) {
                                                src.sendFeedback(() -> Text.literal("No members"), false);
                                                return 1;
                                            }

                                            List<String> memberNames = new ArrayList<>();
                                            for (UUID id : members) {
                                                ServerPlayerEntity online = src.getServer().getPlayerManager().getPlayer(id);
                                                if (online != null) {
                                                    memberNames.add(online.getName().getString());
                                                } else {
                                                    // Offline: show short UUID + offline indicator
                                                    String shortId = id.toString().substring(0, 8);
                                                    memberNames.add(shortId + " (offline)");
                                                }
                                            }
                                            String message = "Members: " + String.join(", ", memberNames);
                                            src.sendFeedback(() -> Text.literal(message), false);
                                            return 1;
                                        })
                                )
                                .then(CommandManager.literal("open")
                                        .then(CommandManager.argument("player", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    ServerCommandSource src = ctx.getSource();
                                                    ServerPlayerEntity opener = src.getPlayerOrThrow();
                                                    String targetName = StringArgumentType.getString(ctx, "player");

                                                    UUID ownerId = getPlayerUuid(src, targetName);
                                                    if (ownerId == null) {
                                                        src.sendError(Text.literal("Player not found: " + targetName));
                                                        return 0;
                                                    }

                                                    if (!opener.getUuid().equals(ownerId) && !MANAGER.isSharedWith(ownerId, opener.getUuid())) {
                                                        src.sendError(Text.literal("You don't have permission to view this ender chest"));
                                                        return 0;
                                                    }

                                                    ServerPlayerEntity owner = src.getServer().getPlayerManager().getPlayer(ownerId);
                                                    if (owner == null) {
                                                        src.sendError(Text.literal("The target player is offline, cannot open their ender chest"));
                                                        return 0;
                                                    }

                                                    SimpleInventory tempInv = InventoryUtils.copyEnderChest(owner.getEnderChestInventory());
                                                    Text title = Text.literal(owner.getName().getString() + "'s Ender Chest");
                                                    InventoryUtils.openCustomInventoryScreen(opener, tempInv, title);
                                                    src.sendFeedback(() -> Text.literal("Opened ender chest of " + targetName), false);
                                                    return 1;
                                                })
                                        )
                                )
                        )
        );
    }

    private static UUID getPlayerUuid(ServerCommandSource src, String name) {
        ServerPlayerEntity online = src.getServer().getPlayerManager().getPlayer(name);
        if (online != null) return online.getUuid();

        Optional<GameProfile> offlineOpt = src.getServer().getUserCache().findByName(name);
        if (offlineOpt.isPresent()) {
            GameProfile profile = offlineOpt.get();
            if (profile.getId() != null) {
                return profile.getId();
            }
        }
        return null;
    }
}