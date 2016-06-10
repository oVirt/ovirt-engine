package org.ovirt.engine.core.vdsbroker.builder.vminfo;

enum VnicProfileProperties {
    PORT_MIRRORING("Port Mirroring"),
    CUSTOM_PROPERTIES("Custom Properties"),
    NETWORK_QOS("Network QoS");

    private final String featureName;

    VnicProfileProperties(String featureName) {
        this.featureName = featureName;
    }

    public String getFeatureName() {
        return featureName;
    }
}
