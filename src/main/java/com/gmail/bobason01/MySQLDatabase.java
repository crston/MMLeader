package com.gmail.bobason01;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MySQLDatabase implements DatabaseManager {

    private final MMLeader plugin;
    private Connection connection;

    private final String host;
    private final String port;
    private final String database;
    private final String user;
    private final String password;

    private final Map<String, List<KillRecord>> cache = new ConcurrentHashMap<>();

    public MySQLDatabase(MMLeader plugin, String host, String port, String database, String user, String password) {
        this.plugin = plugin;
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    @Override
    public synchronized void init() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true",
                    user,
                    password
            );
            Statement st = connection.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS mmleader (mobId VARCHAR(64), orderIndex INT, player VARCHAR(64), leader VARCHAR(64));");
            st.close();
        } catch (Exception e) {
            plugin.getLogger().severe("MySQL init failed " + e.getMessage());
        }
    }

    @Override
    public synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("MySQL close failed " + e.getMessage());
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
            plugin.getLogger().severe("MySQL select failed " + e.getMessage());
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
            plugin.getLogger().warning("MySQL connection is null on recordKill");
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
            plugin.getLogger().severe("MySQL insert failed " + e.getMessage());
        }
    }

    @Override
    public synchronized List<KillRecord> getKillRecords(String mobId) {
        List<KillRecord> list = getOrLoad(mobId);
        return new ArrayList<>(list);
    }
}
