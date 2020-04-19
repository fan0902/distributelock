package com.fl.nx.distributelock.exception;

/**
 * 锁初始化异常
 */
public class LockInitException extends RuntimeException{

    public LockInitException(String message) {
        super(message);
    }
}
