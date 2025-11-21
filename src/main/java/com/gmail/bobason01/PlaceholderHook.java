package com.gmail.bobason01;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaceholderHook extends PlaceholderExpansion {

    private final MMLeader plugin;
    private final DatabaseManager databaseManager;
    private final IPartyProvider partyProvider;
    private final PlayerInfoProvider playerInfoProvider;

    private final boolean sortDesc;

    public PlaceholderHook(
            MMLeader plugin,
            DatabaseManager db,
            IPartyProvider partyProvider,
            PlayerInfoProvider playerInfoProvider
    ) {
        this.plugin = plugin;
        this.databaseManager = db;
        this.partyProvider = partyProvider;
        this.playerInfoProvider = playerInfoProvider;

        String mode = plugin.getConfig().getString("options.sort-mode", "asc");
        this.sortDesc = mode.equalsIgnoreCase("desc");
    }

    @Override
    public String getIdentifier() {
        return "mythicleader";
    }

    @Override
    public String getAuthor() {
        return "crston";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (params == null || params.isEmpty()) {
            return "";
        }

        try {
            String[] arr = params.split("_");
            if (arr.length < 2) {
                return "";
            }

            String mobId = arr[0];
            int killIndex = parseInt(arr[1]);
            if (killIndex <= 0) {
                return "";
            }

            List<KillRecord> sourceList = databaseManager.getKillRecords(mobId);
            if (sourceList.isEmpty()) {
                return "";
            }

            List<KillRecord> list = new ArrayList<>(sourceList);

            if (sortDesc) {
                Collections.reverse(list);
            }

            if (list.size() < killIndex) {
                return "";
            }

            KillRecord record = list.get(killIndex - 1);

            if (arr.length == 2) {
                return record.getPlayer();
            }

            int partyIdx = parseInt(arr[2]);
            if (partyIdx <= 0) {
                return "";
            }

            String target;
            if (partyIdx == 1) {
                target = record.getLeader();
            } else {
                target = record.getPlayer();
            }

            if (arr.length == 3) {
                return target;
            }

            String meta = arr[3].toLowerCase();

            if (meta.contains("prefix")) {
                return playerInfoProvider.getLuckPermsPrefix(target);
            }

            if (meta.contains("suffix")) {
                return playerInfoProvider.getLuckPermsSuffix(target);
            }

            return target;

        } catch (Throwable t) {
            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().warning("Placeholder error " + t.getMessage());
            }
            return "";
        }
    }

    private int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Throwable ignored) {
            return -1;
        }
    }
}
