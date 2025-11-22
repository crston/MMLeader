package com.gmail.bobason01;

import com.gmail.bobason01.kill.KillRecord;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AsyncDatabaseManager implements DatabaseManager {

    private final DatabaseManager delegate;
    private ExecutorService executorService;

    public AsyncDatabaseManager(DatabaseManager delegate) {
        this.delegate = delegate;
    }

    @Override
    public synchronized void init() {
        delegate.init();
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newSingleThreadExecutor();
        }
    }

    @Override
    public synchronized void close() {
        try {
            if (executorService != null) {
                executorService.shutdown();
            }
        } catch (Throwable ignored) {
        }
        delegate.close();
    }

    @Override
    public void recordKill(String mobId, List<String> partyMembers, String partyLeader) {
        ExecutorService exec = executorService;
        if (exec == null || exec.isShutdown()) {
            delegate.recordKill(mobId, partyMembers, partyLeader);
            return;
        }
        exec.submit(() -> delegate.recordKill(mobId, partyMembers, partyLeader));
    }

    @Override
    public List<KillRecord> getKillRecords(String mobId) {
        return delegate.getKillRecords(mobId);
    }
}
