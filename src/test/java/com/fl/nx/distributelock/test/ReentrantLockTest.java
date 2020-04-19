package com.fl.nx.distributelock.test;

import com.fl.nx.distributelock.ZkLock;
import com.fl.nx.distributelock.ZkLockBuilder;
import com.fl.nx.distributelock.exception.ZkConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 可重入测试
 * 1. 线程1抢锁成功，可重入
 * 2. 线程2抢锁失败，等待
 */
public class ReentrantLockTest {

    private static Logger logger = LoggerFactory.getLogger(ReentrantLockTest.class);

    public static void main(String[] args) throws InterruptedException {

        Thread getLockThread1 = new Thread(() -> {
            ZkLockBuilder zkLockBuilder = new ZkLockBuilder();
            try {
                ZkLock zkLock = zkLockBuilder.addLockKey("LockKey-").addReentrant(true).newLock();
                for (int i = 0; i < 3; i++) {
                    zkLock.lock();
                    logger.info("{} Get Lock {} times", Thread.currentThread().getName(), i);
                }
                zkLock.unlock();
            } catch (ZkConnectionException e) {
                e.printStackTrace();
            }
        });
        Thread getLockThread2 = new Thread(() -> {
            ZkLockBuilder zkLockBuilder = new ZkLockBuilder();
            try {
                ZkLock zkLock = zkLockBuilder.addLockKey("LockKey-").addReentrant(true).newLock();
                zkLock.lock();
                logger.info("{} Get Lock 1 time", Thread.currentThread().getName());
                TimeUnit.SECONDS.sleep(1);
                zkLock.unlock();
            } catch (ZkConnectionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        getLockThread1.start();
        getLockThread2.start();
        getLockThread1.join();
        getLockThread2.join();
    }
}
