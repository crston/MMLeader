package com.gmail.bobason01;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class YamlDatabase implements DatabaseManager {

    private final MMLeader plugin;
    private File file;
    private YamlConfiguration yaml;
    private final Map<String, List<KillRecord>> cache = new ConcurrentHashMap<>();

    public YamlDatabase(MMLeader plugin) {
        this.plugin = plugin;
    }

    @Override
    public synchronized void init() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            file = new File(plugin.getDataFolder(), "kills.yml");
            if (!file.exists()) {
                file.createNewFile();
            }
            yaml = YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            plugin.getLogger().severe("Yaml init failed " + e.getMessage());
        }
    }

    @Override
    public synchronized void close() {
        try {
            if (yaml != null && file != null) {
                yaml.save(file);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Yaml save failed on close " + e.getMessage());
        }
    }

    @Override
    public synchronized void recordKill(String mobId, List<String> members, String leader) {
        if (members == null || members.isEmpty()) {
            return;
        }
        if (yaml == null || file == null) {
            plugin.getLogger().warning("Yaml is not initialized on recordKill");
            return;
        }
        try {
            List<String> list = new ArrayList<>(yaml.getStringList(mobId));
            for (String p : members) {
                if (p != null) {
                    list.add(leader + "," + p);
                }
            }
            yaml.set(mobId, list);
            yaml.save(file);

            List<KillRecord> records = new ArrayList<>();
            int index = 1;
            for (String s : list) {
                String[] arr = s.split(",");
                if (arr.length == 2) {
                    records.add(new KillRecord(index, arr[1], arr[0]));
                    index++;
                }
            }
            cache.put(mobId, records);
        } catch (Exception e) {
            plugin.getLogger().severe("Yaml recordKill failed " + e.getMessage());
        }
    }

    @Override
    public synchronized List<KillRecord> getKillRecords(String mobId) {
        List<KillRecord> cachedList = cache.get(mobId);
        if (cachedList != null) {
            return new ArrayList<>(cachedList);
        }
        List<KillRecord> list = new ArrayList<>();
        if (yaml == null) {
            return list;
        }
        List<String> raw = yaml.getStringList(mobId);
        int index = 1;
        for (String s : raw) {
            String[] arr = s.split(",");
            if (arr.length == 2) {
                list.add(new KillRecord(index, arr[1], arr[0]));
                index++;
            }
        }
        cache.put(mobId, list);
        return new ArrayList<>(list);
    }
}
