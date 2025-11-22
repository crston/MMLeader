package com.gmail.bobason01.party;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.provided.Party;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class PartyProviderMMOCore implements IPartyProvider {

    @Override
    public List<String> getPartyMembers(Player player) {
        List<String> list = new ArrayList<>();
        if (player == null) {
            return list;
        }
        try {
            PlayerData data = PlayerData.get(player);
            Party party = (Party) data.getParty();
            if (party != null) {
                for (PlayerData pd : party.getOnlineMembers()) {
                    if (pd != null && pd.isOnline()) {
                        Player p = pd.getPlayer();
                        if (p != null) {
                            list.add(p.getName());
                        }
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
            PlayerData data = PlayerData.get(player);
            Party party = (Party) data.getParty();
            if (party != null) {
                PlayerData owner = party.getOwner();
                if (owner != null && owner.isOnline()) {
                    Player op = owner.getPlayer();
                    if (op != null) {
                        return op.getName();
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return player.getName();
    }
}
