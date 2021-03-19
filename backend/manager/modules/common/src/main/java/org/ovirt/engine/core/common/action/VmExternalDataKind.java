package org.ovirt.engine.core.common.action;

public enum VmExternalDataKind {
    TPM("tpm"),
    NVRAM("nvram");

    private final String value;

    private VmExternalDataKind(String value) {
        this.value = value;
    }

    /**
     * Return the external representation of the value.
     *
     * The external representation is used for communication with VDS and in exports
     * and imports so it must be strictly defined and may not change.
     */
    public String getExternal() {
        return value;
    }

    public static VmExternalDataKind fromExternal(String value) {
        for (VmExternalDataKind kind : VmExternalDataKind.values()) {
            if (kind.getExternal().equals(value)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Illegal external data kind: " + value);
    }
}
