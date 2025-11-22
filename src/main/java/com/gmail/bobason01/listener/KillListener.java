package com.gmail.bobason01.listener;

import com.gmail.bobason01.core.KillHistoryManager;
import com.gmail.bobason01.party.IPartyProvider;
import com.gmail.bobason01.whitelist.WhitelistManager;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public final class KillListener implements Listener {

    private final KillHistoryManager historyManager;
    private final IPartyProvider partyProvider;
    private final WhitelistManager whitelist;

    public KillListener(KillHistoryManager historyManager, IPartyProvider partyProvider, WhitelistManager whitelist) {
        this.historyManager = historyManager;
        this.partyProvider = partyProvider;
        this.whitelist = whitelist;
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        Entity killerEntity = event.getKiller();
        Player killer = null;

        if (killerEntity instanceof Player) {
            killer = (Player) killerEntity;
        } else if (killerEntity instanceof LivingEntity) {
            LivingEntity le = (LivingEntity) killerEntity;
            if (le.getKiller() != null) {
                killer = le.getKiller();
            }
        }

        if (killer == null) {
            return;
        }

        String mobId = event.getMob().getType().getInternalName();
        if (mobId == null || mobId.isEmpty()) {
            return;
        }

        if (whitelist.isEnabled() && !whitelist.isAllowed(mobId)) {
            return;
        }

        List<String> members = partyProvider.getPartyMembers(killer);
        String leader = partyProvider.getPartyLeader(killer);
        if (leader == null || leader.isEmpty()) {
            leader = killer.getName();
        }

        try {
            historyManager.recordKill(mobId, leader, members);
        } catch (Throwable ignored) {
        }
    }
}
