package com.PhantomPixel0418.showmyitem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SharedEnderChestManager {
    private static final String FILE_NAME = "enderchest_shared.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int CURRENT_VERSION = 1;

    private final Map<UUID, Set<UUID>> shares = new ConcurrentHashMap<>();
    private static SharedEnderChestManager instance;

    public static synchronized SharedEnderChestManager getInstance() {
        if (instance == null) instance = new SharedEnderChestManager();
        return instance;
    }

    private SharedEnderChestManager() {
        load();
    }

    public void invite(UUID owner, UUID member) {
        shares.computeIfAbsent(owner, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(member);
        save();
    }

    public void revoke(UUID owner, UUID member) {
        Set<UUID> set = shares.get(owner);
        if (set != null) {
            set.remove(member);
            if (set.isEmpty()) shares.remove(owner);
            save();
        }
    }

    public Set<UUID> listMembers(UUID owner) {
        return Collections.unmodifiableSet(shares.getOrDefault(owner, Collections.emptySet()));
    }

    public boolean isSharedWith(UUID owner, UUID member) {
        return owner.equals(member) || shares.getOrDefault(owner, Collections.emptySet()).contains(member);
    }

    public UUID findInviter(UUID member) {
        for (Map.Entry<UUID, Set<UUID>> entry : shares.entrySet()) {
            if (entry.getValue().contains(member)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private File getFile() {
        return new File(FabricLoader.getInstance().getConfigDir().toFile(), FILE_NAME);
    }

    private void load() {
        File f = getFile();
        if (!f.exists()) return;

        DataContainer data = loadFromFile(f);
        if (data == null) {
            File bak = new File(f.getParentFile(), f.getName() + ".bak");
            if (bak.exists()) {
                Showmyitem.LOGGER.info("Attempting to restore ender chest shares from backup");
                data = loadFromFile(bak);
                if (data != null) {
                    Showmyitem.LOGGER.info("Restored from backup successfully");
                }
            }
        }

        if (data != null) {
            shares.clear();
            for (Map.Entry<UUID, List<UUID>> e : data.shares.entrySet()) {
                shares.put(e.getKey(), Collections.newSetFromMap(new ConcurrentHashMap<>()));
                shares.get(e.getKey()).addAll(e.getValue());
            }
            // Future: if data.version != CURRENT_VERSION, perform migration
        }
    }

    private DataContainer loadFromFile(File file) {
        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            Type type = new TypeToken<DataContainer>() {}.getType();
            return GSON.fromJson(reader, type);
        } catch (Exception ex) {
            Showmyitem.LOGGER.error("Failed to load ender chest shares from " + file.getName(), ex);
            return null;
        }
    }

    private void save() {
        File target = getFile();
        File temp = new File(target.getParentFile(), target.getName() + ".tmp");

        DataContainer container = new DataContainer();
        container.version = CURRENT_VERSION;
        container.shares = new HashMap<>();
        for (Map.Entry<UUID, Set<UUID>> e : shares.entrySet()) {
            container.shares.put(e.getKey(), new ArrayList<>(e.getValue()));
        }

        try (FileWriter writer = new FileWriter(temp, StandardCharsets.UTF_8)) {
            GSON.toJson(container, writer);
            writer.flush();
            Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ex) {
            Showmyitem.LOGGER.error("Failed to save ender chest shares", ex);
            if (target.exists()) {
                try {
                    File bak = new File(target.getParentFile(), target.getName() + ".bak");
                    Files.copy(target.toPath(), bak.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException backupEx) {
                    Showmyitem.LOGGER.error("Failed to create backup before save", backupEx);
                }
            }
        }
    }

    private static class DataContainer {
        int version;
        Map<UUID, List<UUID>> shares;
    }
}