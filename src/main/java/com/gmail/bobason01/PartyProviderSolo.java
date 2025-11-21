package com.gmail.bobason01;

import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class PartyProviderSolo implements IPartyProvider {

    @Override
    public List<String> getPartyMembers(Player player) {
        List<String> list = new ArrayList<>();
        if (player != null) {
            list.add(player.getName());
        }
        return list;
    }

    @Override
    public String getPartyLeader(Player player) {
        return player != null ? player.getName() : "";
    }
}
