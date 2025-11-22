package com.gmail.bobason01.database;

import com.gmail.bobason01.MMLeader;
import com.gmail.bobason01.model.KillGroup;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class YamlBackend implements DatabaseBackend {

    private final MMLeader plugin;
    private File file;
    private YamlConfiguration yaml;
    private final Object lock = new Object();

    public YamlBackend(MMLeader plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            file = new File(plugin.getDataFolder(), "groups.yml");
            if (!file.exists()) {
                file.createNewFile();
            }
            yaml = YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            plugin.getLogger().severe("Yaml backend init failed " + e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            if (yaml != null && file != null) {
                synchronized (lock) {
                    yaml.save(file);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Yaml backend close failed " + e.getMessage());
        }
    }

    @Override
    public void saveGroup(String mobId, KillGroup group) {
        if (yaml == null || file == null || mobId == null || group == null) {
            return;
        }
        try {
            synchronized (lock) {
                String path = mobId;
                List<String> list = yaml.getStringList(path);
                if (list == null) {
                    list = new ArrayList<>();
                }
                String members = String.join(",", group.getMembers());
                String line = group.getKillIndex() + ";" + group.getLeader() + ";" + members;
                list.add(line);
                yaml.set(path, list);
                yaml.save(file);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Yaml saveGroup failed " + e.getMessage());
        }
    }
}
