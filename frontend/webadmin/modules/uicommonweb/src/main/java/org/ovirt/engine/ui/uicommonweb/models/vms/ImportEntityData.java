package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public abstract class ImportEntityData<E> extends EntityModel<E> {
    boolean isExistsInSystem;
    private EntityModel<Boolean> clone;
    private ListModel<Cluster> cluster;
    private ListModel<Quota> clusterQuota;

    public ImportEntityData() {
        setClone(new EntityModel<>(false));
        setCluster(new ListModel<Cluster>());
        setClusterQuota(new ListModel<Quota>());
    }

    public boolean isExistsInSystem() {
        return isExistsInSystem;
    }

    public void setExistsInSystem(boolean isExistsInSystem) {
        this.isExistsInSystem = isExistsInSystem;
    }

    public EntityModel<Boolean> getClone() {
        return clone;
    }

    public void setClone(EntityModel<Boolean> clone) {
        this.clone = clone;
    }

    public ListModel<Cluster> getCluster() {
        return cluster;
    }

    public void setCluster(ListModel<Cluster> cluster) {
        this.cluster = cluster;
    }

    public ListModel<Quota> getClusterQuota() {
        return clusterQuota;
    }

    public void setClusterQuota(ListModel<Quota> clusterQuota) {
        this.clusterQuota = clusterQuota;
    }

    public void selectClusterByName(String name) {
        for (Cluster cluster : getCluster().getItems()) {
            if (cluster.getName().equals(name)) {
                getCluster().setSelectedItem(cluster);
                break;
            }
        }
    }

    public List<String> getClusterNames() {
        List<String> names = new ArrayList<>();
        if (getCluster().getItems() != null) {
            for (Cluster cluster : getCluster().getItems()) {
                names.add(cluster.getName());
            }
        }
        return names;
    }

    public abstract ArchitectureType getArchType();

    public abstract String getName();
}
