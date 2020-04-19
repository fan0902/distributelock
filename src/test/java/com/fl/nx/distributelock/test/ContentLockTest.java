package com.fl.nx.distributelock.test;

import com.fl.nx.distributelock.ZkLock;
import com.fl.nx.distributelock.ZkLockBuilder;
import com.fl.nx.distributelock.exception.ZkConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 竟锁测试
 */
public class ContentLockTest {

    private static Logger logger = LoggerFactory.getLogger(ContentLockTest.class);

    public static void main(String[] args) throws InterruptedException {

        Thread contentLock1 = new Thread(() -> {
            ZkLockBuilder zkLockBuilder = new ZkLockBuilder();
            try {
                ZkLock zkLock = zkLockBuilder.addLockKey("ContentLock").newLock();
                zkLock.lock();
                TimeUnit.SECONDS.sleep(5);
                zkLock.unlock();
            } catch (ZkConnectionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread contentLock2 = new Thread(() -> {
            ZkLockBuilder zkLockBuilder = new ZkLockBuilder();
            try {
                ZkLock zkLock = zkLockBuilder.addLockKey("ContentLock").newLock();
                zkLock.lock();
                TimeUnit.SECONDS.sleep(5);
                zkLock.unlock();
            } catch (ZkConnectionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        contentLock1.start();
        TimeUnit.MILLISECONDS.sleep(20);
        contentLock2.start();

        contentLock1.join();
        contentLock2.join();
    }
}
