package com.gmail.bobason01.database;

import com.gmail.bobason01.DatabaseManager;
import com.gmail.bobason01.MMLeader;
import com.gmail.bobason01.kill.KillRecord;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class YamlDatabase implements DatabaseManager {

    private static final class MobCacheEntry {
        final List<KillRecord> records;
        final AtomicInteger nextOrderIndex;
        final ReentrantReadWriteLock lock;

        MobCacheEntry(List<KillRecord> records, int nextOrderIndex) {
            this.records = records;
            this.nextOrderIndex = new AtomicInteger(nextOrderIndex);
            this.lock = new ReentrantReadWriteLock();
        }
    }

    private final MMLeader plugin;
    private File file;
    private YamlConfiguration yaml;
    private final Object yamlLock = new Object();
    private final ConcurrentHashMap<String, MobCacheEntry> cache = new ConcurrentHashMap<>();

    public YamlDatabase(MMLeader plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
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
    public void close() {
        try {
            if (yaml != null && file != null) {
                synchronized (yamlLock) {
                    yaml.save(file);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Yaml save failed on close " + e.getMessage());
        }
    }

    private MobCacheEntry loadMobEntry(String mobId) {
        List<KillRecord> records = new ArrayList<>();
        int index = 0;
        if (yaml != null) {
            List<String> raw;
            synchronized (yamlLock) {
                raw = yaml.getStringList(mobId);
            }
            if (raw != null && !raw.isEmpty()) {
                for (String s : raw) {
                    String[] arr = s.split(",", 2);
                    if (arr.length == 2) {
                        index++;
                        String leader = arr[0];
                        String player = arr[1];
                        records.add(new KillRecord(index, player, leader));
                    }
                }
            }
        }
        return new MobCacheEntry(records, index);
    }

    @Override
    public void recordKill(String mobId, List<String> members, String leader) {
        if (mobId == null || mobId.isEmpty()) {
            return;
        }
        if (members == null || members.isEmpty()) {
            return;
        }
        if (yaml == null || file == null) {
            plugin.getLogger().warning("Yaml is not initialized on recordKill");
            return;
        }
        MobCacheEntry entry = cache.computeIfAbsent(mobId, this::loadMobEntry);
        ReentrantReadWriteLock.WriteLock writeLock = entry.lock.writeLock();
        writeLock.lock();
        try {
            List<KillRecord> toAdd = new ArrayList<>(members.size());
            for (String player : members) {
                if (player == null) {
                    continue;
                }
                int orderIndex = entry.nextOrderIndex.incrementAndGet();
                KillRecord record = new KillRecord(orderIndex, player, leader);
                entry.records.add(record);
                toAdd.add(record);
            }

            List<String> raw;
            synchronized (yamlLock) {
                raw = new ArrayList<>(yaml.getStringList(mobId));
                for (KillRecord record : toAdd) {
                    raw.add(record.getLeader() + "," + record.getPlayer());
                }
                yaml.set(mobId, raw);
                yaml.save(file);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Yaml recordKill failed " + e.getMessage());
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<KillRecord> getKillRecords(String mobId) {
        if (mobId == null || mobId.isEmpty()) {
            return new ArrayList<>();
        }
        MobCacheEntry entry = cache.computeIfAbsent(mobId, this::loadMobEntry);
        ReentrantReadWriteLock.ReadLock readLock = entry.lock.readLock();
        readLock.lock();
        try {
            return new ArrayList<>(entry.records);
        } finally {
            readLock.unlock();
        }
    }
}
