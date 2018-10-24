package org.ovirt.engine.core.aaa;

public enum CreateUserSessionsError {
    /**
     * User is not authorized to login
     */
    USER_NOT_AUTHORIZED,

    /**
     * Maximum number of sessions exceeded
     */
    NUM_OF_SESSIONS_EXCEEDED;
}
