package org.ovirt.engine.core.common.businessentities;

public enum ConsoleDisconnectAction {
    NONE,
    LOCK_SCREEN,
    LOGOUT,
    SHUTDOWN,
    REBOOT;

    public static ConsoleDisconnectAction fromString(String name) {
        if(name == null) {
            return LOCK_SCREEN;
        }
        return ConsoleDisconnectAction.valueOf(name);
    }
}
