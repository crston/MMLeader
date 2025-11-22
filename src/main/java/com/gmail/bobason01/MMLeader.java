package com.gmail.bobason01;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.gmail.bobason01.config.DatabaseConfig;
import com.gmail.bobason01.config.PluginConfig;
import com.gmail.bobason01.config.WhitelistConfig;
import com.gmail.bobason01.core.KillHistoryManager;
import com.gmail.bobason01.database.DatabaseBackend;
import com.gmail.bobason01.database.MySQLBackend;
import com.gmail.bobason01.database.SQLiteBackend;
import com.gmail.bobason01.database.YamlBackend;
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

    private DatabaseBackend backend;
    private KillHistoryManager historyManager;
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

        this.backend = createBackend(databaseConfig);
        if (backend != null) {
            backend.init();
        }

        int maxPerMob = pluginConfig.getInt("options.max-kills-per-mob", 100);
        int asyncThreads = pluginConfig.getInt("options.async-threads", 1);
        this.historyManager = new KillHistoryManager(backend, maxPerMob, asyncThreads);

        this.partyProvider = PartyProviderSelector.select(pluginConfig);
        this.playerInfoProvider = new PlayerInfoProvider();

        Bukkit.getPluginManager().registerEvents(
                new KillListener(historyManager, partyProvider, whitelistManager),
                this
        );

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(
                    this,
                    historyManager,
                    playerInfoProvider
            ).register();
        }

        getLogger().info("MMLeader 2.0 enabled");
    }

    @Override
    public void onDisable() {
        try {
            if (historyManager != null) {
                historyManager.shutdown();
            }
        } catch (Throwable ignored) {
        }
        try {
            if (backend != null) {
                backend.close();
            }
        } catch (Throwable ignored) {
        }
        getLogger().info("MMLeader disabled");
    }

    private DatabaseBackend createBackend(DatabaseConfig cfg) {
        String type = cfg.getType();
        switch (type) {
            case "mysql":
                return new MySQLBackend(this, cfg);
            case "yaml":
                return new YamlBackend(this);
            case "sqlite":
            default:
                return new SQLiteBackend(this);
        }
    }

    public static MMLeader getInstance() {
        return instance;
    }

    public KillHistoryManager getHistoryManager() {
        return historyManager;
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

    public PartiesAPI getPartiesApiIfPresent() {
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("Parties")) {
                return Parties.getApi();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}
