package com.gmail.bobason01;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WhitelistManager {

    private final boolean enabled;
    private final Set<String> whitelist;

    public WhitelistManager(FileConfiguration config) {
        this.enabled = config.getBoolean("whitelist.enabled", false);
        this.whitelist = new HashSet<>();
        List<String> mobs = config.getStringList("whitelist.mobs");
        for (String m : mobs) {
            if (m != null) {
                whitelist.add(m.toLowerCase());
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAllowed(String mobId) {
        if (mobId == null) {
            return false;
        }
        return whitelist.contains(mobId.toLowerCase());
    }
}
