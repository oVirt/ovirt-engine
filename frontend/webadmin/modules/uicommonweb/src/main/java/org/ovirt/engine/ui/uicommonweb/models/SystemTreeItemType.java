package org.ovirt.engine.ui.uicommonweb.models;

@SuppressWarnings("unused")
public enum SystemTreeItemType {
    System,
    DataCenters,
    DataCenter,
    Storages,
    Storage,
    Templates,
    Clusters,
    Cluster,
    Cluster_Gluster,
    VMs,
    Hosts,
    Host,
    Disk,
    Volume,
    Volumes,
    Networks,
    Network,
    Providers,
    Provider,
    Sessions,
    Errata;

    public int getValue() {
        return this.ordinal();
    }

    public static SystemTreeItemType forValue(int value) {
        return values()[value];
    }
}
