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
                // 无参数时显示全部设置
                .executes(ViewInventoryCommand::showAll)
                // 分类浏览
                .then(CommandManager.literal("category")
                        .then(CommandManager.argument("category", StringArgumentType.word())
                                .executes(ViewInventoryCommand::showCategoryConfig)
                        )
                )
                // 模糊搜索
                .then(CommandManager.literal("find")
                        .then(CommandManager.argument("query", StringArgumentType.greedyString())
                                .executes(ViewInventoryCommand::findSettings)
                        )
                )
                // 查看背包快照
                .then(CommandManager.literal("viewinv")
                        .then(CommandManager.argument("snapshot", StringArgumentType.word())
                                .executes(ViewInventoryCommand::viewInventory)
                        )
                )
                // 重载配置
                .then(CommandManager.literal("reloadconfig")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(ViewInventoryCommand::reloadConfig)
                )
                // 修改配置值
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

    // ---------- 主菜单：显示所有设置 + 分类标签 ----------
    private static int showAll(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        MutableText message = Text.literal(I18n.translate(player, "text.showmyitem.current_settings"))
                .formatted(Formatting.WHITE, Formatting.BOLD);
        message.append("\n");

        // 遍历所有类别输出设置项（平铺显示）
        for (Map.Entry<String, List<String>> entry : CATEGORIES.entrySet()) {
            for (String key : entry.getValue()) {
                MutableText line = formatSettingLine(player, key);
                message.append(line).append("\n");
            }
        }

        // 版本信息（可从 gradle.properties 读取，这里简单写死模组版本，也可通过 FabricLoader 获取）
        message.append(Text.literal("Mod version: 1.1.0")   // 之后可改为动态读取
                .formatted(Formatting.WHITE)).append("\n");

        // 分类浏览
        message.append(Text.literal(I18n.translate(player, "text.showmyitem.browse_categories"))
                .formatted(Formatting.WHITE)).append("\n");
        message.append(buildCategoryButtons(player));

        source.sendFeedback(() -> message, false);
        return 1;
    }

    // ---------- 模糊搜索 ----------
    private static int findSettings(CommandContext<ServerCommandSource> context) {
        String query = StringArgumentType.getString(context, "query").toLowerCase();
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        MutableText message = Text.literal(I18n.translate(player, "text.showmyitem.settings_matching", query))
                .formatted(Formatting.WHITE, Formatting.BOLD);
        message.append("\n");

        boolean found = false;
        for (Map.Entry<String, List<String>> entry : CATEGORIES.entrySet()) {
            for (String key : entry.getValue()) {
                String translatedName = I18n.translate(player, "config." + key).toLowerCase();
                // 匹配键名或翻译名
                if (key.toLowerCase().contains(query) || translatedName.contains(query)) {
                    MutableText line = formatSettingLine(player, key);
                    message.append(line).append("\n");
                    found = true;
                }
            }
        }

        if (!found) {
            message.append(Text.literal(I18n.translate(player, "text.showmyitem.no_matching_found"))
                    .formatted(Formatting.GRAY));
        }

        source.sendFeedback(() -> message, false);
        return 1;
    }

    // ---------- 分类浏览 ----------
    private static int showCategoryConfig(CommandContext<ServerCommandSource> context) {
        String catKey = StringArgumentType.getString(context, "category");
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        List<String> keys = CATEGORIES.get(catKey);
        if (keys == null) {
            source.sendError(Text.literal(I18n.translate(player, "text.showmyitem.invalid_category", catKey)));
            return 0;
        }

        MutableText message = Text.literal(I18n.translate(player, "text.showmyitem.current_settings"))
                .formatted(Formatting.WHITE, Formatting.BOLD);
        message.append("\n");

        for (String key : keys) {
            MutableText line = formatSettingLine(player, key);
            message.append(line).append("\n");
        }

        source.sendFeedback(() -> message, false);
        return 1;
    }

    // ---------- 工具方法：格式化一个设置项为一行 ----------
    private static MutableText formatSettingLine(ServerPlayerEntity player, String key) {
        String translatedName = I18n.translate(player, "config." + key);
        MutableText line = Text.literal("- " + translatedName + " (" + key + "): ")
                .formatted(Formatting.WHITE);

        ModConfig config = ModConfig.getInstance();
        switch (key) {
            case "defaultLanguage" -> {
                // 语言切换按钮：与 Carpet 的 [true] [false] 风格一致
                String currentLang = config.defaultLanguage;
                // 两个语言选项
                line.append(buildToggleButton(player, "en_us", currentLang));
                line.append(Text.literal(" "));
                line.append(buildToggleButton(player, "zh_cn", currentLang));
            }
            case "snapshotExpiryMs" -> {
                String val = config.snapshotExpiryMs + " ms";
                MutableText valueText = Text.literal(val).formatted(Formatting.YELLOW);
                // 点击建议命令，悬停提示“点击修改”
                Style editStyle = Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                "/showmyitem set snapshotExpiryMs "))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Text.literal(I18n.translate(player, "text.showmyitem.click_to_edit"))));
                valueText.setStyle(editStyle);
                line.append(valueText);
            }
            case "maxSnapshots" -> {
                String val = String.valueOf(config.maxSnapshots);
                MutableText valueText = Text.literal(val).formatted(Formatting.YELLOW);
                Style editStyle = Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                "/showmyitem set maxSnapshots "))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Text.literal(I18n.translate(player, "text.showmyitem.click_to_edit"))));
                valueText.setStyle(editStyle);
                line.append(valueText);
            }
            default -> line.append(Text.literal("?"));
        }
        return line;
    }

    // 构造语言切换按钮，仿照 [true] [false] 样式：绿色方括号，当前语言黄色，非当前白色
    private static MutableText buildToggleButton(ServerPlayerEntity player, String lang, String currentLang) {
        boolean active = lang.equals(currentLang);
        MutableText bracket = Text.literal("[").formatted(Formatting.GREEN);
        MutableText content = Text.literal(lang)
                .formatted(active ? Formatting.YELLOW : Formatting.WHITE);
        MutableText bracketClose = Text.literal("]").formatted(Formatting.GREEN);

        MutableText button = Text.empty().append(bracket).append(content).append(bracketClose);
        if (!active) {
            // 点击可切换至该语言
            button.setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/showmyitem set defaultLanguage " + lang))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Text.literal(I18n.translate(player, "text.showmyitem.switch_to", lang)))));
        }
        return button;
    }

    // 构造分类按钮行（Carpet 风格：绿色方括号，浅蓝色文字，悬停提示）
    private static MutableText buildCategoryButtons(ServerPlayerEntity player) {
        MutableText line = Text.empty();
        for (String catKey : CATEGORIES.keySet()) {
            String catName = I18n.translate(player, "category." + catKey);
            MutableText bracket = Text.literal("[").formatted(Formatting.GREEN);
            MutableText catText = Text.literal(catName).formatted(Formatting.AQUA);
            MutableText bracketClose = Text.literal("]").formatted(Formatting.GREEN);

            MutableText button = Text.empty().append(bracket).append(catText).append(bracketClose);
            button.setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/showmyitem category " + catKey))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Text.literal(I18n.translate(player, "text.showmyitem.list_category_hint", catName)))));
            line.append(button);
            line.append(Text.literal(" "));
        }
        return line;
    }

    // ---------- 原有的查看背包、重载、设置方法（保留不变）----------
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