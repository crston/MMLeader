package com.gmail.bobason01;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PartyProviderParties implements IPartyProvider {

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

                    Object raw = party.getOnlineMembers();
                    if (raw instanceof List<?>) {
                        List<?> list = (List<?>) raw;

                        if (!list.isEmpty()) {
                            Object f = list.get(0);

                            if (f instanceof PartyPlayer) {
                                for (Object o : list) {
                                    PartyPlayer m = (PartyPlayer) o;
                                    UUID uid = m.getPlayerUUID();
                                    Player bp = Bukkit.getPlayer(uid);
                                    if (bp != null) {
                                        result.add(bp.getName());
                                    }
                                }
                            } else if (f instanceof UUID) {
                                for (Object o : list) {
                                    UUID uid = (UUID) o;
                                    Player bp = Bukkit.getPlayer(uid);
                                    if (bp != null) {
                                        result.add(bp.getName());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        if (result.isEmpty() && player != null) {
            result.add(player.getName());
        }
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
                if (party == null) {
                    return player.getName();
                }

                Object leaderObj = party.getLeader();
                if (leaderObj instanceof String) {
                    try {
                        Player lp = Bukkit.getPlayer(UUID.fromString((String) leaderObj));
                        return lp != null ? lp.getName() : player.getName();
                    } catch (Throwable ignored) {
                    }
                }

                if (leaderObj instanceof PartyPlayer) {
                    PartyPlayer lpp = (PartyPlayer) leaderObj;
                    Player lp = Bukkit.getPlayer(lpp.getPlayerUUID());
                    return lp != null ? lp.getName() : player.getName();
                }
            }
        } catch (Throwable ignored) {
        }

        return player.getName();
    }
}
