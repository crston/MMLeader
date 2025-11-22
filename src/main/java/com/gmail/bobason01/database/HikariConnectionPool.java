package com.gmail.bobason01.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class HikariConnectionPool {

    private final HikariDataSource dataSource;

    public HikariConnectionPool(String host, int port, String database, String user, String password, int poolSize) {
        HikariConfig config = new HikariConfig();

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4";

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(password);

        if (poolSize <= 0) {
            poolSize = 5;
        }

        config.setMaximumPoolSize(poolSize);
        config.setMinimumIdle(Math.max(1, poolSize / 2));
        config.setConnectionTimeout(10000);
        config.setMaxLifetime(1800000);
        config.setIdleTimeout(600000);
        config.setPoolName("MMLeader-Hikari");

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void shutdown() {
        if (!dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
