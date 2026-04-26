package com.PhantomPixel0418.showmyitem;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import java.util.UUID;

public class ViewInventoryCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("showmyitem")
                .then(CommandManager.literal("viewinv")
                        .then(CommandManager.argument("snapshot", StringArgumentType.word())
                                .executes(context -> {
                                    String snapshotIdStr = StringArgumentType.getString(context, "snapshot");
                                    ServerCommandSource source = context.getSource();
                                    ServerPlayerEntity executor = source.getPlayer();
                                    if (executor == null) {
                                        source.sendError(Text.literal(
                                                I18n.translate("text.showmyitem.only_player")));
                                        return 0;
                                    }
                                    UUID snapshotId;
                                    try {
                                        snapshotId = UUID.fromString(snapshotIdStr);
                                    } catch (IllegalArgumentException e) {
                                        source.sendError(Text.literal(
                                                I18n.translate(executor, "text.showmyitem.invalid_snapshot")));
                                        return 0;
                                    }
                                    InventorySnapshot snapshot = InventorySnapshotManager.getSnapshotObject(snapshotId);
                                    if (snapshot == null) {
                                        long minutes = ModConfig.getInstance().snapshotExpiryMs / 60000;
                                        source.sendError(Text.literal(
                                                I18n.translate(executor, "text.showmyitem.snapshot_expired", minutes)));
                                        return 0;
                                    }

                                    // 权限检查：仅创建者或 OP
                                    boolean isCreator = executor.getUuid().equals(snapshot.getCreatorUUID());
                                    boolean isOp = executor.hasPermissionLevel(2);
                                    if (!isCreator && !isOp) {
                                        source.sendError(Text.literal(
                                                I18n.translate(executor, "text.showmyitem.not_creator")));
                                        return 0;
                                    }

                                    ItemStack[] items = snapshot.getItems();
                                    String playerName = snapshot.getPlayerName();
                                    ReadOnlyInventory inv = new ReadOnlyInventory(items);
                                    Text title = Text.literal(
                                            I18n.translate(executor, "text.showmyitem.inventory_title", playerName));
                                    executor.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                                            (syncId, playerInv, player) -> new ReadOnlyInventoryScreenHandler(
                                                    syncId, playerInv, inv, 4),
                                            title
                                    ));
                                    return 1;
                                })
                        )
                )
                .then(CommandManager.literal("reloadconfig")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            ServerPlayerEntity executor = source.getPlayer();
                            ModConfig.reload();
                            String msg = I18n.translate(executor, "text.showmyitem.config_reloaded");
                            source.sendFeedback(() -> Text.literal(msg), true);
                            return 1;
                        })
                )
        );
    }
}