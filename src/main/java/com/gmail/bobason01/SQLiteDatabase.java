package com.gmail.bobason01;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SQLiteDatabase implements DatabaseManager {

    private final MMLeader plugin;
    private Connection connection;
    private final Map<String, List<KillRecord>> cache = new ConcurrentHashMap<>();

    public SQLiteDatabase(MMLeader plugin) {
        this.plugin = plugin;
    }

    @Override
    public synchronized void init() {
        try {
            File folder = plugin.getDataFolder();
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File file = new File(folder, "data.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            Statement st = connection.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS mmleader (mobId TEXT, orderIndex INT, player TEXT, leader TEXT);");
            st.close();
        } catch (Exception e) {
            plugin.getLogger().severe("SQLite init failed " + e.getMessage());
        }
    }

    @Override
    public synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("SQLite close failed " + e.getMessage());
        }
    }

    private List<KillRecord> loadFromDatabase(String mobId) {
        List<KillRecord> list = new ArrayList<>();
        if (connection == null) {
            return list;
        }
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT orderIndex, player, leader FROM mmleader WHERE mobId=? ORDER BY orderIndex ASC;");
            ps.setString(1, mobId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new KillRecord(
                        rs.getInt("orderIndex"),
                        rs.getString("player"),
                        rs.getString("leader")
                ));
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            plugin.getLogger().severe("SQLite select failed " + e.getMessage());
        }
        return list;
    }

    private List<KillRecord> getOrLoad(String mobId) {
        List<KillRecord> cached = cache.get(mobId);
        if (cached != null) {
            return cached;
        }
        List<KillRecord> loaded = loadFromDatabase(mobId);
        cache.put(mobId, loaded);
        return loaded;
    }

    @Override
    public synchronized void recordKill(String mobId, List<String> members, String leader) {
        if (members == null || members.isEmpty()) {
            return;
        }
        if (connection == null) {
            plugin.getLogger().warning("SQLite connection is null on recordKill");
            return;
        }
        try {
            List<KillRecord> current = getOrLoad(mobId);
            int base = current.size();
            PreparedStatement ps = connection.prepareStatement("INSERT INTO mmleader (mobId, orderIndex, player, leader) VALUES (?,?,?,?);");
            List<KillRecord> toAdd = new ArrayList<>();
            int index = 1;
            for (String p : members) {
                if (p == null) {
                    continue;
                }
                int orderIndex = base + index;
                ps.setString(1, mobId);
                ps.setInt(2, orderIndex);
                ps.setString(3, p);
                ps.setString(4, leader);
                ps.addBatch();
                toAdd.add(new KillRecord(orderIndex, p, leader));
                index++;
            }
            ps.executeBatch();
            ps.close();
            if (!toAdd.isEmpty()) {
                current.addAll(toAdd);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("SQLite insert failed " + e.getMessage());
        }
    }

    @Override
    public synchronized List<KillRecord> getKillRecords(String mobId) {
        List<KillRecord> list = getOrLoad(mobId);
        return new ArrayList<>(list);
    }
}
