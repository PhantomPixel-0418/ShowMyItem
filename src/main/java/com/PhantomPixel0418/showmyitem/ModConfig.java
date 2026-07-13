package com.PhantomPixel0418.showmyitem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class ModConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("showmyitem.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public long snapshotExpiryMs = 5 * 60 * 1000;
    public int maxSnapshots = 100;
    public String defaultLanguage = "en_us";

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
                Showmyitem.LOGGER.info("Loaded config from {}", CONFIG_PATH);
            } catch (Exception e) {
                Showmyitem.LOGGER.error("Failed to load config, using defaults.", e);
                instance = new ModConfig();
            }
        } else {
            instance = new ModConfig();
            save();
            Showmyitem.LOGGER.info("Created default config at {}", CONFIG_PATH);
        }
        I18n.load();
    }

    public static void reload() {
        load();
    }

    public static void save() {
        try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            Showmyitem.LOGGER.error("Failed to save config.", e);
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
        String noBlockComments = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL).matcher(input).replaceAll("");
        String noLineComments = Pattern.compile("//[^\n]*").matcher(noBlockComments).replaceAll("");
        return noLineComments;
    }
}