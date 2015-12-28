package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.StoragePool;

public class DataCenterWithCluster implements Nameable {

    private StoragePool dataCenter;

    private Cluster cluster;

    public DataCenterWithCluster(StoragePool dataCenter, Cluster cluster) {
        this.dataCenter = dataCenter;
        this.cluster = cluster;
    }

    public boolean contentEquals(StoragePool dataCenter, Cluster cluster) {
        if (dataCenter == null || cluster == null) {
            return false;
        }

        return dataCenter.getId().equals(this.dataCenter.getId()) && cluster.getId().equals(this.cluster.getId());
    }

    public StoragePool getDataCenter() {
        return dataCenter;
    }

    public Cluster getCluster() {
        return cluster;
    }

    @Override
    public String getName() {
        return getCluster().getName();
    }
}
