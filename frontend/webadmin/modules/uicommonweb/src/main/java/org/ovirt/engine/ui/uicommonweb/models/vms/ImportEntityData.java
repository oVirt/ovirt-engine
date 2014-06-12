package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

import java.util.ArrayList;
import java.util.List;

public abstract class ImportEntityData extends EntityModel {
    protected Object entity;
    boolean isExistsInSystem;
    private EntityModel clone;
    private ListModel<VDSGroup> cluster;

    public ImportEntityData() {
        setClone(new EntityModel(false));
        setCluster(new ListModel());
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public boolean isExistsInSystem() {
        return isExistsInSystem;
    }

    public void setExistsInSystem(boolean isExistsInSystem) {
        this.isExistsInSystem = isExistsInSystem;
    }

    public EntityModel getClone() {
        return clone;
    }

    public void setClone(EntityModel clone) {
        this.clone = clone;
    }

    public ListModel<VDSGroup> getCluster() {
        return cluster;
    }

    public void setCluster(ListModel<VDSGroup> cluster) {
        this.cluster = cluster;
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
