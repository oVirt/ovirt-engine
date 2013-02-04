package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class VmPoolMapper {

    @Mapping(from = VmPool.class, to = org.ovirt.engine.core.common.businessentities.VmPool.class)
    public static org.ovirt.engine.core.common.businessentities.VmPool map(VmPool model,
            org.ovirt.engine.core.common.businessentities.VmPool template) {
        org.ovirt.engine.core.common.businessentities.VmPool entity =
                template != null ? template : new org.ovirt.engine.core.common.businessentities.VmPool();
        if (model.isSetId()) {
            entity.setvm_pool_id(new Guid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setvm_pool_name(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setvm_pool_description(model.getDescription());
        }
        if (model.isSetSize()) {
            entity.setvm_assigned_count(model.getSize());
        }
        if (model.isSetCluster()) {
            if (model.getCluster().isSetId()) {
                entity.setvds_group_id(new Guid(model.getCluster().getId()));
            } else if (model.getCluster().isSetName()) {
                entity.setvds_group_name(model.getCluster().getName());
            }
        }
        if (model.isSetPrestartedVms()) {
            entity.setPrestartedVms(model.getPrestartedVms());
        }
        return entity;
    }

    @Mapping(from = VmPool.class, to = VM.class)
    public static VM map(VmPool model, VM template) {
        VM entity = template != null ? template : new VM();
        entity.setVmName(model.getName());
        entity.setVmDescription(model.getDescription());
        if (model.isSetTemplate() &&
            model.getTemplate().isSetId()) {
            entity.setVmtGuid(new Guid(model.getTemplate().getId()));
        }
        if (model.isSetCluster() &&
            model.getCluster().isSetId()) {
            entity.setVdsGroupId(new Guid(model.getCluster().getId()));
        }
        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.VmPool.class, to = VmPool.class)
    public static VmPool map(org.ovirt.engine.core.common.businessentities.VmPool entity, VmPool template) {
        VmPool model = template != null ? template : new VmPool();
        model.setId(entity.getvm_pool_id().toString());
        model.setName(entity.getvm_pool_name());
        model.setDescription(entity.getvm_pool_description());
        model.setSize(entity.getvm_assigned_count());
        model.setPrestartedVms(entity.getPrestartedVms());
        if (entity.getvds_group_id() != null ||
            entity.getvds_group_name() != null) {
            model.setCluster(new Cluster());
            model.getCluster().setId(entity.getvds_group_id().toString());
        }
        return model;
    }
}
