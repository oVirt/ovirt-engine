package org.ovirt.engine.core.common.action;

import java.io.Serializable;

/**
 * Defines the lock properties for the command, the value is used
 * to determine if the lock is released at the end of the command's
 * execution or not.
 */
public class LockProperties implements Serializable {


    private static final long serialVersionUID = -4444694059467965831L;

    public static enum Scope {
        /**
         * Lock is released at the end of the command's execution
         */
        Execution,
        /**
         * Lock is not released at the the of the command's execution, used when
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
     * Wait until the lock is acquired
     */
    private boolean wait = true;

    private LockProperties() {}

    public boolean isWait() {
        return wait;
    }

    public LockProperties withWait(boolean wait) {
        this.wait = wait;
        return this;
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
