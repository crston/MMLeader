package com.gmail.bobason01.party;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.gmail.bobason01.config.PluginConfig;
import org.bukkit.Bukkit;

public final class PartyProviderSelector {

    private PartyProviderSelector() {
    }

    public static IPartyProvider select(PluginConfig config) {
        String mode = config.getString("party.provider", "auto");
        if (mode == null) {
            mode = "auto";
        }
        mode = mode.toLowerCase();

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
