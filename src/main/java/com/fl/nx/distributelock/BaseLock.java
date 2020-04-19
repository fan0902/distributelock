package com.fl.nx.distributelock;

import com.fl.nx.distributelock.common.Constants;
import com.fl.nx.distributelock.exception.LockException;

import java.util.HashSet;

public abstract class BaseLock {

    protected String key;

    protected byte[] value;

    protected long ttl;

    protected boolean reentrant;

    protected static final ThreadLocal<HashSet<String>> holdLocks = new ThreadLocal() {
        @Override
        protected HashSet<String> initialValue() {
            return new HashSet<>();
        }
    };

    public BaseLock(String key, long ttl, boolean reentrant) {
        this.key = key;
        this.value = Constants.LOCK_DEFAULT_VAL;
        this.ttl = ttl;
        this.reentrant = reentrant;
    }

    public String getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }

    public long getTtl() {
        return ttl;
    }

    public boolean isReentrant() {
        return reentrant;
    }

    /**
     * 非阻塞竟锁
     *
     * @return
     */
    protected abstract boolean tryLock();

    /**
     * 非阻塞竟锁
     *
     * @param retryTimes 自动重试次数
     * @param interval   重试间隔
     * @return
     */
    protected abstract boolean tryLock(int retryTimes, long interval);

    /**
     * 阻塞竟锁
     */
    protected abstract boolean lock();

    protected abstract boolean doLock() throws LockException;

    protected abstract boolean unlock();

}
