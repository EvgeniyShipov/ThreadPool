package ru.sbt.threadPool;


import java.util.ArrayDeque;
import java.util.Queue;

public class ScalableThreadPool implements ThreadPool {

    private volatile Queue<Runnable> tasks = new ArrayDeque<>();
    private final int minThread;
    private final int maxThread;
    private volatile int currentWorkedThread;

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

    public synchronized void execute(Runnable runnable) {
        tasks.add(runnable);
        if (currentWorkedThread >= minThread && currentWorkedThread < maxThread) {
            Thread thread = new Thread(() -> {
                try {
                    currentWorkedThread++;
                    Runnable poll = tasks.poll();
                    poll.run();
                } finally {
                    currentWorkedThread--;
                }
            });
            thread.start();
        } else notify();
    }

    public class Worker extends Thread {
        @Override
        public void run() {
            while (true) {
                if (!tasks.isEmpty()) {
                    currentWorkedThread++;
                    Runnable poll = tasks.poll();
                    poll.run();
                } else try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    currentWorkedThread--;
                }
            }
        }
    }
}
