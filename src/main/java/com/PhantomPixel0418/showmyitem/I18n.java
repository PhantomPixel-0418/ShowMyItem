package com.PhantomPixel0418.showmyitem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.nio.charset.StandardCharsets;

public class I18n {
    private static final Gson GSON = new Gson();
    // Per-language translation strings
    private static final Map<String, Map<String, String>> TRANSLATIONS = new HashMap<>();
    private static final Set<String> SUPPORTED_LANGS = new HashSet<>();

    public static void load() {
        TRANSLATIONS.clear();
        SUPPORTED_LANGS.clear();
        ClassLoader classLoader = I18n.class.getClassLoader();

        String resourceBase = "assets/showmyitem/lang/";
        try {
            Enumeration<URL> resources = classLoader.getResources(resourceBase);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                File dir = new File(url.toURI());
                if (dir.isDirectory()) {
                    for (File file : dir.listFiles()) {
                        if (file.getName().endsWith(".json")) {
                            String lang = file.getName().replace(".json", "");
                            SUPPORTED_LANGS.add(lang);

                            try (InputStream is = new FileInputStream(file)) {
                                Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                                Map<String, String> map = GSON.fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
                                if (map != null) {
                                    TRANSLATIONS.put(lang, map);
                                    Showmyitem.LOGGER.info("Loaded language: {}", lang);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Showmyitem.LOGGER.warn("Failed to discover language files", e);
        }

        Showmyitem.LOGGER.info("Loaded translations for: {}", TRANSLATIONS.keySet());
    }

    public static String translate(ServerPlayerEntity player, String key, Object... args) {
        String lang = ModConfig.getInstance().defaultLanguage;
        if (lang == null || !TRANSLATIONS.containsKey(lang)) lang = "en_us";
        Map<String, String> map = TRANSLATIONS.get(lang);
        if (map == null) return key;
        String val = map.getOrDefault(key, key);
        if (args.length > 0) return String.format(val, args);
        return val;
    }

    public static String translate(String key, Object... args) {
        return translate(null, key, args);
    }

    public static List<String> getPlaceholders(ServerPlayerEntity player) {
        Set<String> list = new LinkedHashSet<>(Arrays.asList("item", "offhand", "inventory", "enderchest", "hotbar"));

        // Collect placeholder values from all languages
        for (Map<String, String> langMap : TRANSLATIONS.values()) {
            collectVariant(list, langMap.get("placeholder.item"));
            collectVariant(list, langMap.get("placeholder.offhand"));
            collectVariant(list, langMap.get("placeholder.inventory"));
            collectVariant(list, langMap.get("placeholder.enderchest"));
            collectVariant(list, langMap.get("placeholder.hotbar"));
        }
        // Add digit-suffixed variants for item and hotbar slots (iterate over snapshot of current set)
        List<String> bases = new ArrayList<>(list);
        for (String base : bases) {
            if (base.equals("item") || base.equals("物品") || base.equals("hotbar") || base.equals("快捷栏")) {
                for (int i = 0; i <= 8; i++) list.add(base + i);
            }
        }
        return new ArrayList<>(list);
    }

    private static void collectVariant(Set<String> list, String val) {
        if (val != null && !list.contains(val)) list.add(val);
    }

    public static Set<String> getAllTranslations(String key) {
        Set<String> result = new LinkedHashSet<>();
        for (Map<String, String> langMap : TRANSLATIONS.values()) {
            String val = langMap.get(key);
            if (val != null) result.add(val);
        }
        return result;
    }
}