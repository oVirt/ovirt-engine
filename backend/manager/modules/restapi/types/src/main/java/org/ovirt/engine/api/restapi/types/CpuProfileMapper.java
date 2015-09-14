package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.CpuProfile;
import org.ovirt.engine.api.model.Qos;
import org.ovirt.engine.api.restapi.utils.GuidUtils;

public class CpuProfileMapper {
    @Mapping(from = CpuProfile.class, to = org.ovirt.engine.core.common.businessentities.profiles.CpuProfile.class)
    public static org.ovirt.engine.core.common.businessentities.profiles.CpuProfile map(CpuProfile model,
            org.ovirt.engine.core.common.businessentities.profiles.CpuProfile template) {
        org.ovirt.engine.core.common.businessentities.profiles.CpuProfile entity =
                template != null ? template : new org.ovirt.engine.core.common.businessentities.profiles.CpuProfile();
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setDescription(model.getDescription());
        }
        if (model.isSetCluster() && model.getCluster().isSetId()) {
            entity.setClusterId(GuidUtils.asGuid(model.getCluster().getId()));
        }
        if (model.isSetQos() && model.getQos().isSetId()) {
            entity.setQosId(GuidUtils.asGuid(model.getQos().getId()));
        }
        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.profiles.CpuProfile.class, to = CpuProfile.class)
    public static CpuProfile map(org.ovirt.engine.core.common.businessentities.profiles.CpuProfile entity,
            CpuProfile template) {
        CpuProfile model = template != null ? template : new CpuProfile();
        if (entity.getId() != null) {
            model.setId(entity.getId().toString());
        }
        if (entity.getName() != null) {
            model.setName(entity.getName());
        }
        if (entity.getDescription() != null) {
            model.setDescription(entity.getDescription());
        }
        if (entity.getClusterId() != null) {
            model.setCluster(new Cluster());
            model.getCluster().setId(entity.getClusterId().toString());
        }
        if (entity.getQosId() != null) {
            model.setQos(new Qos());
            model.getQos().setId(entity.getQosId().toString());
        }

        return model;
    }
}
