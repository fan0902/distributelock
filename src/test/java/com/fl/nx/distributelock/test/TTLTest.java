package com.fl.nx.distributelock.test;


import com.fl.nx.distributelock.ZkLock;
import com.fl.nx.distributelock.ZkLockBuilder;
import com.fl.nx.distributelock.exception.ZkConnectionException;

import java.util.concurrent.TimeUnit;

/**
 * 锁上报监控中心，超过TTL未释放报警测试
 */
public class TTLTest {

    public static void main(String[] args) {

        ZkLockBuilder zkLockBuilder = new ZkLockBuilder();
        try {
            ZkLock zkLock = zkLockBuilder.addLockKey("TTLLock").addLockTTL(5000L).newLock();
            zkLock.lock();
            TimeUnit.MILLISECONDS.sleep(10 * 1000);
        } catch (ZkConnectionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
