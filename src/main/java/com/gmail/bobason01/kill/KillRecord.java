package com.gmail.bobason01.kill;

public final class KillRecord {

    private final int order;
    private final String player;
    private final String leader;

    public KillRecord(int order, String player, String leader) {
        this.order = order;
        this.player = player;
        this.leader = leader;
    }

    public int getOrder() {
        return order;
    }

    public String getPlayer() {
        return player;
    }

    public String getLeader() {
        return leader;
    }
}
