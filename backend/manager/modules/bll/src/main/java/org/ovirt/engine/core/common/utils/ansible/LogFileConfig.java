package org.ovirt.engine.core.common.utils.ansible;

/**
 * Read-only ansible command log file configuration view
 */
public interface LogFileConfig extends PlaybookConfig {

    boolean enableLogging();

    String logFileDirectory();

    String logFileName();

    String logFilePrefix();

    String logFileSuffix();
}
