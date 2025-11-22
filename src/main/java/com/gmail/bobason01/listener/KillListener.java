package com.gmail.bobason01.listener;

import com.gmail.bobason01.DatabaseManager;
import com.gmail.bobason01.party.IPartyProvider;
import com.gmail.bobason01.whitelist.WhitelistManager;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public final class KillListener implements Listener {

    private final DatabaseManager database;
    private final IPartyProvider partyProvider;
    private final WhitelistManager whitelist;

    public KillListener(DatabaseManager database, IPartyProvider partyProvider, WhitelistManager whitelist) {
        this.database = database;
        this.partyProvider = partyProvider;
        this.whitelist = whitelist;
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        LivingEntity killer = event.getKiller();
        if (!(killer instanceof Player)) {
            return;
        }
        Player player = (Player) killer;
        String mobId = event.getMob().getType().getInternalName();
        if (mobId == null || mobId.isEmpty()) {
            return;
        }
        if (whitelist.isEnabled() && !whitelist.isAllowed(mobId)) {
            return;
        }
        List<String> members = partyProvider.getPartyMembers(player);
        String leader = partyProvider.getPartyLeader(player);
        try {
            database.recordKill(mobId, members, leader);
        } catch (Throwable ignored) {
        }
    }
}
