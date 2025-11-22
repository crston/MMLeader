package com.gmail.bobason01.database;

import com.gmail.bobason01.MMLeader;
import com.gmail.bobason01.model.KillGroup;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public final class SQLiteBackend implements DatabaseBackend {

    private final MMLeader plugin;
    private File dbFile;
    private String jdbcUrl;

    public SQLiteBackend(MMLeader plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            this.dbFile = new File(plugin.getDataFolder(), "mmleader.db");
            this.jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            Class.forName("org.sqlite.JDBC");
            try (Connection connection = DriverManager.getConnection(jdbcUrl);
                 Statement st = connection.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS mmleader_groups (" +
                        "mobId TEXT NOT NULL," +
                        "killIndex INTEGER NOT NULL," +
                        "leader TEXT NOT NULL," +
                        "members TEXT NOT NULL," +
                        "PRIMARY KEY (mobId, killIndex)" +
                        ");";
                st.executeUpdate(sql);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("SQLite backend init failed " + e.getMessage());
        }
    }

    @Override
    public void close() {
    }

    @Override
    public void saveGroup(String mobId, KillGroup group) {
        if (jdbcUrl == null || mobId == null || group == null) {
            return;
        }
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO mmleader_groups (mobId, killIndex, leader, members) VALUES (?,?,?,?)")) {
            ps.setString(1, mobId);
            ps.setInt(2, group.getKillIndex());
            ps.setString(3, group.getLeader());
            String members = String.join(",", group.getMembers());
            ps.setString(4, members);
            ps.executeUpdate();
        } catch (Exception e) {
            plugin.getLogger().severe("SQLite saveGroup failed " + e.getMessage());
        }
    }
}
