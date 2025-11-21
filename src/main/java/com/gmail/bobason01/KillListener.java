package com.gmail.bobason01;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class KillListener implements Listener {

    private final MMLeader plugin;
    private final DatabaseManager databaseManager;
    private final IPartyProvider partyProvider;
    private final WhitelistManager whitelistManager;

    public KillListener(MMLeader plugin, DatabaseManager databaseManager, IPartyProvider partyProvider, WhitelistManager whitelistManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.partyProvider = partyProvider;
        this.whitelistManager = whitelistManager;
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent event) {

        LivingEntity killer = event.getKiller();

        if (!(killer instanceof Player)) {
            return;
        }

        Player player = (Player) killer;

        String mobId = event.getMob().getType().getInternalName();

        if (whitelistManager != null && whitelistManager.isEnabled() && !whitelistManager.isAllowed(mobId)) {
            return;
        }

        List<String> members = partyProvider.getPartyMembers(player);
        String leader = partyProvider.getPartyLeader(player);

        try {
            databaseManager.recordKill(mobId, members, leader);
        } catch (Throwable t) {
            plugin.getLogger().severe("Failed to record kill " + t.getMessage());
        }
    }
}
