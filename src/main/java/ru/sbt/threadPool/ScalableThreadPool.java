package ru.sbt.threadPool;


import java.util.ArrayDeque;
import java.util.Queue;

public class ScalableThreadPool implements ThreadPool {

    private volatile Queue<Runnable> tasks = new ArrayDeque<>();
    private volatile int currentWorkedThread;
    private final Object lock = new Object();
    private final int minThread;
    private final int maxThread;

    public ScalableThreadPool(int minThread, int maxThread) {
        this.minThread = minThread;
        this.maxThread = maxThread;
    }

    @Override
    public void start() {
        for (int i = 0; i < minThread; i++) {
            new ScalableThreadPool.Worker().start();
        }
    }

    public void execute(Runnable runnable) {
        synchronized (lock) {
            tasks.add(runnable);
            if (currentWorkedThread >= minThread && currentWorkedThread < maxThread) {
                Thread thread = new Thread(() -> {
                    try {
                        currentWorkedThread++;
                        while (!tasks.isEmpty()) {
                            Runnable poll = tasks.poll();
                            poll.run();
                        }
                    } finally {
                        currentWorkedThread--;
                    }
                });
                thread.start();
            } else lock.notify();
        }
    }

    public class Worker extends Thread {
        @Override
        public void run() {
            while (true) {
                synchronized (lock) {
                    if (!tasks.isEmpty()) {
                        currentWorkedThread++;
                        Runnable poll = tasks.poll();
                        poll.run();
                    } else try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        currentWorkedThread--;
                    }
                }
            }
        }
    }
}
