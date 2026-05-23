package com.PhantomPixel0418.showmyitem;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class EnderchestShareCommand {
    public static void register(CommandManager.RegistrationEnvironment env, CommandManager dispatcher, CommandManager.RegistrationEnvironment environment) {
        // left empty to satisfy method reference compatibility
    }

    public static void register(CommandRegistrationCallback.RegistrationCallback callback) {
        // placeholder
    }

    // Using direct registration method used by Fabric (lambda style) will be registered in Showmyitem
    public static void register(net.fabricmc.fabric.api.command.v2.CommandDispatcher dispatcher) {
        // not used
    }

    public static void register(CommandManager.RegistrationEnvironment env) {
        // not used
    }

    // Simpler: provide a method used from Showmyitem to register commands
    public static void registerCommands(com.mojang.brigadier.CommandDispatcher<com.mojang.brigadier.context.CommandContext<ServerCommandSource>> dispatcher) {
        dispatcher.register(CommandManager.literal("enderchest")
                .then(CommandManager.literal("share")
                        .then(CommandManager.literal("invite")
                                .then(CommandManager.argument("player", StringArgumentType.string())
                                        .executes(ctx -> {
                                            ServerCommandSource src = ctx.getSource();
                                            String targetName = StringArgumentType.getString(ctx, "player");
                                            UUID owner = src.getPlayer().getUuid();
                                            try {
                                                UUID target = src.getMinecraftServer().getPlayerManager().getPlayer(targetName).getUuid();
                                                SharedEnderChestManager.getInstance().invite(owner, target);
                                                src.sendFeedback(new LiteralText("Invited " + targetName), false);
                                            } catch (Exception ex) {
                                                src.sendError(new LiteralText("Player not found: " + targetName));
                                            }
                                            return 1;
                                        })
                                )
                        )
                        .then(CommandManager.literal("revoke")
                                .then(CommandManager.argument("player", StringArgumentType.string())
                                        .executes(ctx -> {
                                            ServerCommandSource src = ctx.getSource();
                                            String targetName = StringArgumentType.getString(ctx, "player");
                                            UUID owner = src.getPlayer().getUuid();
                                            try {
                                                UUID target = src.getMinecraftServer().getPlayerManager().getPlayer(targetName).getUuid();
                                                SharedEnderChestManager.getInstance().revoke(owner, target);
                                                src.sendFeedback(new LiteralText("Revoked " + targetName), false);
                                            } catch (Exception ex) {
                                                src.sendError(new LiteralText("Player not found: " + targetName));
                                            }
                                            return 1;
                                        })
                                )
                        )
                        .then(CommandManager.literal("list")
                                .executes(ctx -> {
                                    ServerCommandSource src = ctx.getSource();
                                    UUID owner = src.getPlayer().getUuid();
                                    var members = SharedEnderChestManager.getInstance().listMembers(owner);
                                    if (members.isEmpty()) {
                                        src.sendFeedback(new LiteralText("No members"), false);
                                    } else {
                                        StringBuilder sb = new StringBuilder();
                                        for (UUID u : members) sb.append(u.toString()).append(" ");
                                        src.sendFeedback(new LiteralText("Members: " + sb.toString()), false);
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }
}
