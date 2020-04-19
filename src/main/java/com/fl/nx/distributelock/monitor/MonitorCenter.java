package com.fl.nx.distributelock.monitor;


import com.fl.nx.distributelock.ZkLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 模拟监控中心
 */
public class MonitorCenter {

    private static Logger logger = LoggerFactory.getLogger(MonitorCenter.class);

    private static final Map<String, MonitorLockData> monitorDatas = new HashMap<>();

    private static MonitorThread monitorThread = new MonitorThread();

    /**
     * 上报命令
     */
    public enum ReportCmd {

        ACQUIRE_LOCK, RELEASE_LOCK

    }

    public static void report(ReportCmd reportCmd, String bId, ZkLock zkLock, long startTime) {
        String storageKey = zkLock.getKey() + "-" + zkLock.getCurLockPath();
        if (reportCmd == ReportCmd.ACQUIRE_LOCK) {
            MonitorLockData monitorLockData = new MonitorLockData();
            monitorLockData.setbId(bId);
            monitorLockData.setLockKey(zkLock.getKey());
            monitorLockData.setLockPath(zkLock.getCurLockPath());
            monitorLockData.setStartTime(startTime);
            monitorLockData.setTtl(zkLock.getTtl());
            monitorDatas.put(storageKey, monitorLockData);
        } else if (reportCmd == ReportCmd.RELEASE_LOCK) {
            monitorDatas.remove(storageKey);
        }
        if (monitorThread.getIsRunning() == 0) {
            monitorThread.setIsRunning(1);
            monitorThread.start();
        } else if (monitorThread.getIsRunning() < 0) {
            monitorThread = new MonitorThread();
            monitorThread.setIsRunning(1);
            monitorThread.start();
        }
    }

    public static class MonitorLockData {

        private String bId;

        private String lockKey;

        private String lockPath;

        private long startTime;

        private long ttl;

        public String getbId() {
            return bId;
        }

        public void setbId(String bId) {
            this.bId = bId;
        }

        public String getLockKey() {
            return lockKey;
        }

        public void setLockKey(String lockKey) {
            this.lockKey = lockKey;
        }

        public String getLockPath() {
            return lockPath;
        }

        public void setLockPath(String lockPath) {
            this.lockPath = lockPath;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getTtl() {
            return ttl;
        }

        public void setTtl(long ttl) {
            this.ttl = ttl;
        }

        @Override
        public String toString() {
            return "MonitorLockData{" +
                    "bId='" + bId + '\'' +
                    ", lockKey='" + lockKey + '\'' +
                    ", lockPath='" + lockPath + '\'' +
                    ", startTime=" + startTime +
                    ", ttl=" + ttl +
                    '}';
        }
    }

    public static class MonitorThread extends Thread {

        private volatile int isRunning = 0;

        public MonitorThread() {
            this.setDaemon(true);
            this.setName("MonitorThread");
        }

        public int getIsRunning() {
            return isRunning;
        }

        public void setIsRunning(int isRunning) {
            this.isRunning = isRunning;
        }

        @Override
        public void run() {
            for (; isRunning == 1; ) {
                long currentTime = System.currentTimeMillis();
                for (Map.Entry<String, MonitorLockData> entry : monitorDatas.entrySet()) {
                    long startTime = entry.getValue().getStartTime();
                    long ttl = entry.getValue().getTtl();
                    if (currentTime > startTime + ttl) {
                        logger.error("{} find abnormal lock {}", Thread.currentThread().getName(), entry.getValue());
                    }
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    isRunning = -1;
                }
            }
        }
    }
}
