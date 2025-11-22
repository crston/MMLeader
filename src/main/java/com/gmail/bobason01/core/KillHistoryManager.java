package com.gmail.bobason01.core;

import com.gmail.bobason01.database.DatabaseBackend;
import com.gmail.bobason01.model.KillGroup;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class KillHistoryManager {

    private static final class MobHistory {
        final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        final Deque<KillGroup> groups = new ArrayDeque<>();
        int nextKillIndex;
    }

    private final ConcurrentHashMap<String, MobHistory> history = new ConcurrentHashMap<>();
    private final DatabaseBackend backend;
    private final ExecutorService executor;
    private final int maxPerMob;

    public KillHistoryManager(DatabaseBackend backend, int maxPerMob, int asyncThreads) {
        this.backend = backend;
        this.maxPerMob = maxPerMob <= 0 ? 100 : maxPerMob;
        int threads = asyncThreads <= 0 ? 1 : asyncThreads;
        this.executor = Executors.newFixedThreadPool(threads);
    }

    public void shutdown() {
        try {
            executor.shutdown();
        } catch (Throwable ignored) {
        }
    }

    public void recordKill(String mobId, String leader, List<String> members) {
        if (mobId == null || mobId.isEmpty()) {
            return;
        }
        if (members == null || members.isEmpty()) {
            return;
        }
        MobHistory mh = history.computeIfAbsent(mobId, k -> new MobHistory());
        ReentrantReadWriteLock.WriteLock wl = mh.lock.writeLock();
        wl.lock();
        KillGroup group;
        try {
            mh.nextKillIndex++;
            int killIndex = mh.nextKillIndex;
            List<String> copy = new ArrayList<>(members.size());
            for (String s : members) {
                if (s != null && !s.isEmpty()) {
                    copy.add(s);
                }
            }
            if (copy.isEmpty()) {
                return;
            }
            group = new KillGroup(killIndex, leader, copy);
            mh.groups.addLast(group);
            while (mh.groups.size() > maxPerMob) {
                mh.groups.pollFirst();
            }
        } finally {
            wl.unlock();
        }
        if (backend != null && group != null) {
            executor.submit(() -> backend.saveGroup(mobId, group));
        }
    }

    public KillGroup getKillGroup(String mobId, int killIndex) {
        if (mobId == null || mobId.isEmpty()) {
            return null;
        }
        if (killIndex <= 0) {
            return null;
        }
        MobHistory mh = history.get(mobId);
        if (mh == null) {
            return null;
        }
        ReentrantReadWriteLock.ReadLock rl = mh.lock.readLock();
        rl.lock();
        try {
            int total = mh.groups.size();
            if (total == 0) {
                return null;
            }
            int firstIndex = mh.nextKillIndex - total + 1;
            if (killIndex < firstIndex || killIndex > mh.nextKillIndex) {
                return null;
            }
            int offset = killIndex - firstIndex;
            int i = 0;
            for (KillGroup g : mh.groups) {
                if (i == offset) {
                    return g;
                }
                i++;
            }
            return null;
        } finally {
            rl.unlock();
        }
    }
}
