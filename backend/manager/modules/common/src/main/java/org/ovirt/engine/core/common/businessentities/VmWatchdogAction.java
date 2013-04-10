package org.ovirt.engine.core.common.businessentities;

public enum VmWatchdogAction {
    NONE,
    RESET,
    POWEROFF,
    DUMP,
    PAUSE;

    public static VmWatchdogAction getByName(String name) {
        if (name == null || name.length() == 0) {
            return null;
        } else {
            for (VmWatchdogAction vmWatchdogAction : VmWatchdogAction.values()) {
                if (vmWatchdogAction.name().equalsIgnoreCase(name)) {
                    return vmWatchdogAction;
                }
            }
        }
        return null;
    }

}
