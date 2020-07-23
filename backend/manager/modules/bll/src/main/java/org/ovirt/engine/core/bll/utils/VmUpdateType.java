package org.ovirt.engine.core.bll.utils;

public enum VmUpdateType {

    MEMORY("memory"),
    CPU_TOPOLOGY("CPU topology"),
    PROPERTIES("properties"),
    MIGRATION_SUPPORT("migration support"),
    MIGRATION_POLICY("migration policy"),
    BIOS_TYPE("BIOS type"),
    DEFAULT_DISPLAY_TYPE("default display type"),
    RNG_DEVICE("RNG device"),
    CHIPSET("Chipset");

    private String displayName;

    VmUpdateType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
