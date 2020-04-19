package com.fl.nx.distributelock.test;


import com.fl.nx.distributelock.ZkLock;
import com.fl.nx.distributelock.ZkLockBuilder;
import com.fl.nx.distributelock.exception.ZkConnectionException;

/**
 * 锁构造异常测试
 */
public class ZkLockBuilderExceptionTest {

    public static void main(String[] args) {
        ZkLockBuilder zkLockBuilder = new ZkLockBuilder();
        try {
            ZkLock zkLock = zkLockBuilder.newLock();
        } catch (ZkConnectionException e) {
            e.printStackTrace();
        }
    }
}
