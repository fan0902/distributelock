package com.fl.nx.distributelock;

import com.fl.nx.distributelock.common.Constants;
import com.fl.nx.distributelock.common.PropertiesUtil;
import com.fl.nx.distributelock.exception.LockException;
import com.fl.nx.distributelock.exception.LockInitException;
import com.fl.nx.distributelock.monitor.MonitorCenter;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ZkLock extends BaseLock implements Watcher {

    private static final Logger logger = LoggerFactory.getLogger(ZkLock.class);

    private ZooKeeper zooKeeper;

    private CountDownLatch waitLatch;

    private String curLockPath;

    private String waitLockPath;

    private final AtomicBoolean hold = new AtomicBoolean(false);

    public String getCurLockPath() {
        return curLockPath;
    }

    public String getWaitLockPath() {
        return waitLockPath;
    }

    ZkLock(ZooKeeper zooKeeper, String key, long ttl, boolean reentrant) {
        super(key, ttl, reentrant);
        this.zooKeeper = zooKeeper;
        try {
            if (null == zooKeeper.exists(PropertiesUtil.getZookeeperLockRootPath(), true)) {
                if (null == zooKeeper.create(PropertiesUtil.getZookeeperLockRootPath(), Constants.LOCK_DEFAULT_VAL, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)) {
                    throw new LockInitException("Init Lock root path error ...");
                }
            }
        } catch (Exception e) {
            throw new LockInitException("Init Lock root path error ...");
        }
    }

    private boolean doWaiting() {
        try {
            String waitLockPath = PropertiesUtil.getZookeeperLockRootPath() + "/" + this.waitLockPath;
            Stat stat = zooKeeper.exists(waitLockPath, this);
            if (null != stat) {
                this.waitLatch = new CountDownLatch(1);
                logger.warn("{} do waiting at {}", Thread.currentThread().getName(), waitLockPath);
                this.waitLatch.await();
                this.waitLatch = null;
                this.waitLockPath = null;
                logger.info("{} Get Lock", Thread.currentThread().getName());
                return true;
            }
            // 如果没有等待的节点, 重新竟锁
            else {
                return doLock();
            }
        } catch (Exception e) {
            this.waitLatch = null;
            this.waitLockPath = null;
            return close();
        }
    }

    @Override
    public boolean tryLock() {
        return tryLock(0, -1);
    }

    @Override
    public boolean tryLock(int retryTimes, long interval) {
        try {
            if (reentrant && holdLocks.get().contains(super.key)) {
                return true;
            }
            List<String> existedLockPathList = zooKeeper.getChildren(PropertiesUtil.getZookeeperLockRootPath(), false);
            if (existedLockPathList != null && existedLockPathList.size() > 0) {
                if (retryTimes > 0 && interval > 0) {
                    TimeUnit.MILLISECONDS.sleep(interval);
                    retryTimes = retryTimes - 1;
                    logger.info("{} tryLock, remain {} times", Thread.currentThread().getName(), retryTimes);
                    return tryLock(retryTimes, interval);
                }
                return false;
            }
            return doLock();
        } catch (Exception e) {
            logger.error("TryLock error", e);
            if (retryTimes > 0 && interval > 0) {
                try {
                    TimeUnit.MILLISECONDS.sleep(interval);
                    return tryLock(--retryTimes, interval);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    public boolean lock() {
        if (reentrant && holdLocks.get().contains(super.key)) {
            return true;
        }
        try {
            if (!doLock()) {
                return doWaiting();
            }
        }
        // doLock()异常
        catch (LockException e) {

            return close();
        }
        return true;
    }

    @Override
    protected boolean doLock() throws LockException {
        try {
            String lockKey = PropertiesUtil.getZookeeperLockRootPath() + "/" + super.key;
            String actualPath = zooKeeper.create(lockKey, super.value, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            if (null != actualPath) {
                this.curLockPath = actualPath;
                List<String> existedLockPathList = zooKeeper.getChildren(PropertiesUtil.getZookeeperLockRootPath(), false);
                if (null != existedLockPathList && existedLockPathList.size() > 0) {
                    Collections.sort(existedLockPathList);
                    if (actualPath.equals(PropertiesUtil.getZookeeperLockRootPath() + "/" + existedLockPathList.get(0))) {
                        logger.info("{} Get Lock success, lockList: {}", Thread.currentThread().getName(), existedLockPathList);
                        hold.set(true);
                        if (reentrant) {
                            holdLocks.get().add(super.key);
                        }
                        MonitorCenter.report(MonitorCenter.ReportCmd.ACQUIRE_LOCK, PropertiesUtil.getAppId(), this, System.currentTimeMillis());
                        return true;
                    }
                    int index = existedLockPathList.indexOf(actualPath.split("/")[2]);
                    this.waitLockPath = existedLockPathList.get(index - 1);
                    logger.warn("{} Get Lock fail, lockList: {}, waitPath: {}", Thread.currentThread().getName(), existedLockPathList, waitLockPath);
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            close();
            throw new LockException("Get Lock error", e);
        }
    }

    @Override
    public boolean unlock() {
        try {
            if (this.hold.get() && this.curLockPath != null) {
                try {
                    //此处保证最终上报
                    zooKeeper.delete(this.curLockPath, -1);
                    MonitorCenter.report(MonitorCenter.ReportCmd.RELEASE_LOCK, PropertiesUtil.getAppId(), this, System.currentTimeMillis());
                } finally {
                    this.hold.set(false);
                    holdLocks.get().remove(super.key);
                }
            }
            logger.info("{} unlock {}", Thread.currentThread().getName(), this.curLockPath);
            return true;
        } catch (Exception e) {
            logger.error("zk delete error", e);
            return close();
        }
    }

    boolean close() {
        try {
            //此处保证最终上报
            zooKeeper.close();
            MonitorCenter.report(MonitorCenter.ReportCmd.RELEASE_LOCK, PropertiesUtil.getAppId(), this, System.currentTimeMillis());
            return true;
        } catch (Exception ex) {
            logger.error("zk client close error", ex);
            return false;
        }
    }

    @Override
    public void process(WatchedEvent event) {
        if (Event.EventType.NodeDeleted == event.getType()) {
            if (this.waitLatch != null) {
                this.waitLatch.countDown();
            }
        }
    }
}
