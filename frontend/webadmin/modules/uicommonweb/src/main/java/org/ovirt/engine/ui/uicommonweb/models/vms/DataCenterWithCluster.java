package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;

public class DataCenterWithCluster implements Nameable {

    private StoragePool dataCenter;

    private VDSGroup cluster;

    public DataCenterWithCluster(StoragePool dataCenter, VDSGroup cluster) {
        this.dataCenter = dataCenter;
        this.cluster = cluster;
    }

    public boolean contentEquals(StoragePool dataCenter, VDSGroup cluster) {
        if (dataCenter == null || cluster == null) {
            return false;
        }

        return dataCenter.getId().equals(this.dataCenter.getId()) && cluster.getId().equals(this.cluster.getId());
    }

    public StoragePool getDataCenter() {
        return dataCenter;
    }

    public VDSGroup getCluster() {
        return cluster;
    }

    @Override
    public String getName() {
        return getCluster().getName();
    }
}
