package ru.sbt.threadPool;


import java.util.ArrayDeque;
import java.util.Queue;

public class FixedThreadPool implements ThreadPool {

    private volatile Queue<Runnable> tasks = new ArrayDeque<>();
    private final Object lock = new Object();
    private final int threadCount;

    public FixedThreadPool(int threadCount) {
        this.threadCount = threadCount;
    }

    @Override
    public void start() {
        for (int i = 0; i < threadCount; i++) {
            new Worker().start();
        }
    }

    @Override
    public void execute(Runnable runnable) {
        synchronized (lock) {
            tasks.add(runnable);
            lock.notify();
        }
    }

    public class Worker extends Thread {
        @Override
        public void run() {
            while (true) {
                synchronized (lock) {
                    if (!tasks.isEmpty()) {
                        Runnable poll = tasks.poll();
                        poll.run();
                    } else try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
