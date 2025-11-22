package com.gmail.bobason01.database;

import com.gmail.bobason01.kill.KillRecord;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class KillRecordCache {

    private final Map<String, List<KillRecord>> cache = new ConcurrentHashMap<>();

    public List<KillRecord> get(String mobId) {
        List<KillRecord> list = cache.get(mobId);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    public void set(String mobId, List<KillRecord> records) {
        if (mobId == null || records == null) {
            return;
        }
        List<KillRecord> immutable = Collections.unmodifiableList(records);
        cache.put(mobId, immutable);
    }

    public void clear(String mobId) {
        if (mobId == null) {
            return;
        }
        cache.remove(mobId);
    }

    public void clearAll() {
        cache.clear();
    }
}
