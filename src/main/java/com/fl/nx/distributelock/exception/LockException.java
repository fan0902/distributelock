package com.fl.nx.distributelock.exception;

/**
 * 锁异常
 */
public class LockException extends Exception{

    public LockException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockException(String message) {
        super(message);
    }
}
