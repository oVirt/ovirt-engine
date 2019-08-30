package org.ovirt.engine.core.common.utils.ansible;

import java.io.File;
import java.util.List;

public class AnsibleCommand {
    private final List<String> command;
    private final File logFile;

    public AnsibleCommand(List<String> command, File logFile) {
        this.command = command;
        this.logFile = logFile;
    }

    public List<String> getCommand() {
        return command;
    }

    public File getLogFile() {
        return logFile;
    }
}
