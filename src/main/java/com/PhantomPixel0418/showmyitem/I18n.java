package com.PhantomPixel0418.showmyitem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class I18n {
    private static final Gson GSON = new Gson();
    private static final Map<String, Map<String, String>> TRANSLATIONS = new HashMap<>();
    private static final String[] SUPPORTED_LANGS = {"en_us", "zh_cn"};

    public static void load() {
        TRANSLATIONS.clear();
        ClassLoader classLoader = I18n.class.getClassLoader();
        for (String lang : SUPPORTED_LANGS) {
            String resourcePath = "assets/showmyitem/lang/" + lang + ".json";
            try (InputStream is = classLoader.getResourceAsStream(resourcePath)) {
                if (is == null) {
                    Showmyitem.LOGGER.warn("Language file not found: {}", resourcePath);
                    continue;
                }
                Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                Type type = new TypeToken<Map<String, String>>() {}.getType();
                Map<String, String> map = GSON.fromJson(reader, type);
                if (map != null) {
                    TRANSLATIONS.put(lang, map);
                    Showmyitem.LOGGER.info("Loaded language: {}", lang);
                }
            } catch (Exception e) {
                Showmyitem.LOGGER.warn("Failed to load language file: {}", resourcePath, e);
            }
        }
        Showmyitem.LOGGER.info("Loaded translations for: {}", TRANSLATIONS.keySet());
    }

    /**
     * 根据服务器配置的默认语言进行翻译
     */
    public static String translate(ServerPlayerEntity player, String key, Object... args) {
        String lang = ModConfig.getInstance().defaultLanguage;
        if (lang == null || !TRANSLATIONS.containsKey(lang)) {
            lang = "en_us";
        }
        Map<String, String> map = TRANSLATIONS.get(lang);
        if (map == null) return key;
        String template = map.getOrDefault(key, key);
        if (args.length > 0) {
            return String.format(template, args);
        }
        return template;
    }

    /**
     * 无玩家时的翻译（控制台等）
     */
    public static String translate(String key, Object... args) {
        return translate(null, key, args);
    }

    /**
     * 获取所有可用语言的占位符列表（合并 en_us 和当前服务器语言）
     * 这样玩家无论使用哪种语言的占位符都能被识别
     */
    public static List<String> getPlaceholders(ServerPlayerEntity player) {
        // 基础英文占位符始终存在
        List<String> list = new ArrayList<>(Arrays.asList("item", "offhand", "inventory"));

        // 添加服务器默认语言的占位符
        String lang = ModConfig.getInstance().defaultLanguage;
        if (lang == null) lang = "en_us";
        Map<String, String> map = TRANSLATIONS.get(lang);
        if (map != null) {
            addIfPresent(map, list, "placeholder.item");
            addIfPresent(map, list, "placeholder.offhand");
            addIfPresent(map, list, "placeholder.inventory");
        }

        // 额外再合并另一种语言的占位符（若与默认不同），实现多语言兼容
        for (String l : SUPPORTED_LANGS) {
            if (!l.equals(lang)) {
                Map<String, String> m = TRANSLATIONS.get(l);
                if (m != null) {
                    addIfPresent(m, list, "placeholder.item");
                    addIfPresent(m, list, "placeholder.offhand");
                    addIfPresent(m, list, "placeholder.inventory");
                }
            }
        }
        return list;
    }

    private static void addIfPresent(Map<String, String> map, List<String> list, String key) {
        String value = map.get(key);
        if (value != null && !list.contains(value)) {
            list.add(value);
        }
    }
}