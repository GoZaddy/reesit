package com.example.reesit.misc;

import com.example.reesit.utils.ReesitCallback;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Debouncer {
    private static final ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
    private static final ConcurrentHashMap<String, TimerTask> delayedMap = new ConcurrentHashMap<String, TimerTask>();


    public static void call(String key, ReesitCallback c,  int interval) {
        TimerTask task = new TimerTask(key, c, interval);

        TimerTask prev;

        do {
            prev = delayedMap.putIfAbsent(key, task);
            // if there was no task in the map before - schedule one
            if (prev == null) {
                sched.schedule(task, interval, TimeUnit.MILLISECONDS);
            }
            // if there was a task, extend it and if it wasn't extended successfully, schedule a new one
        } while (prev != null && !prev.extend()); // Exit only if new task was added to map, or existing task was extended successfully
    }

    public static void terminate() {
        sched.shutdownNow();
    }

    // The task that wakes up when the wait time elapses
    private static class TimerTask implements Runnable {
        private final String key;
        private long dueTime;
        private final ReesitCallback callback;
        private final int interval;
        private final Object lock = new Object();

        public TimerTask(String key, ReesitCallback c, int interval) {
            this.key = key;
            this.callback = c;
            this.interval = interval;
            extend();
        }

        // returns true is task's time was successfully extended
        public boolean extend() {
            synchronized (lock) {
                if (dueTime < 0) // Task has been shutdown
                    return false;
                dueTime = System.currentTimeMillis() + interval;
                return true;
            }
        }

        public void run() {
            synchronized (lock) {
                long remaining = dueTime - System.currentTimeMillis();
                if (remaining > 0) { // Re-schedule task
                    sched.schedule(this, remaining, TimeUnit.MILLISECONDS);
                } else { // Mark as terminated and invoke callback
                    dueTime = -1;
                    try {
                        callback.run();
                    } finally {
                        delayedMap.remove(key);
                    }
                }
            }
        }
    }
}

