package com.apitest.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigManager — centralises all environment configuration.
 * Loads from config.properties; individual keys can be
 * overridden via system properties (e.g. -Dbase.url=...).
 */
public class ConfigManager {

    private static final Logger log = LogManager.getLogger(ConfigManager.class);
    private static final Properties props = new Properties();
    private static ConfigManager instance;

    private ConfigManager() {
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (is == null) throw new RuntimeException("config.properties not found on classpath");
            props.load(is);
            log.info("Configuration loaded successfully");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) instance = new ConfigManager();
        return instance;
    }

    /** Returns the value, preferring a JVM system property over the file value. */
    public String get(String key) {
        return System.getProperty(key, props.getProperty(key));
    }

    public String getBaseUrl()       { return get("base.url"); }
    public String getApiVersion()    { return get("api.version"); }
    public int    getTimeout()       { return Integer.parseInt(get("timeout.seconds")); }
    public String getAdminToken()    { return get("auth.admin.token"); }
    public String getUserToken()     { return get("auth.user.token"); }
    public String getInvalidToken()  { return get("auth.invalid.token"); }
    public String getEnvironment()   { return get("environment"); }

    /** Full base path: baseUrl + /apiVersion */
    public String getBasePath() {
        return getBaseUrl() + "/" + getApiVersion();
    }
}
