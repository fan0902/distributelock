package com.fl.nx.distributelock.test;


import com.fl.nx.distributelock.ZkLock;
import com.fl.nx.distributelock.ZkLockBuilder;
import com.fl.nx.distributelock.exception.ZkConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class InterruptedThreadTest {

    private static Logger logger = LoggerFactory.getLogger(InterruptedThreadTest.class);

    public static void main(String[] args) throws InterruptedException {

        Thread getLockThread = new Thread(() -> {
            ZkLockBuilder zkLockBuilder = new ZkLockBuilder();
            try {
                ZkLock zkLock = zkLockBuilder.addLockKey("LockKey-").addLockTTL(60 * 1000L).newLock();
                zkLock.lock();
                TimeUnit.SECONDS.sleep(30);
                zkLock.unlock();
            } catch (ZkConnectionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        getLockThread.setName("GetLockThread");

        Thread contentLockThread = new Thread(() -> {
            ZkLockBuilder zkLockBuilder = new ZkLockBuilder();
            try {
                ZkLock zkLock = zkLockBuilder.addLockKey("LockKey-").addLockTTL(60 * 1000L).newLock();
                if (!zkLock.lock()) {
                    logger.warn("{} is interrupted and remove {}", Thread.currentThread().getName(), zkLock.getCurLockPath());
                }
            } catch (ZkConnectionException e) {
                e.printStackTrace();
            }
        });
        contentLockThread.setName("ContentLockThread");

        Thread interruptedThread = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            contentLockThread.interrupt();
        });
        interruptedThread.setName("InterruptedThread");

        getLockThread.start();
        interruptedThread.start();
        TimeUnit.MILLISECONDS.sleep(2000);
        contentLockThread.start();

    }
}
