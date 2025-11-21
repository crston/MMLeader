package com.gmail.bobason01;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MMLeader extends JavaPlugin {

    private static MMLeader instance;

    private DatabaseManager databaseManager;
    private IPartyProvider partyProvider;
    private PlayerInfoProvider playerInfoProvider;
    private WhitelistManager whitelistManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        whitelistManager = new WhitelistManager(getConfig());
        loadDatabase();

        partyProvider = PartyProviderSelector.select(this);
        playerInfoProvider = new PlayerInfoProvider();

        Bukkit.getPluginManager().registerEvents(new KillListener(this, databaseManager, partyProvider, whitelistManager), this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(this, databaseManager, partyProvider, playerInfoProvider).register();
        }

        getLogger().info("MMLeader enabled");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    private void loadDatabase() {
        String type = getConfig().getString("database.type", "sqlite").toLowerCase();

        DatabaseManager coreDatabase;

        switch (type) {
            case "mysql":
                coreDatabase = new MySQLDatabase(
                        this,
                        getConfig().getString("database.mysql.host", "localhost"),
                        getConfig().getString("database.mysql.port", "3306"),
                        getConfig().getString("database.mysql.database", "mmleader"),
                        getConfig().getString("database.mysql.user", "root"),
                        getConfig().getString("database.mysql.password", "password")
                );
                break;

            case "yaml":
                coreDatabase = new YamlDatabase(this);
                break;

            case "sqlite":
            default:
                coreDatabase = new SQLiteDatabase(this);
                break;
        }

        databaseManager = new AsyncDatabaseManager(coreDatabase);
        databaseManager.init();
    }

    public static MMLeader getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public IPartyProvider getPartyProvider() {
        return partyProvider;
    }

    public PlayerInfoProvider getPlayerInfoProvider() {
        return playerInfoProvider;
    }

    public WhitelistManager getWhitelistManager() {
        return whitelistManager;
    }
}
