package com.fl.nx.distributelock;

import com.fl.nx.distributelock.common.PropertiesUtil;
import com.fl.nx.distributelock.exception.LockInitException;
import com.fl.nx.distributelock.exception.ZkConnectionException;
import com.fl.nx.distributelock.watcher.ConnectionWatcher;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.client.ZKClientConfig;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * zookeeper锁构造器
 */
public class ZkLockBuilder {

    private CountDownLatch connectionLatch = new CountDownLatch(1);

    private ZKClientConfig zkClientConfig;

    private Watcher watcher = new ConnectionWatcher(connectionLatch);

    private int sessionTimeout = PropertiesUtil.getZookeeperSessionTimeout();

    private int connectionTimeout = PropertiesUtil.getZookeeperConnectionTimeout();

    private long ttl = PropertiesUtil.getZookeeperLockTTL();

    private String lockKey;

    private boolean reentrant = false;

    public ZkLockBuilder() {
        this.zkClientConfig = new ZKClientConfig();
    }

    public ZkLockBuilder addSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
        return this;
    }

    public ZkLockBuilder addConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public ZkLockBuilder addLockKey(String lockKey) {
        this.lockKey = lockKey;
        return this;
    }

    public ZkLockBuilder addReentrant(boolean reentrant) {
        this.reentrant = reentrant;
        return this;
    }

    public ZkLockBuilder addLockTTL(long ttl) {
        this.ttl = ttl;
        return this;
    }

    public ZkLock newLock() throws ZkConnectionException {
        if (null == lockKey) {
            throw new LockInitException("No lockKey");
        }
        ZooKeeper zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper(PropertiesUtil.getZookeeperConnectionString(), this.sessionTimeout, this.watcher, this.zkClientConfig);
            if (!connectionLatch.await(connectionTimeout, TimeUnit.MILLISECONDS)) {
                throw new ZkConnectionException("Init zookeeper client, connect error ...");
            }
        } catch (Exception e) {
            throw new ZkConnectionException("Init zookeeper client, connect error ...", e);
        }
        return new ZkLock(zooKeeper, this.lockKey, this.ttl, this.reentrant);
    }
}
