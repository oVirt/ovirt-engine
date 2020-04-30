package org.ovirt.engine.core.common.queries;

public class ArchCapabilitiesParameters extends QueryParametersBase {

    private static final long serialVersionUID = -7091068258018831149L;

    private ArchCapabilitiesVerb archCapabilitiesVerb;

    public ArchCapabilitiesParameters() {

    }

    public ArchCapabilitiesParameters(ArchCapabilitiesVerb verb) {
        this.archCapabilitiesVerb = verb;
    }

    public ArchCapabilitiesVerb getArchCapabilitiesVerb() {
        return archCapabilitiesVerb;
    }

    public enum ArchCapabilitiesVerb {
        GetMigrationSupport,
        GetMemorySnapshotSupport,
        GetSuspendSupport,
        GetMemoryHotUnplugSupport,
        GetTpmDeviceSupport
    }
}
