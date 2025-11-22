package com.gmail.bobason01;

import com.gmail.bobason01.kill.KillRecord;
import com.gmail.bobason01.party.IPartyProvider;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class PlaceholderHook extends PlaceholderExpansion {

    private final MMLeader plugin;
    private final DatabaseManager databaseManager;
    private final IPartyProvider partyProvider;
    private final PlayerInfoProvider playerInfoProvider;

    public PlaceholderHook(
            MMLeader plugin,
            DatabaseManager databaseManager,
            IPartyProvider partyProvider,
            PlayerInfoProvider playerInfoProvider
    ) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.partyProvider = partyProvider;
        this.playerInfoProvider = playerInfoProvider;
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
        return "2.0";
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

            List<KillRecord> flatList = databaseManager.getKillRecords(mobId);
            if (flatList.isEmpty()) {
                return "";
            }

            List<List<KillRecord>> grouped = groupByKill(flatList);
            if (grouped.isEmpty() || grouped.size() < killIndex) {
                return "";
            }

            List<KillRecord> selectedGroup = grouped.get(killIndex - 1);

            if (arr.length == 2) {
                KillRecord leaderRecord = selectedGroup.get(0);
                return leaderRecord.getPlayer();
            }

            int partyIndex = parseInt(arr[2]);
            if (partyIndex <= 0) {
                return "";
            }

            if (selectedGroup.size() < partyIndex) {
                return "";
            }

            KillRecord target = selectedGroup.get(partyIndex - 1);

            if (arr.length == 3) {
                return target.getPlayer();
            }

            String key = arr[3].toLowerCase();

            if (key.equals("prefix")) {
                return playerInfoProvider.getLuckPermsPrefix(target.getPlayer());
            }

            if (key.equals("suffix")) {
                return playerInfoProvider.getLuckPermsSuffix(target.getPlayer());
            }

            return "";
        } catch (Throwable t) {
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

    private List<List<KillRecord>> groupByKill(List<KillRecord> list) {
        List<List<KillRecord>> result = new ArrayList<>();
        List<KillRecord> current = new ArrayList<>();
        String currentLeader = null;

        for (KillRecord record : list) {
            String leader = record.getLeader();
            if (currentLeader == null || !currentLeader.equals(leader)) {
                if (!current.isEmpty()) {
                    result.add(current);
                }
                current = new ArrayList<>();
                currentLeader = leader;
            }
            current.add(record);
        }

        if (!current.isEmpty()) {
            result.add(current);
        }

        return result;
    }
}
