package com.gmail.bobason01;

import com.gmail.bobason01.core.KillHistoryManager;
import com.gmail.bobason01.model.KillGroup;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public final class PlaceholderHook extends PlaceholderExpansion {

    private final MMLeader plugin;
    private final KillHistoryManager historyManager;
    private final PlayerInfoProvider playerInfoProvider;

    public PlaceholderHook(
            MMLeader plugin,
            KillHistoryManager historyManager,
            PlayerInfoProvider playerInfoProvider
    ) {
        this.plugin = plugin;
        this.historyManager = historyManager;
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

            KillGroup group = historyManager.getKillGroup(mobId, killIndex);
            if (group == null) {
                return "";
            }

            if (arr.length == 2) {
                return group.getLeader();
            }

            int memberIndex = parseInt(arr[2]);
            if (memberIndex <= 0) {
                return "";
            }

            if (group.getMembers().size() < memberIndex) {
                return "";
            }

            String targetName = group.getMembers().get(memberIndex - 1);

            if (arr.length == 3) {
                return targetName;
            }

            String key = arr[3].toLowerCase();

            if (key.equals("prefix")) {
                return playerInfoProvider.getLuckPermsPrefix(targetName);
            }

            if (key.equals("suffix")) {
                return playerInfoProvider.getLuckPermsSuffix(targetName);
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
}
