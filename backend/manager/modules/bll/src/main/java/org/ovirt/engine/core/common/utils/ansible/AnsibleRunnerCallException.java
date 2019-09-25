package org.ovirt.engine.core.common.utils.ansible;

public class AnsibleRunnerCallException extends RuntimeException {

    public AnsibleRunnerCallException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

    public AnsibleRunnerCallException(String msg, String status, String reason) {
        super(String.format(msg, status, reason));
    }

}
