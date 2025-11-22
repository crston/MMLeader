package com.gmail.bobason01.whitelist;

import com.gmail.bobason01.config.WhitelistConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class WhitelistManager {

    private final boolean enabled;
    private final Set<String> allowed;

    public WhitelistManager(WhitelistConfig config) {
        this.enabled = config.isEnabled();
        this.allowed = new HashSet<>();
        List<String> mobs = config.getMobs();
        if (mobs != null) {
            for (String m : mobs) {
                if (m != null) {
                    allowed.add(m.toLowerCase());
                }
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
        return allowed.contains(mobId.toLowerCase());
    }
}
