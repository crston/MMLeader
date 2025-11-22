package com.gmail.bobason01.config;

import java.util.ArrayList;
import java.util.List;

public final class WhitelistConfig {

    private final boolean enabled;
    private final List<String> mobs;

    public WhitelistConfig(PluginConfig config) {
        this.enabled = config.getBoolean("whitelist.enabled", false);
        List<String> list = config.get().getStringList("whitelist.mobs");
        this.mobs = list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<String> getMobs() {
        return mobs;
    }
}
