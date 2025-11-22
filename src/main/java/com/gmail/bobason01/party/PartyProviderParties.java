package com.gmail.bobason01.party;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class PartyProviderParties implements IPartyProvider {

    private final PartiesAPI api;

    public PartyProviderParties(PartiesAPI api) {
        this.api = api;
    }

    @Override
    public List<String> getPartyMembers(Player player) {
        List<String> result = new ArrayList<>();
        if (player == null) {
            return result;
        }
        try {
            PartyPlayer pp = api.getPartyPlayer(player.getUniqueId());
            if (pp != null && pp.isInParty()) {
                Party party = api.getParty(pp.getPartyId());
                if (party != null) {
                    Set<?> raw = party.getOnlineMembers();
                    if (raw != null && !raw.isEmpty()) {
                        Object first = raw.iterator().next();
                        if (first instanceof PartyPlayer) {
                            for (Object o : raw) {
                                PartyPlayer m = (PartyPlayer) o;
                                UUID uid = m.getPlayerUUID();
                                Player bp = Bukkit.getPlayer(uid);
                                if (bp != null) {
                                    result.add(bp.getName());
                                }
                            }
                        } else if (first instanceof UUID) {
                            for (Object o : raw) {
                                UUID uid = (UUID) o;
                                Player bp = Bukkit.getPlayer(uid);
                                if (bp != null) {
                                    result.add(bp.getName());
                                }
                            }
                        }
                        if (!result.isEmpty()) {
                            return result;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        result.add(player.getName());
        return result;
    }

    @Override
    public String getPartyLeader(Player player) {
        if (player == null) {
            return "";
        }
        try {
            PartyPlayer pp = api.getPartyPlayer(player.getUniqueId());
            if (pp != null && pp.isInParty()) {
                Party party = api.getParty(pp.getPartyId());
                if (party != null) {
                    Object leader = party.getLeader();
                    if (leader instanceof String) {
                        try {
                            UUID uid = UUID.fromString((String) leader);
                            Player lp = Bukkit.getPlayer(uid);
                            if (lp != null) {
                                return lp.getName();
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                    if (leader instanceof PartyPlayer) {
                        PartyPlayer lp = (PartyPlayer) leader;
                        Player bp = Bukkit.getPlayer(lp.getPlayerUUID());
                        if (bp != null) {
                            return bp.getName();
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return player.getName();
    }
}
