package com.PhantomPixel0418.showmyitem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private static final Map<String, Map<String, String>> TRANSLATIONS = new HashMap<>();
    private static final Set<String> SUPPORTED_LANGS = new HashSet<>();

    public static void load() {
        TRANSLATIONS.clear();
        SUPPORTED_LANGS.clear();
        ClassLoader classLoader = I18n.class.getClassLoader();

        // Discover available language files dynamically
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
                                Type type = new TypeToken<Map<String, String>>() {}.getType();
                                Map<String, String> map = GSON.fromJson(reader, type);
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

    public static String translate(String key, Object... args) {
        return translate(null, key, args);
    }

    public static List<String> getPlaceholders(ServerPlayerEntity player) {
        Set<String> list = new LinkedHashSet<>(Arrays.asList("item", "offhand", "inventory", "enderchest"));

        String lang = ModConfig.getInstance().defaultLanguage;
        if (lang == null) lang = "en_us";
        Map<String, String> map = TRANSLATIONS.get(lang);
        if (map != null) {
            addIfPresent(map, list, "placeholder.item");
            addIfPresent(map, list, "placeholder.offhand");
            addIfPresent(map, list, "placeholder.inventory");
            addIfPresent(map, list, "placeholder.enderchest");
        }

        for (String l : SUPPORTED_LANGS) {
            if (!l.equals(lang)) {
                Map<String, String> m = TRANSLATIONS.get(l);
                if (m != null) {
                    addIfPresent(m, list, "placeholder.item");
                    addIfPresent(m, list, "placeholder.offhand");
                    addIfPresent(m, list, "placeholder.inventory");
                    addIfPresent(m, list, "placeholder.enderchest");
                }
            }
        }
        return new ArrayList<>(list);
    }

    private static void addIfPresent(Map<String, String> map, Set<String> list, String key) {
        String value = map.get(key);
        if (value != null && !list.contains(value)) {
            list.add(value);
        }
    }
}