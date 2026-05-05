package com.PhantomPixel0418.showmyitem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Showmyitem implements ModInitializer {
    public static final String MOD_ID = "showmyitem";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Showmyitem mod");

        ModConfig.load();
        LOGGER.info("Config loaded: expiryMs={}, maxSnapshots={}",
                ModConfig.getInstance().snapshotExpiryMs,
                ModConfig.getInstance().maxSnapshots);

        CommandRegistrationCallback.EVENT.register(ViewInventoryCommand::register);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 1200 == 0) {
                InventorySnapshotManager.cleanExpired();
            }
        });

        ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.CONTENT_PHASE, (sender, message) -> {
            if (!(sender instanceof ServerPlayerEntity player)) return message;
            String rawText = message.getString();

            List<String> placeholders = I18n.getPlaceholders(player);
            String regex = "\\[(" + placeholders.stream()
                    .map(Pattern::quote)
                    .collect(Collectors.joining("|")) + ")\\]";
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(rawText);
            if (!matcher.find()) return message;

            MutableText result = Text.empty();
            int lastEnd = 0;
            matcher.reset();

            String itemPlace = I18n.translate(player, "placeholder.item").toLowerCase();
            String offhandPlace = I18n.translate(player, "placeholder.offhand").toLowerCase();
            String inventoryPlace = I18n.translate(player, "placeholder.inventory").toLowerCase();

            while (matcher.find()) {
                if (matcher.start() > lastEnd) {
                    result.append(Text.literal(rawText.substring(lastEnd, matcher.start()))
                            .setStyle(message.getStyle()));
                }

                String placeholder = matcher.group(1).toLowerCase();
                if (placeholder.equals("item") || placeholder.equals(itemPlace)) {
                    result.append(createItemComponent(player.getMainHandStack(), player));
                } else if (placeholder.equals("offhand") || placeholder.equals(offhandPlace)) {
                    result.append(createItemComponent(player.getOffHandStack(), player));
                } else if (placeholder.equals("inventory") || placeholder.equals(inventoryPlace) || placeholder.equals("背包")) {
                    // 拷贝主物品栏 36 格
                    ItemStack[] inventory = new ItemStack[36];
                    for (int i = 0; i < 36; i++) {
                        inventory[i] = player.getInventory().getStack(i).copy();
                    }
                    // 拷贝盔甲 4 格
                    ItemStack[] armor = new ItemStack[4];
                    for (int i = 0; i < 4; i++) {
                        armor[i] = player.getInventory().getArmorStack(i).copy();
                    }
                    // 拷贝副手
                    ItemStack offhand = player.getOffHandStack().copy();

                    String playerName = player.getName().getString();
                    UUID snapshotId = InventorySnapshotManager.storeSnapshot(
                            inventory, armor, offhand, playerName, player.getUuid());
                    result.append(createInventoryComponent(player, snapshotId));
                } else {
                    result.append(Text.literal("[" + placeholder + "]"));
                }

                lastEnd = matcher.end();
            }

            if (lastEnd < rawText.length()) {
                result.append(Text.literal(rawText.substring(lastEnd)).setStyle(message.getStyle()));
            }

            return result;
        });
    }

    private Text createItemComponent(ItemStack stack, ServerPlayerEntity player) {
        if (stack.isEmpty()) {
            String empty = I18n.translate(player, "text.showmyitem.empty_hand");
            String noItem = I18n.translate(player, "text.showmyitem.no_item");
            return Text.literal(empty)
                    .formatted(Formatting.RED)
                    .setStyle(Style.EMPTY.withHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Text.literal(noItem).formatted(Formatting.RED))
                    ));
        }

        MutableText itemName = stack.getName().copy();
        int count = stack.getCount();
        int maxCount = stack.getMaxCount();

        String suffix = (maxCount == 1) ? "" : " ✕" + count;
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_ITEM,
                new HoverEvent.ItemStackContent(stack));
        Style hoverStyle = Style.EMPTY.withHoverEvent(hoverEvent);

        MutableText itemNameWithHover = itemName.setStyle(hoverStyle);
        MutableText itemWithCount;
        if (suffix.isEmpty()) {
            itemWithCount = itemNameWithHover;
        } else {
            Text countText = Text.literal(suffix)
                    .formatted(Formatting.GRAY, Formatting.ITALIC)
                    .setStyle(hoverStyle);
            itemWithCount = itemNameWithHover.append(countText);
        }

        Text leftBracket = Text.literal("[").setStyle(hoverStyle);
        Text rightBracket = Text.literal("]").setStyle(hoverStyle);

        return Text.empty()
                .append(leftBracket)
                .append(itemWithCount)
                .append(rightBracket);
    }

    private Text createInventoryComponent(ServerPlayerEntity player, UUID snapshotId) {
        String playerName = player.getName().getString();
        String linkText = I18n.translate(player, "text.showmyitem.inventory_link", playerName);
        String hoverText = I18n.translate(player, "text.showmyitem.inventory_hover");

        return Text.literal(linkText)
                .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/showmyitem viewinv " + snapshotId.toString()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Text.literal(hoverText))));
    }
}