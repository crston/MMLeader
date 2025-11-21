package com.gmail.bobason01;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import org.bukkit.Bukkit;

public class PartyProviderSelector {

    public static IPartyProvider select(MMLeader plugin) {

        String mode = plugin.getConfig().getString("party.provider", "auto").toLowerCase();

        if (mode.equals("none")) {
            return new PartyProviderSolo();
        }

        if (mode.equals("parties")) {
            PartiesAPI api = Parties.getApi();
            if (api != null) {
                return new PartyProviderParties(api);
            }
            return new PartyProviderSolo();
        }

        if (mode.equals("mmocore")) {
            if (Bukkit.getPluginManager().isPluginEnabled("MMOCore")) {
                return new PartyProviderMMOCore();
            }
            return new PartyProviderSolo();
        }

        if (mode.equals("mythicdungeons")) {
            if (Bukkit.getPluginManager().isPluginEnabled("MythicDungeons")) {
                return new PartyProviderMythicDungeons();
            }
            return new PartyProviderSolo();
        }

        if (mode.equals("auto")) {
            if (Bukkit.getPluginManager().isPluginEnabled("Parties")) {
                PartiesAPI api = Parties.getApi();
                if (api != null) {
                    return new PartyProviderParties(api);
                }
            }

            if (Bukkit.getPluginManager().isPluginEnabled("MMOCore")) {
                return new PartyProviderMMOCore();
            }

            if (Bukkit.getPluginManager().isPluginEnabled("MythicDungeons")) {
                return new PartyProviderMythicDungeons();
            }

            return new PartyProviderSolo();
        }

        return new PartyProviderSolo();
    }
}
