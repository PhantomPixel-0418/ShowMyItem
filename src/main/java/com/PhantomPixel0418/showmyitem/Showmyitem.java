package com.PhantomPixel0418.showmyitem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
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

    private static final int TICK_INTERVAL = 1200;
    private static final int MAIN_INVENTORY_SIZE = 36;
    private static final int ARMOR_SLOT_COUNT = 4;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Showmyitem mod");

        ModConfig.load();
        LOGGER.info("Config loaded: expiryMs={}, maxSnapshots={}",
                ModConfig.getInstance().snapshotExpiryMs,
                ModConfig.getInstance().maxSnapshots);

        CommandRegistrationCallback.EVENT.register(ViewInventoryCommand::register);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            EnderchestShareCommand.register(dispatcher);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % TICK_INTERVAL == 0) {
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
            String enderPlace = I18n.translate(player, "placeholder.enderchest").toLowerCase();

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
                    ItemStack[] inventory = new ItemStack[MAIN_INVENTORY_SIZE];
                    for (int i = 0; i < MAIN_INVENTORY_SIZE; i++) {
                        inventory[i] = player.getInventory().getStack(i).copy();
                    }
                    ItemStack[] armor = new ItemStack[ARMOR_SLOT_COUNT];
                    for (int i = 0; i < ARMOR_SLOT_COUNT; i++) {
                        armor[i] = player.getInventory().getStack(5 + i).copy();
                    }
                    ItemStack offhand = player.getOffHandStack().copy();

                    String playerName = player.getName().getString();
                    UUID snapshotId = InventorySnapshotManager.storeSnapshot(
                            inventory, armor, offhand, playerName, player.getUuid());
                    result.append(createInventoryComponent(player, snapshotId));
                } else if (placeholder.equals("enderchest") || placeholder.equals(enderPlace) || placeholder.equals("末影箱")) {
                    SimpleInventory tempEnder = InventoryUtils.copyEnderChest(player.getEnderChestInventory());
                    ItemStack[] enderItems = new ItemStack[tempEnder.size()];
                    for (int i = 0; i < enderItems.length; i++) {
                        enderItems[i] = tempEnder.getStack(i);
                    }

                    String playerName = player.getName().getString();
                    UUID snapshotId = InventorySnapshotManager.storeSnapshot(
                            enderItems, new ItemStack[0], ItemStack.EMPTY,
                            playerName, player.getUuid());
                    result.append(createEnderChestComponent(player, snapshotId));
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
                            new HoverEvent.ShowText(Text.literal(noItem).formatted(Formatting.RED))
                    ));
        }

        // Check if this is a shulker box (regardless of contents)
        ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
        if (container != null) {
            return createShulkerBoxComponent(player, stack, container);
        }

        MutableText itemName = stack.getName().copy();
        int count = stack.getCount();
        int maxCount = stack.getMaxCount();

        String suffix = (maxCount == 1) ? "" : " ✕" + count;
        HoverEvent.ShowItem hoverEvent = new HoverEvent.ShowItem(stack);
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

    private Text createShulkerBoxComponent(ServerPlayerEntity player, ItemStack shulkerStack, ContainerComponent container) {
        List<ItemStack> stacks = container.stream().toList();
        ItemStack[] items = new ItemStack[27];
        for (int i = 0; i < stacks.size() && i < 27; i++) {
            items[i] = stacks.get(i).copy();
        }

        String playerName = player.getName().getString();
        UUID snapshotId = InventorySnapshotManager.storeShulkerBoxSnapshot(
                items, playerName, player.getUuid());

        // Build the shulker box display text with both hover and click
        HoverEvent.ShowItem hoverEvent = new HoverEvent.ShowItem(shulkerStack);
        Style clickStyle = Style.EMPTY
                .withHoverEvent(hoverEvent)
                .withClickEvent(new ClickEvent.RunCommand("/showmyitem viewinv " + snapshotId.toString()));

        MutableText itemName = shulkerStack.getName().copy();
        int count = shulkerStack.getCount();
        int maxCount = shulkerStack.getMaxCount();
        String suffix = (maxCount == 1) ? "" : " ✕" + count;

        MutableText itemNameWithClick = itemName.setStyle(clickStyle);
        MutableText itemWithCount;
        if (suffix.isEmpty()) {
            itemWithCount = itemNameWithClick;
        } else {
            Text countText = Text.literal(suffix)
                    .formatted(Formatting.GRAY, Formatting.ITALIC)
                    .setStyle(clickStyle);
            itemWithCount = itemNameWithClick.append(countText);
        }

        MutableText leftBracket = Text.literal("[").setStyle(clickStyle);
        MutableText rightBracket = Text.literal("]").setStyle(clickStyle);

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
                .withClickEvent(new ClickEvent.RunCommand("/showmyitem viewinv " + snapshotId.toString()))
                .withHoverEvent(new HoverEvent.ShowText(Text.literal(hoverText))));
    }

    private Text createEnderChestComponent(ServerPlayerEntity player, UUID snapshotId) {
        String playerName = player.getName().getString();
        String linkText = I18n.translate(player, "text.showmyitem.enderchest_link", playerName);
        String hoverText = I18n.translate(player, "text.showmyitem.enderchest_hover");

        return Text.literal(linkText)
                .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent.RunCommand("/showmyitem viewender " + snapshotId.toString()))
                .withHoverEvent(new HoverEvent.ShowText(Text.literal(hoverText))));
    }
}