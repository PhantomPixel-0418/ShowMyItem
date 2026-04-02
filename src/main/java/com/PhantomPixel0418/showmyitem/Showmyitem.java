package com.PhantomPixel0418.showmyitem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Showmyitem implements ModInitializer {
    public static final String MOD_ID = "showmyitem";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // 匹配主手和副手占位符（不区分大小写）
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\[(item|offhand)\\]", Pattern.CASE_INSENSITIVE);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Showmyitem mod");

        ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.CONTENT_PHASE, (sender, message) -> {
            String rawText = message.getString();
            Matcher matcher = PLACEHOLDER_PATTERN.matcher(rawText);

            if (!matcher.find()) {
                return message; // 没有占位符，直接返回原消息
            }

            MutableText result = Text.empty();
            int lastEnd = 0;
            matcher.reset();

            while (matcher.find()) {
                // 添加占位符之前的普通文本
                if (matcher.start() > lastEnd) {
                    String before = rawText.substring(lastEnd, matcher.start());
                    result.append(Text.literal(before));
                }

                // 根据占位符类型获取对应物品
                String placeholder = matcher.group(1).toLowerCase();
                ItemStack stack;
                if ("item".equals(placeholder)) {
                    stack = (sender != null) ? sender.getMainHandStack() : ItemStack.EMPTY;
                } else { // offhand
                    stack = (sender != null) ? sender.getOffHandStack() : ItemStack.EMPTY;
                }
                Text itemComponent = createItemComponent(stack);
                result.append(itemComponent);

                lastEnd = matcher.end();
            }

            // 添加最后一段普通文本
            if (lastEnd < rawText.length()) {
                String after = rawText.substring(lastEnd);
                result.append(Text.literal(after));
            }

            // 尝试保留原始消息的整体样式（如颜色等）
            Style originalStyle = message.getStyle();
            if (originalStyle != null && !originalStyle.isEmpty()) {
                result.setStyle(originalStyle);
            }

            return result;
        });
    }

    /**
     * 创建带方括号的物品显示组件（主副手通用）
     * 数量部分显示为灰色斜体（仅对可堆叠物品），物品名称保留原有颜色
     */
    private Text createItemComponent(ItemStack stack) {
        if (stack.isEmpty()) {
            return Text.translatable("text.showmyitem.empty_hand")
                    .formatted(Formatting.RED)
                    .setStyle(Style.EMPTY.withHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Text.translatable("text.showmyitem.no_item").formatted(Formatting.RED))
                    ));
        }

        MutableText itemName = stack.getName().copy();
        int count = stack.getCount();
        int maxCount = stack.getMaxCount();

        String suffix = (maxCount == 1) ? "" : " ✕" + count;
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(stack));
        Style hoverStyle = Style.EMPTY.withHoverEvent(hoverEvent);

        MutableText itemNameWithHover = itemName.setStyle(itemName.getStyle().withHoverEvent(hoverStyle));

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
}