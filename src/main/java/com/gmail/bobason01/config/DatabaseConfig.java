package com.gmail.bobason01.config;

public final class DatabaseConfig {

    private final String host;
    private final int port;
    private final String database;
    private final String user;
    private final String password;
    private final int poolSize;

    public DatabaseConfig(PluginConfig config) {
        this.host = config.getString("database.mysql.host", "localhost");
        this.port = config.getInt("database.mysql.port", 3306);
        this.database = config.getString("database.mysql.database", "mmleader");
        this.user = config.getString("database.mysql.user", "root");
        this.password = config.getString("database.mysql.password", "password");
        this.poolSize = config.getInt("database.mysql.pool-size", 5);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public int getPoolSize() {
        return poolSize;
    }
}
