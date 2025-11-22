package com.gmail.bobason01.database;

import com.gmail.bobason01.MMLeader;
import com.gmail.bobason01.config.DatabaseConfig;
import com.gmail.bobason01.model.KillGroup;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;

public final class MySQLBackend implements DatabaseBackend {

    private final MMLeader plugin;
    private final DatabaseConfig config;
    private HikariDataSource dataSource;

    public MySQLBackend(MMLeader plugin, DatabaseConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void init() {
        try {
            HikariConfig hc = new HikariConfig();

            String jdbcUrl = "jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase()
                    + "?useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4";

            hc.setJdbcUrl(jdbcUrl);
            hc.setUsername(config.getUser());
            hc.setPassword(config.getPassword());

            int poolSize = config.getPoolSize() <= 0 ? 5 : config.getPoolSize();
            hc.setMaximumPoolSize(poolSize);
            hc.setMinimumIdle(Math.max(1, poolSize / 2));
            hc.setConnectionTimeout(10000);
            hc.setMaxLifetime(1800000);
            hc.setIdleTimeout(600000);
            hc.setPoolName("MMLeader-Hikari");

            hc.addDataSourceProperty("cachePrepStmts", "true");
            hc.addDataSourceProperty("prepStmtCacheSize", "250");
            hc.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(hc);

            try (Connection con = dataSource.getConnection()) {
                String sql = "CREATE TABLE IF NOT EXISTS mmleader_groups (" +
                        "mobId VARCHAR(64) NOT NULL," +
                        "killIndex INT NOT NULL," +
                        "leader VARCHAR(64) NOT NULL," +
                        "members TEXT NOT NULL," +
                        "PRIMARY KEY (mobId, killIndex)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
                con.prepareStatement(sql).executeUpdate();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("MySQL backend init failed " + e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("MySQL backend close failed " + e.getMessage());
        }
    }

    @Override
    public void saveGroup(String mobId, KillGroup group) {
        if (dataSource == null || mobId == null || group == null) {
            return;
        }
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO mmleader_groups (mobId, killIndex, leader, members) VALUES (?,?,?,?)")) {
            ps.setString(1, mobId);
            ps.setInt(2, group.getKillIndex());
            ps.setString(3, group.getLeader());
            String members = String.join(",", group.getMembers());
            ps.setString(4, members);
            ps.executeUpdate();
        } catch (Exception e) {
            plugin.getLogger().severe("MySQL saveGroup failed " + e.getMessage());
        }
    }
}
