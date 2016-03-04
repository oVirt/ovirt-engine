package org.ovirt.engine.core.common.vdscommands;

public enum FutureVDSCommandType {
    Poll,
    TimeBoundPoll,
    HostSetupNetworks;

    private static final String DEFAULT_PACKAGE = "org.ovirt.engine.core.vdsbroker.vdsbroker";
    String packageName;
    private String fullyQualifiedClassName;

    private FutureVDSCommandType() {
        this.packageName = DEFAULT_PACKAGE;
        buildFqClassName();
    }

    private FutureVDSCommandType(String packageName) {
        this.packageName = packageName;
        buildFqClassName();
    }

    public String getPackageName() {
        return packageName;
    }

    public String getFullyQualifiedClassName() {
        return fullyQualifiedClassName;
    }

    private void buildFqClassName() {
        this.fullyQualifiedClassName = this.packageName + "." + this.name() + "VDSCommand";
    }
}
