package com.gmail.bobason01.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AsyncExecutor {

    private final ExecutorService executor;

    public AsyncExecutor(int threads) {
        if (threads <= 0) {
            threads = 1;
        }
        this.executor = Executors.newFixedThreadPool(threads);
    }

    public CompletableFuture<Void> runAsync(Runnable task) {
        return CompletableFuture.runAsync(task, executor);
    }

    public <T> CompletableFuture<T> supplyAsync(java.util.concurrent.Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    public void shutdown() {
        executor.shutdown();
    }
}
