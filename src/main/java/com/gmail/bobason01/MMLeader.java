package com.gmail.bobason01;

import com.gmail.bobason01.config.DatabaseConfig;
import com.gmail.bobason01.config.PluginConfig;
import com.gmail.bobason01.config.WhitelistConfig;
import com.gmail.bobason01.database.MySQLDatabase;
import com.gmail.bobason01.database.SQLiteDatabase;
import com.gmail.bobason01.database.YamlDatabase;
import com.gmail.bobason01.listener.KillListener;
import com.gmail.bobason01.party.IPartyProvider;
import com.gmail.bobason01.party.PartyProviderSelector;
import com.gmail.bobason01.whitelist.WhitelistManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class MMLeader extends JavaPlugin {

    private static MMLeader instance;

    private PluginConfig pluginConfig;
    private DatabaseConfig databaseConfig;
    private WhitelistConfig whitelistConfig;

    private DatabaseManager databaseManager;
    private IPartyProvider partyProvider;
    private PlayerInfoProvider playerInfoProvider;
    private WhitelistManager whitelistManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.pluginConfig = new PluginConfig(getConfig());
        this.databaseConfig = new DatabaseConfig(pluginConfig);
        this.whitelistConfig = new WhitelistConfig(pluginConfig);

        this.whitelistManager = new WhitelistManager(whitelistConfig);
        loadDatabase();

        this.partyProvider = PartyProviderSelector.select(pluginConfig);
        this.playerInfoProvider = new PlayerInfoProvider();

        Bukkit.getPluginManager().registerEvents(
                new KillListener(databaseManager, partyProvider, whitelistManager),
                this
        );

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(
                    this,
                    databaseManager,
                    partyProvider,
                    playerInfoProvider
            ).register();
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
        String type = pluginConfig.getString("database.type", "sqlite");
        if (type == null) {
            type = "sqlite";
        }
        type = type.toLowerCase();

        DatabaseManager coreDatabase;

        switch (type) {
            case "mysql":
                coreDatabase = new MySQLDatabase(this, databaseConfig);
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
