package com.fl.nx.distributelock.exception;

/**
 * zookeeper连接异常
 */
public class ZkConnectionException extends Exception {

    public ZkConnectionException(String message) {
        super(message);
    }

    public ZkConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
