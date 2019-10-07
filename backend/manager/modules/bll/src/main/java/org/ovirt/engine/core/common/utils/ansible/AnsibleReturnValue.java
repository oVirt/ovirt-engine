/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.core.common.utils.ansible;

import java.nio.file.Path;

/**
 * A class returned by AnsibleExecutor::runCommand. It stores the return code of the ansible-playbook execution
 * and it may store the stdout of the execution.
 */
public class AnsibleReturnValue {

    /**
     * Is set to return code which is returned by ansible-playbook command.
     */
    private AnsibleReturnCode ansibleReturnCode;

    /**
     * Stdout of playbook execution. It is set only if user is using custom stdout callback plugin.
     */
    private String stdout;

    /**
     * Stderr of playbook execution. It is set only if user is using custom stdout callback plugin.
     */
    private String stderr;

    /**
     * Log file of playbook execution. It is only set if user enabled logging
     */
    private Path logFile;

    public AnsibleReturnValue(AnsibleReturnCode ansibleReturnCode) {
        this(ansibleReturnCode, null);
    }

    public AnsibleReturnValue(AnsibleReturnCode ansibleReturnCode, String stdout) {
        this.ansibleReturnCode = ansibleReturnCode;
        this.stdout = stdout;
    }

    public AnsibleReturnCode getAnsibleReturnCode() {
        return ansibleReturnCode;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public Path getLogFile() {
        return logFile;
    }

    public void setAnsibleReturnCode(AnsibleReturnCode ansibleReturnCode) {
        this.ansibleReturnCode = ansibleReturnCode;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public void setLogFile(Path logFile) {
        this.logFile = logFile;
    }
}
