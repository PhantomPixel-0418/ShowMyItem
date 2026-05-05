package com.PhantomPixel0418.showmyitem;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import java.util.*;

public class ViewInventoryCommand {

    private static final Map<String, List<String>> CATEGORIES = new LinkedHashMap<>();
    static {
        CATEGORIES.put("general", Arrays.asList("defaultLanguage"));
        CATEGORIES.put("snapshot", Arrays.asList("snapshotExpiryMs", "maxSnapshots"));
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("showmyitem")
                .executes(ViewInventoryCommand::showAll)
                .then(CommandManager.literal("category")
                        .then(CommandManager.argument("category", StringArgumentType.word())
                                .executes(ViewInventoryCommand::showCategoryConfig)
                        )
                )
                .then(CommandManager.literal("find")
                        .then(CommandManager.argument("query", StringArgumentType.greedyString())
                                .executes(ViewInventoryCommand::findSettings)
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

    private static int showAll(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        MutableText message = Text.literal(I18n.translate(player, "text.showmyitem.current_settings"))
                .formatted(Formatting.WHITE, Formatting.BOLD);
        message.append("\n");

        for (Map.Entry<String, List<String>> entry : CATEGORIES.entrySet()) {
            for (String key : entry.getValue()) {
                MutableText line = formatSettingLine(player, key);
                message.append(line).append("\n");
            }
        }

        // 动态获取真实模组版本
        String modVersion = getModVersion();
        message.append(Text.literal("Mod version: " + modVersion).formatted(Formatting.WHITE)).append("\n");

        message.append(Text.literal(I18n.translate(player, "text.showmyitem.browse_categories"))
                .formatted(Formatting.WHITE)).append("\n");
        message.append(buildCategoryButtons(player));

        source.sendFeedback(() -> message, false);
        return 1;
    }

    private static String getModVersion() {
        Optional<ModContainer> container = FabricLoader.getInstance()
                .getModContainer(Showmyitem.MOD_ID);
        return container.map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
    }

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

    private static MutableText formatSettingLine(ServerPlayerEntity player, String key) {
        String translatedName = I18n.translate(player, "config." + key);
        MutableText line = Text.literal("- " + translatedName + " (" + key + "): ")
                .formatted(Formatting.WHITE);

        ModConfig config = ModConfig.getInstance();
        switch (key) {
            case "defaultLanguage" -> {
                String currentLang = config.defaultLanguage;
                line.append(buildToggleButton(player, "en_us", currentLang));
                line.append(Text.literal(" "));
                line.append(buildToggleButton(player, "zh_cn", currentLang));
            }
            case "snapshotExpiryMs" -> {
                String val = config.snapshotExpiryMs + " ms";
                MutableText valueText = Text.literal(val).formatted(Formatting.YELLOW);
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

    private static MutableText buildToggleButton(ServerPlayerEntity player, String lang, String currentLang) {
        boolean active = lang.equals(currentLang);
        MutableText bracket = Text.literal("[").formatted(Formatting.GREEN);
        MutableText content = Text.literal(lang).formatted(active ? Formatting.YELLOW : Formatting.WHITE);
        MutableText bracketClose = Text.literal("]").formatted(Formatting.GREEN);

        MutableText button = Text.empty().append(bracket).append(content).append(bracketClose);
        if (!active) {
            button.setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/showmyitem set defaultLanguage " + lang))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Text.literal(I18n.translate(player, "text.showmyitem.switch_to", lang)))));
        }
        return button;
    }

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

        ItemStack[] mainItems = snapshot.getItems();      // 36格
        ItemStack[] armorItems = snapshot.getArmor();     // 4格
        ItemStack offhandItem = snapshot.getOffhand();    // 1格

        // 合并为 41 格容器：0-35 主物品栏，36-39 盔甲，40 副手
        SimpleInventory combinedInventory = new SimpleInventory(41);
        for (int i = 0; i < 36; i++) {
            combinedInventory.setStack(i, mainItems[i].copy());
        }
        for (int i = 0; i < 4; i++) {
            combinedInventory.setStack(36 + i, armorItems[i].copy());
        }
        combinedInventory.setStack(40, offhandItem.copy());

        String playerName = snapshot.getPlayerName();
        Text title = Text.literal(I18n.translate(player, "text.showmyitem.inventory_title", playerName));
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInv, p) -> new CustomInventoryScreenHandler(syncId, playerInv, combinedInventory),
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