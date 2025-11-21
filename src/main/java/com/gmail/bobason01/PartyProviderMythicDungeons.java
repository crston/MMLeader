package com.gmail.bobason01;

import net.playavalon.mythicdungeons.MythicDungeons;
import net.playavalon.mythicdungeons.player.MythicPlayer;
import net.playavalon.mythicdungeons.player.party.partysystem.MythicParty;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PartyProviderMythicDungeons implements IPartyProvider {

    @Override
    public List<String> getPartyMembers(Player player) {
        List<String> list = new ArrayList<>();
        if (player == null) {
            return list;
        }

        try {
            Object partyObj = MythicDungeons.inst().getPartyManager().getParty(player);
            if (partyObj instanceof MythicParty) {
                MythicParty party = (MythicParty) partyObj;

                for (MythicPlayer mp : party.getMythicPlayers()) {
                    Player p = mp.getPlayer();
                    if (p != null) {
                        list.add(p.getName());
                    }
                }

                if (!list.isEmpty()) {
                    return list;
                }
            }
        } catch (Throwable ignored) {
        }

        list.add(player.getName());
        return list;
    }

    @Override
    public String getPartyLeader(Player player) {
        if (player == null) {
            return "";
        }

        try {
            Object partyObj = MythicDungeons.inst().getPartyManager().getParty(player);
            if (partyObj instanceof MythicParty) {
                MythicParty party = (MythicParty) partyObj;

                Player leader = party.getLeader();
                if (leader != null) {
                    return leader.getName();
                }
            }
        } catch (Throwable ignored) {
        }

        return player.getName();
    }
}
