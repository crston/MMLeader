package com.gmail.bobason01;

import com.gmail.bobason01.kill.KillRecord;

import java.util.List;

public interface DatabaseManager {

    void init();

    void close();

    void recordKill(String mobId, List<String> partyMembers, String partyLeader);

    List<KillRecord> getKillRecords(String mobId);
}
