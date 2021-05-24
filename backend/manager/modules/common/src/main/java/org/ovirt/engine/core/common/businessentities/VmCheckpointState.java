package org.ovirt.engine.core.common.businessentities;

import java.util.Arrays;

public enum VmCheckpointState {

    CREATED("Created"),
    INVALID("Invalid");

    private String name;

    VmCheckpointState(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static VmCheckpointState forName(String name) {
        return Arrays.stream(values()).filter(val -> val.name.equals(name)).findAny().orElse(null);
    }
}
