package com.gmail.bobason01;

import org.bukkit.entity.Player;
import java.util.List;

public interface IPartyProvider {
    List<String> getPartyMembers(Player player);
    String getPartyLeader(Player player);
}
