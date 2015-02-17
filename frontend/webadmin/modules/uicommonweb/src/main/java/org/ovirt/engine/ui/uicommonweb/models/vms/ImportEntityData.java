package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

import java.util.ArrayList;
import java.util.List;

public abstract class ImportEntityData<E> extends EntityModel<E> {
    boolean isExistsInSystem;
    private EntityModel<Boolean> clone;
    private ListModel<VDSGroup> cluster;
    private ListModel<Quota> clusterQuota;

    public ImportEntityData() {
        setClone(new EntityModel<>(false));
        setCluster(new ListModel<VDSGroup>());
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

    public ListModel<VDSGroup> getCluster() {
        return cluster;
    }

    public void setCluster(ListModel<VDSGroup> cluster) {
        this.cluster = cluster;
    }

    public ListModel<Quota> getClusterQuota() {
        return clusterQuota;
    }

    public void setClusterQuota(ListModel<Quota> clusterQuota) {
        this.clusterQuota = clusterQuota;
    }

    public void selectClusterByName(String name) {
        for (VDSGroup vdsGroup : getCluster().getItems()) {
            if (vdsGroup.getName().equals(name)) {
                getCluster().setSelectedItem(vdsGroup);
                break;
            }
        }
    }

    public List<String> getClusterNames() {
        List<String> names = new ArrayList<String>();
        if (getCluster().getItems() != null) {
            for (VDSGroup vdsGroup : getCluster().getItems()) {
                names.add(vdsGroup.getName());
            }
        }
        return names;
    }

    public abstract ArchitectureType getArchType();

    public abstract String getName();
}
