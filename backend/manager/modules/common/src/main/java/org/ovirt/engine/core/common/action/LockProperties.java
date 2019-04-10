package org.ovirt.engine.core.common.action;

import java.io.Serializable;

/**
 * Defines the lock properties for the command, the value is used
 * to determine if the lock is released at the end of the command's
 * execution or not.
 */
public class LockProperties implements Serializable {


    private static final long serialVersionUID = -4444694059467965831L;
    private static final long NO_WAIT = -1L;
    private static final long WAIT_FOREVER = 0L;

    public static enum Scope {
        /**
         * Lock is released at the end of the command's execution
         */
        Execution,
        /**
         * Lock is not released at the of the command's execution, used when
         * a child command is using the lock of the parent. The child command should
         * not release the lock, the parent will take care of releasing it
         */
        Command,
        /**
         * No lock is required for the command's execution
         */
        None
    }

    /**
     * The scope of the command's lock
     */
    private Scope scope = Scope.None;

    /**
     * <pre>
     * Policy for acquiring the lock. The values of this field signify:
     *    -2 or less: undefined
     *    NO_WAIT (-1): if lock is not acquired on the first trial, do not try again and return a failure message.
     *    WAIT_FOREVER(0): try to acquire the lock indefinitely.
     *    1 or more: try to acquire the lock until the timeout value (milliseconds) is reached.
     * </pre>
     */
    private long timeoutMillis = WAIT_FOREVER;

    private LockProperties() {}

    public boolean isNoWait() {
        return timeoutMillis == NO_WAIT;
    }

    public boolean isWaitForever() {
        return timeoutMillis == WAIT_FOREVER;
    }

    public boolean isTimeout() {
        return timeoutMillis > 0;
    }

    public LockProperties withNoWait() {
        return withTimeout(NO_WAIT);
    }

    public LockProperties withWaitForever() {
        return withTimeout(WAIT_FOREVER);
    }

    public LockProperties withWaitTimeout(long timeoutMillis) {
        return withTimeout(timeoutMillis);
    }

    private LockProperties withTimeout(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public LockProperties withScope(Scope scope) {
        this.scope = scope;
        return this;
    }

    public Scope getScope() {
        return scope;
    }

    public static LockProperties create(Scope scope) {
        return new LockProperties().withScope(scope);
    }

}
