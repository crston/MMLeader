package com.gmail.bobason01.database;

import com.gmail.bobason01.DatabaseManager;
import com.gmail.bobason01.MMLeader;
import com.gmail.bobason01.kill.KillRecord;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class SQLiteDatabase implements DatabaseManager {

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
    private File dbFile;
    private String jdbcUrl;
    private final ConcurrentHashMap<String, MobCacheEntry> cache = new ConcurrentHashMap<>();

    public SQLiteDatabase(MMLeader plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            this.dbFile = new File(plugin.getDataFolder(), "data.db");
            this.jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            Class.forName("org.sqlite.JDBC");
            try (Connection connection = DriverManager.getConnection(jdbcUrl);
                 Statement st = connection.createStatement()) {
                String tableSql = "CREATE TABLE IF NOT EXISTS mmleader (" +
                        "mobId TEXT NOT NULL," +
                        "orderIndex INTEGER NOT NULL," +
                        "player TEXT NOT NULL," +
                        "leader TEXT NOT NULL," +
                        "PRIMARY KEY (mobId, orderIndex)" +
                        ");";
                st.executeUpdate(tableSql);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("SQLite init failed " + e.getMessage());
        }
    }

    @Override
    public void close() {
    }

    private MobCacheEntry loadMobEntry(String mobId) {
        List<KillRecord> records = new ArrayList<>();
        int maxOrder = 0;
        if (jdbcUrl == null) {
            return new MobCacheEntry(records, 0);
        }
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT orderIndex, player, leader FROM mmleader WHERE mobId=? ORDER BY orderIndex ASC")) {
            ps.setString(1, mobId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int orderIndex = rs.getInt("orderIndex");
                    String player = rs.getString("player");
                    String leader = rs.getString("leader");
                    records.add(new KillRecord(orderIndex, player, leader));
                    if (orderIndex > maxOrder) {
                        maxOrder = orderIndex;
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("SQLite load failed " + e.getMessage());
        }
        return new MobCacheEntry(records, maxOrder);
    }

    private void insertRecords(String mobId, List<KillRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        if (jdbcUrl == null) {
            return;
        }
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO mmleader (mobId, orderIndex, player, leader) VALUES (?,?,?,?)")) {
            for (KillRecord record : records) {
                ps.setString(1, mobId);
                ps.setInt(2, record.getOrder());
                ps.setString(3, record.getPlayer());
                ps.setString(4, record.getLeader());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (Exception e) {
            plugin.getLogger().severe("SQLite insert failed " + e.getMessage());
        }
    }

    @Override
    public void recordKill(String mobId, List<String> members, String leader) {
        if (mobId == null || mobId.isEmpty()) {
            return;
        }
        if (members == null || members.isEmpty()) {
            return;
        }
        MobCacheEntry entry = cache.computeIfAbsent(mobId, this::loadMobEntry);
        ReentrantReadWriteLock.WriteLock writeLock = entry.lock.writeLock();
        writeLock.lock();
        try {
            List<KillRecord> toInsert = new ArrayList<>(members.size());
            for (String player : members) {
                if (player == null) {
                    continue;
                }
                int orderIndex = entry.nextOrderIndex.incrementAndGet();
                KillRecord record = new KillRecord(orderIndex, player, leader);
                entry.records.add(record);
                toInsert.add(record);
            }
            insertRecords(mobId, toInsert);
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
