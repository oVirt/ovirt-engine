package org.ovirt.engine.core.common.utils.ansible;

public class AnsibleRunnerCallException extends RuntimeException {

    public AnsibleRunnerCallException(String msg) {
        super(msg);
    }

    public AnsibleRunnerCallException(String msg, Throwable throwable) {
        super(String.format("%1$s %2$s", msg, throwable.getMessage()), throwable);
    }

    public AnsibleRunnerCallException(String msg, String error) {
        super(String.format(msg, error));
    }

    public AnsibleRunnerCallException(String msg, String status, String reason) {
        super(String.format(msg, status, reason));
    }

}
