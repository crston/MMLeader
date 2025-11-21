package com.gmail.bobason01;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncDatabaseManager implements DatabaseManager {

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
        if (executorService == null || executorService.isShutdown()) {
            delegate.recordKill(mobId, partyMembers, partyLeader);
            return;
        }
        executorService.submit(() -> delegate.recordKill(mobId, partyMembers, partyLeader));
    }

    @Override
    public List<KillRecord> getKillRecords(String mobId) {
        return delegate.getKillRecords(mobId);
    }
}
