package com.PhantomPixel0418.showmyitem;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import java.util.*;

public class ViewInventoryCommand {

    // 配置项分类定义（类别键 → 包含的配置键列表）
    private static final Map<String, List<String>> CATEGORIES = new LinkedHashMap<>();
    static {
        CATEGORIES.put("general", Arrays.asList("defaultLanguage"));
        CATEGORIES.put("snapshot", Arrays.asList("snapshotExpiryMs", "maxSnapshots"));
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("showmyitem")
                .executes(ViewInventoryCommand::showCategories)
                .then(CommandManager.literal("category")
                        .then(CommandManager.argument("category", StringArgumentType.word())
                                .executes(ViewInventoryCommand::showCategoryConfig)
                        )
                )
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

    private static int showCategories(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        ModConfig config = ModConfig.getInstance();

        MutableText menu = Text.literal(I18n.translate(player, "text.showmyitem.menu_title"))
                .formatted(Formatting.WHITE, Formatting.BOLD);
        menu.append("\n");

        // 列出所有类别按钮
        for (String catKey : CATEGORIES.keySet()) {
            String catName = I18n.translate(player, "category." + catKey);
            MutableText btn = Text.literal("[" + catName + "]").formatted(Formatting.AQUA)
                    .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/showmyitem category " + catKey)));
            menu.append(btn).append(" ");
        }

        source.sendFeedback(() -> menu, false);
        return 1;
    }

    private static int showCategoryConfig(CommandContext<ServerCommandSource> context) {
        String catKey = StringArgumentType.getString(context, "category");
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        ModConfig config = ModConfig.getInstance();

        List<String> keys = CATEGORIES.get(catKey);
        if (keys == null) {
            source.sendError(Text.literal(I18n.translate(player, "text.showmyitem.invalid_category", catKey)));
            return 0;
        }

        MutableText message = Text.literal(I18n.translate(player, "text.showmyitem.menu_title"))
                .formatted(Formatting.WHITE, Formatting.BOLD);
        message.append("\n");

        String catDesc = I18n.translate(player, "category." + catKey + ".desc");
        message.append(Text.literal(catDesc).formatted(Formatting.GRAY, Formatting.ITALIC)).append("\n");

        for (String key : keys) {
            String translatedName = I18n.translate(player, "config." + key);
            MutableText line = Text.literal("- " + translatedName + " (" + key + "): ");
            // 根据键获取当前值
            String currentVal = switch (key) {
                case "snapshotExpiryMs" -> config.snapshotExpiryMs + " ms";
                case "maxSnapshots" -> String.valueOf(config.maxSnapshots);
                case "defaultLanguage" -> config.defaultLanguage;
                default -> "?";
            };
            line.append(Text.literal(currentVal).formatted(Formatting.GRAY, Formatting.ITALIC));

            // 编辑按钮
            MutableText editBtn = Text.literal(" [✎]").formatted(Formatting.AQUA)
                    .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                            "/showmyitem set " + key + " ")));
            line.append(editBtn);
            message.append(line).append("\n");
        }

        source.sendFeedback(() -> message, false);
        return 1;
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
}