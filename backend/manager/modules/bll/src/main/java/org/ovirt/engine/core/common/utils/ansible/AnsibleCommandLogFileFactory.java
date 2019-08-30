package org.ovirt.engine.core.common.utils.ansible;

import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Date;

import javax.inject.Singleton;

import org.ovirt.engine.core.utils.EngineLocalConfig;

@Singleton
public class AnsibleCommandLogFileFactory {

    private EngineLocalConfig config;

    private Clock clock;

    public AnsibleCommandLogFileFactory() {
        config = EngineLocalConfig.getInstance();
        clock = Clock.systemDefaultZone();
    }

    /**
     * The logFile is set up to:
     *
     * /var/log/ovirt-engine/${logDirectory:ansible}/
     * ${logFilePrefix:ansible}-${timestamp}-${logFileName:playbook}[-${logFileSuffix}].log
     */
    public File create(LogFileConfig logFileConfig) {
        File logFile = null;
        String logFileDirectory = logFileConfig.logFileDirectory();
        String logFileName = logFileConfig.logFileName();
        String logFilePrefix = logFileConfig.logFilePrefix();
        String logFileSuffix = logFileConfig.logFileSuffix();
        String playbook = logFileConfig.playbook();
        if (logFileConfig.enableLogging()) {

            logFile = Paths.get(
                    config.getLogDir().toString(),
                    logFileDirectory != null ? logFileDirectory : AnsibleExecutor.DEFAULT_LOG_DIRECTORY,
                    String.format(
                            "%1$s-%2$s-%3$s%4$s.log",
                            logFilePrefix != null ? logFilePrefix : "ansible",
                            new SimpleDateFormat("yyyyMMddHHmmss")
                                    .format(Date.from(clock.instant())),
                            logFileName != null ? logFileName
                                    : playbook.substring(playbook.lastIndexOf('/') + 1).replace('.', '_'),
                            logFileSuffix != null ? "-" + logFileSuffix : ""))
                    .toFile();
        }

        return logFile;
    }

    // visible for testing
    void setClock(Clock clock) {
        this.clock = clock;
    }
}
