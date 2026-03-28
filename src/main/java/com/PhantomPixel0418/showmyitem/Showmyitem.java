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

    private static final Pattern ITEM_PATTERN = Pattern.compile("\\[item\\]", Pattern.CASE_INSENSITIVE);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Showmyitem mod");

        // 注册消息装饰器事件，允许修改发送的消息文本
        ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.CONTENT_PHASE, (sender, message) -> {
            String text = message.getString();

            if (ITEM_PATTERN.matcher(text).find()) {
                ItemStack stack = sender != null ? sender.getMainHandStack() : ItemStack.EMPTY;
                Text itemComponent = createItemComponent(stack);
                return replaceItemPlaceholders(message, itemComponent);
            }
            return message;
        });
    }

    /**
     * 创建带方括号的物品显示组件（整个组件可悬停显示物品信息）
     */
    private Text createItemComponent(ItemStack stack) {
        if (stack.isEmpty()) {
            // 空手时显示红色提示，不加括号
            return Text.literal("空手")
                    .formatted(Formatting.RED)
                    .setStyle(Style.EMPTY.withHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("你手里没有物品").formatted(Formatting.RED))
                    ));
        }

        // 获取物品的显示名称（MutableText，保留原有颜色、样式）
        MutableText itemName = stack.getName().copy();

        // 构建悬停事件：直接传递 ItemStack
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(stack));
        Style hoverStyle = Style.EMPTY.withHoverEvent(hoverEvent);

        // 为左括号和右括号添加相同的悬停事件（使整个组件悬停效果一致）
        Text leftBracket = Text.literal("[").setStyle(hoverStyle);
        Text rightBracket = Text.literal("]").setStyle(hoverStyle);

        // 将物品名也添加悬停事件（保留其原有样式，并叠加悬停）
        Text namedItem = itemName.setStyle(itemName.getStyle().withHoverEvent(hoverEvent));

        // 拼接： [ + 物品名 + ]
        return Text.empty()
                .append(leftBracket)
                .append(namedItem)
                .append(rightBracket);
    }

    /**
     * 替换消息中的所有 [item] 占位符
     */
    private Text replaceItemPlaceholders(Text original, Text replacement) {
        String rawString = original.getString();
        Matcher matcher = ITEM_PATTERN.matcher(rawString);

        if (!matcher.find()) {
            return original;
        }

        MutableText result = Text.empty();
        int lastEnd = 0;
        matcher.reset();

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String before = rawString.substring(lastEnd, matcher.start());
                result.append(Text.literal(before));
            }
            result.append(replacement.copy());
            lastEnd = matcher.end();
        }

        if (lastEnd < rawString.length()) {
            String after = rawString.substring(lastEnd);
            result.append(Text.literal(after));
        }

        // 尝试保留原始消息样式
        Style originalStyle = original.getStyle();
        if (originalStyle != null && !originalStyle.isEmpty()) {
            result.setStyle(originalStyle);
        }

        return result;
    }
}