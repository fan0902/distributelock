package com.fl.nx.distributelock.exception;

/**
 * 配置加载异常
 */
public class PropertiesLoadException extends RuntimeException {

    public PropertiesLoadException(String message) {
        super(message);
    }

    public PropertiesLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
