package com.adam;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Scheduler {
    private ExecutorService executor = Executors.newCachedThreadPool();

    public void schedule(Runnable runnable) {
        executor.submit(runnable);
    }
}
