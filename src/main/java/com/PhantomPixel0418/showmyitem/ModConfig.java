package com.PhantomPixel0418.showmyitem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class ModConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("showmyitem.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger(Showmyitem.MOD_ID);

    public long snapshotExpiryMs = 5 * 60 * 1000;   // 5分钟
    public int maxSnapshots = 100;
    public String defaultLanguage = "en_us";         // 新增：默认语言

    private static ModConfig instance;

    public static ModConfig getInstance() {
        if (instance == null) {
            instance = new ModConfig();
        }
        return instance;
    }

    public static void load() {
        if (CONFIG_PATH.toFile().exists()) {
            try (Reader reader = new FileReader(CONFIG_PATH.toFile())) {
                String content = readAll(reader);
                content = stripJsonComments(content);
                instance = GSON.fromJson(content, ModConfig.class);
                LOGGER.info("Loaded config from {}", CONFIG_PATH);
            } catch (Exception e) {
                LOGGER.error("Failed to load config, using defaults.", e);
                instance = new ModConfig();
            }
        } else {
            instance = new ModConfig();
            saveTemplate();
            LOGGER.info("Created default config at {}", CONFIG_PATH);
        }
        I18n.load();   // 每次加载配置后重新加载语言
    }

    public static void reload() {
        load();
    }

    private static void saveTemplate() {
        String template = "{\n" +
                "  // 背包快照的过期时间（毫秒），超过此时间后快照将无法查看。\n" +
                "  \"snapshotExpiryMs\": " + instance.snapshotExpiryMs + ",\n" +
                "  // 同时存储的最大快照数量，超出后自动删除最早的快照。\n" +
                "  \"maxSnapshots\": " + instance.maxSnapshots + ",\n" +
                "  // 服务器默认语言，例如 \"en_us\" 或 \"zh_cn\"\n" +
                "  \"defaultLanguage\": \"" + instance.defaultLanguage + "\"\n" +
                "}\n";
        try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
            writer.write(template);
        } catch (IOException e) {
            LOGGER.error("Failed to save config template.", e);
        }
    }

    private static String readAll(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[1024];
        int len;
        while ((len = reader.read(buffer)) != -1) {
            sb.append(buffer, 0, len);
        }
        return sb.toString();
    }

    private static String stripJsonComments(String input) {
        // 移除 /* ... */ 风格注释
        String noBlockComments = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL).matcher(input).replaceAll("");
        // 移除 // 行注释
        String noLineComments = Pattern.compile("//[^\n]*").matcher(noBlockComments).replaceAll("");
        return noLineComments;
    }
}