package com.PhantomPixel0418.showmyitem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple shared ender chest manager. Persists a map ownerUuid -> set of member UUIDs to a JSON file in config.
 * Note: minimal implementation for initial feature – permission model is whitelist (owner + invited members).
 */
public class SharedEnderChestManager {
    private static final String FILE_NAME = "enderchest_shared.json";
    private static final Gson GSON = new Gson();

    private final Map<UUID, Set<UUID>> shares = new ConcurrentHashMap<>();

    private static SharedEnderChestManager instance;

    public static synchronized SharedEnderChestManager getInstance() {
        if (instance == null) instance = new SharedEnderChestManager();
        return instance;
    }

    private SharedEnderChestManager() {
        load();
    }

    public synchronized void invite(UUID owner, UUID member) {
        shares.computeIfAbsent(owner, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(member);
        save();
    }

    public synchronized void revoke(UUID owner, UUID member) {
        Set<UUID> set = shares.get(owner);
        if (set != null) {
            set.remove(member);
            if (set.isEmpty()) shares.remove(owner);
            save();
        }
    }

    public synchronized Set<UUID> listMembers(UUID owner) {
        return Collections.unmodifiableSet(shares.getOrDefault(owner, Collections.emptySet()));
    }

    public synchronized boolean isSharedWith(UUID owner, UUID member) {
        return owner.equals(member) || shares.getOrDefault(owner, Collections.emptySet()).contains(member);
    }

    private File getFile() {
        File configDir = FabricLoader.getInstance().getConfigDir().toFile();
        return new File(configDir, FILE_NAME);
    }

    private void load() {
        try {
            File f = getFile();
            if (!f.exists()) return;
            FileReader r = new FileReader(f, StandardCharsets.UTF_8);
            Type type = new TypeToken<Map<UUID, List<UUID>>>() {}.getType();
            Map<UUID, List<UUID>> raw = GSON.fromJson(r, type);
            r.close();
            if (raw != null) {
                for (Map.Entry<UUID, List<UUID>> e : raw.entrySet()) {
                    shares.put(e.getKey(), Collections.newSetFromMap(new ConcurrentHashMap<>()));
                    shares.get(e.getKey()).addAll(e.getValue());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void save() {
        try {
            File f = getFile();
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            Map<UUID, List<UUID>> raw = new HashMap<>();
            for (Map.Entry<UUID, Set<UUID>> e : shares.entrySet()) {
                raw.put(e.getKey(), new ArrayList<>(e.getValue()));
            }
            FileWriter w = new FileWriter(f, StandardCharsets.UTF_8);
            GSON.toJson(raw, w);
            w.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
