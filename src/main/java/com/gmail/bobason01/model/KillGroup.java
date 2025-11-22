package com.gmail.bobason01.model;

import java.util.Collections;
import java.util.List;

public final class KillGroup {

    private final int killIndex;
    private final String leader;
    private final List<String> members;

    public KillGroup(int killIndex, String leader, List<String> members) {
        this.killIndex = killIndex;
        this.leader = leader;
        this.members = members == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(members);
    }

    public int getKillIndex() {
        return killIndex;
    }

    public String getLeader() {
        return leader;
    }

    public List<String> getMembers() {
        return members;
    }
}
