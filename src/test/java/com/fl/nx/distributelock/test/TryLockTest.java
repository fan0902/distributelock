package com.fl.nx.distributelock.test;

import com.fl.nx.distributelock.ZkLock;
import com.fl.nx.distributelock.ZkLockBuilder;
import com.fl.nx.distributelock.exception.ZkConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TryLockTest {

    private static Logger logger = LoggerFactory.getLogger(TryLockTest.class);

    public static void main(String[] args) throws ZkConnectionException, InterruptedException {

        CountDownLatch waitLatch = new CountDownLatch(1);

        Thread getLockThread = new Thread(() -> {
            ZkLockBuilder zkLockBuilder = new ZkLockBuilder();
            try {
                ZkLock zkLock = zkLockBuilder.addLockKey("LockKey-").addLockTTL(60 * 1000).newLock();
                zkLock.lock();
                waitLatch.countDown();
                TimeUnit.SECONDS.sleep(10);
                zkLock.unlock();
            } catch (ZkConnectionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        getLockThread.setName("GetLockThread");
        getLockThread.start();

        waitLatch.await();
        ZkLockBuilder zkLockBuilder = new ZkLockBuilder();
        ZkLock zkLock = zkLockBuilder.addLockKey("LockKey-").addLockTTL(60 * 1000).newLock();
        boolean result = zkLock.tryLock(5, 1000);
        logger.info("TryLock 5 times, result: " + result);
    }
}
