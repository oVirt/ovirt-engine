package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.VM;

public class VmPoolMapper {

    @Mapping(from = VmPool.class, to = org.ovirt.engine.core.common.businessentities.VmPool.class)
    public static org.ovirt.engine.core.common.businessentities.VmPool map(VmPool model,
            org.ovirt.engine.core.common.businessentities.VmPool template) {
        org.ovirt.engine.core.common.businessentities.VmPool entity =
                template != null ? template : new org.ovirt.engine.core.common.businessentities.VmPool();
        if (model.isSetId()) {
            entity.setVmPoolId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setVmPoolDescription(model.getDescription());
        }
        if (model.isSetSize()) {
            entity.setAssignedVmsCount(model.getSize());
        }
        if (model.isSetCluster()) {
            if (model.getCluster().isSetId()) {
                entity.setVdsGroupId(GuidUtils.asGuid(model.getCluster().getId()));
            } else if (model.getCluster().isSetName()) {
                entity.setVdsGroupName(model.getCluster().getName());
            }
        }
        if (model.isSetPrestartedVms()) {
            entity.setPrestartedVms(model.getPrestartedVms());
        }
        if (model.isSetMaxUserVms()) {
            entity.setMaxAssignedVmsPerUser(model.getMaxUserVms());
        }
        return entity;
    }

    @Mapping(from = VmPool.class, to = VM.class)
    public static VM map(VmPool model, VM template) {
        VM entity = template != null ? template : new VM();
        entity.setName(model.getName());
        entity.setVmDescription(model.getDescription());
        if (model.isSetTemplate() &&
            model.getTemplate().isSetId()) {
            entity.setVmtGuid(GuidUtils.asGuid(model.getTemplate().getId()));
        }
        if (model.isSetCluster() &&
            model.getCluster().isSetId()) {
            entity.setVdsGroupId(GuidUtils.asGuid(model.getCluster().getId()));
        }
        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.VmPool.class, to = VmPool.class)
    public static VmPool map(org.ovirt.engine.core.common.businessentities.VmPool entity, VmPool template) {
        VmPool model = template != null ? template : new VmPool();
        model.setId(entity.getVmPoolId().toString());
        model.setName(entity.getName());
        model.setDescription(entity.getVmPoolDescription());
        model.setSize(entity.getAssignedVmsCount());
        model.setPrestartedVms(entity.getPrestartedVms());
        if (entity.getVdsGroupId() != null ||
            entity.getVdsGroupName() != null) {
            model.setCluster(new Cluster());
            model.getCluster().setId(entity.getVdsGroupId().toString());
        }
        model.setMaxUserVms(entity.getMaxAssignedVmsPerUser());
        return model;
    }
}
