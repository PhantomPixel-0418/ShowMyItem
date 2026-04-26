package com.PhantomPixel0418.showmyitem;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;                     // 新增导入
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import java.util.UUID;

public class ViewInventoryCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        @SuppressWarnings("unused")                                 // 抑制未使用警告
        var unused = new Object() { Object x = registryAccess; Object y = environment; }; // 或直接用注解

        dispatcher.register(CommandManager.literal("showmyitem")
                .executes(ViewInventoryCommand::showMainMenu)
                .then(CommandManager.literal("viewinv")
                        .then(CommandManager.argument("snapshot", StringArgumentType.word())
                                .executes(ViewInventoryCommand::viewInventory)
                        )
                )
                .then(CommandManager.literal("reloadconfig")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(ViewInventoryCommand::reloadConfig)
                )
                .then(CommandManager.literal("set")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("key", StringArgumentType.word())
                                .then(CommandManager.argument("value", StringArgumentType.greedyString())
                                        .executes(ViewInventoryCommand::setConfig)
                                )
                        )
                )
        );
    }

    private static int viewInventory(CommandContext<ServerCommandSource> context) {
        String snapshotIdStr = StringArgumentType.getString(context, "snapshot");
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal(I18n.translate("text.showmyitem.only_player")));
            return 0;
        }
        UUID snapshotId;
        try {
            snapshotId = UUID.fromString(snapshotIdStr);
        } catch (IllegalArgumentException e) {
            source.sendError(Text.literal(I18n.translate(player, "text.showmyitem.invalid_snapshot")));
            return 0;
        }
        InventorySnapshot snapshot = InventorySnapshotManager.getSnapshotObject(snapshotId);
        if (snapshot == null) {
            long minutes = ModConfig.getInstance().snapshotExpiryMs / 60000;
            source.sendError(Text.literal(I18n.translate(player, "text.showmyitem.snapshot_expired", minutes)));
            return 0;
        }
        if (!player.getUuid().equals(snapshot.getCreatorUUID()) && !player.hasPermissionLevel(2)) {
            source.sendError(Text.literal(I18n.translate(player, "text.showmyitem.not_creator")));
            return 0;
        }
        ItemStack[] items = snapshot.getItems();
        String ownerName = snapshot.getPlayerName();
        ReadOnlyInventory inv = new ReadOnlyInventory(items);
        Text title = Text.literal(I18n.translate(player, "text.showmyitem.inventory_title", ownerName));
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInv, p) -> new ReadOnlyInventoryScreenHandler(syncId, playerInv, inv, 4),
                title
        ));
        return 1;
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        ModConfig.reload();
        String msg = I18n.translate(player, "text.showmyitem.config_reloaded");
        source.sendFeedback(() -> Text.literal(msg), true);
        return 1;
    }

    private static int setConfig(CommandContext<ServerCommandSource> context) {
        String key = StringArgumentType.getString(context, "key");
        String valueStr = StringArgumentType.getString(context, "value").trim();
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        ModConfig config = ModConfig.getInstance();
        try {
            switch (key) {
                case "snapshotExpiryMs" -> {
                    long ms = Long.parseLong(valueStr);
                    if (ms < 1000) throw new NumberFormatException("too small");
                    config.snapshotExpiryMs = ms;
                }
                case "maxSnapshots" -> {
                    int max = Integer.parseInt(valueStr);
                    if (max < 1) throw new NumberFormatException("too small");
                    config.maxSnapshots = max;
                }
                case "defaultLanguage" -> {
                    if (!valueStr.equals("en_us") && !valueStr.equals("zh_cn")) {
                        source.sendError(Text.literal(I18n.translate(player, "text.showmyitem.invalid_language", valueStr)));
                        return 0;
                    }
                    config.defaultLanguage = valueStr;
                }
                default -> {
                    source.sendError(Text.literal(I18n.translate(player, "text.showmyitem.unknown_key", key)));
                    return 0;
                }
            }
            ModConfig.save();
            ModConfig.reload();
            String msg = I18n.translate(player, "text.showmyitem.set_success", key, valueStr);
            source.sendFeedback(() -> Text.literal(msg), true);
            return 1;
        } catch (NumberFormatException e) {
            source.sendError(Text.literal(I18n.translate(player, "text.showmyitem.invalid_value", valueStr)));
            return 0;
        }
    }

    private static int showMainMenu(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        ModConfig config = ModConfig.getInstance();

        MutableText menu = Text.literal(I18n.translate(player, "text.showmyitem.config_menu_title"))
                .formatted(Formatting.GOLD, Formatting.BOLD);
        menu.append("\n");

        MutableText expiryLine = Text.literal("snapshotExpiryMs: " + config.snapshotExpiryMs + " ms")
                .formatted(Formatting.YELLOW);
        expiryLine.append(
                Text.literal(" [✎]").formatted(Formatting.AQUA)
                        .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                "/showmyitem set snapshotExpiryMs ")))
        );
        menu.append(expiryLine).append("\n");

        MutableText maxLine = Text.literal("maxSnapshots: " + config.maxSnapshots)
                .formatted(Formatting.YELLOW);
        maxLine.append(
                Text.literal(" [✎]").formatted(Formatting.AQUA)
                        .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                "/showmyitem set maxSnapshots ")))
        );
        menu.append(maxLine).append("\n");

        String currentLang = config.defaultLanguage;
        String toggleLang = currentLang.equals("en_us") ? "zh_cn" : "en_us";
        MutableText langLine = Text.literal("defaultLanguage: " + currentLang)
                .formatted(Formatting.YELLOW);
        MutableText toggleBtn = Text.literal(" [↻ " + toggleLang + "]").formatted(Formatting.GREEN)
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/showmyitem set defaultLanguage " + toggleLang)));
        langLine.append(toggleBtn);
        menu.append(langLine).append("\n");

        menu.append(Text.literal(I18n.translate(player, "text.showmyitem.menu_hint")).formatted(Formatting.GRAY));

        source.sendFeedback(() -> menu, false);
        return 1;
    }
}