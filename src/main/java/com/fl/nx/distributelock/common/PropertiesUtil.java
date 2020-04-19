package com.fl.nx.distributelock.common;


import com.fl.nx.distributelock.exception.PropertiesCheckException;
import com.fl.nx.distributelock.exception.PropertiesLoadException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertiesUtil {

    private static final String appId = "app.id";

    private static final String zookeeperConnectionString = "zookeeper.connectionString";

    private static final String zookeeperLockRootPath = "zookeeper.lockRootPath";

    private static final String zookeeperSessionTimeout = "zookeeper.sessionTimeout";

    private static final String zookeeperConnectionTimeout = "zookeeper.connectionTimeout";

    private static final String zookeeperLockTTL = "zookeeper.lockTTL";

    private static Properties properties;

    static {
        init();
    }

    private PropertiesUtil() {
    }

    static void init() {
        InputStream in = PropertiesUtil.class.getResourceAsStream("/lock.properties");
        if (null == in) {
            throw new PropertiesLoadException("Can not find lock property file");
        }
        properties = new Properties();
        try {
            properties.load(in);
            String errorPropertykey = checkErrorPropertyKey();
            if (null != errorPropertykey) {
                throw new PropertiesCheckException("Can not find necessary property " + errorPropertykey);
            }
        } catch (IOException e) {
            throw new PropertiesLoadException("lock property file load error", e);
        }
    }

    static String checkErrorPropertyKey() {
        if (!properties.containsKey(zookeeperConnectionString)) {
            return zookeeperConnectionString;
        }
        if (!properties.containsKey(zookeeperLockRootPath)) {
            return zookeeperLockRootPath;
        }
        return null;
    }

    public static String getZookeeperConnectionString() {
        return properties.getProperty(zookeeperConnectionString);
    }

    public static String getZookeeperLockRootPath() {
        return properties.getProperty(zookeeperLockRootPath);
    }

    public static long getZookeeperLockTTL() {
        return Long.valueOf(properties.getProperty(zookeeperLockTTL, "10"));
    }

    public static int getZookeeperSessionTimeout() {
        return Integer.valueOf(properties.getProperty(zookeeperSessionTimeout, "5000"));
    }

    public static int getZookeeperConnectionTimeout() {
        return Integer.valueOf(properties.getProperty(zookeeperConnectionTimeout, "3000"));
    }

    public static String getAppId() {
        return properties.getProperty(appId, "APP-" + System.currentTimeMillis());
    }
}
