package org.ovirt.engine.core.common.utils.ansible;

public interface LogFileConfig extends PlaybookConfig {

    boolean enableLogging();

    String logFileDirectory();

    String logFileName();

    String logFilePrefix();

    String logFileSuffix();
}
