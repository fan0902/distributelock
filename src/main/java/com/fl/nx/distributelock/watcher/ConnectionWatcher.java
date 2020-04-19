package com.fl.nx.distributelock.watcher;

import org.apache.zookeeper.*;

import java.util.concurrent.CountDownLatch;

public class ConnectionWatcher implements Watcher {

    private CountDownLatch connectionLatch;

    public ConnectionWatcher(CountDownLatch connectionLatch) {
        this.connectionLatch = connectionLatch;
    }

    @Override
    public void process(WatchedEvent event) {
        if (Event.KeeperState.SyncConnected == event.getState()) {
            this.connectionLatch.countDown();
        }
    }
}
