package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.api.model.Qos;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.restapi.utils.GuidUtils;

public class DiskProfileMapper {
    @Mapping(from = DiskProfile.class, to = org.ovirt.engine.core.common.businessentities.profiles.DiskProfile.class)
    public static org.ovirt.engine.core.common.businessentities.profiles.DiskProfile map(DiskProfile model,
            org.ovirt.engine.core.common.businessentities.profiles.DiskProfile template) {
        org.ovirt.engine.core.common.businessentities.profiles.DiskProfile entity =
                template != null ? template : new org.ovirt.engine.core.common.businessentities.profiles.DiskProfile();
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setDescription(model.getDescription());
        }
        if (model.isSetStorageDomain() && model.getStorageDomain().isSetId()) {
            entity.setStorageDomainId(GuidUtils.asGuid(model.getStorageDomain().getId()));
        }
        if (model.isSetQos()) {
            if (model.getQos().isSetId()) {
                entity.setQosId(GuidUtils.asGuid(model.getQos().getId()));
            } else {
                entity.setQosId(null);
            }
        }
        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.profiles.DiskProfile.class, to = DiskProfile.class)
    public static DiskProfile map(org.ovirt.engine.core.common.businessentities.profiles.DiskProfile entity,
            DiskProfile template) {
        DiskProfile model = template != null ? template : new DiskProfile();
        if (entity.getId() != null) {
            model.setId(entity.getId().toString());
        }
        if (entity.getName() != null) {
            model.setName(entity.getName());
        }
        if (entity.getDescription() != null) {
            model.setDescription(entity.getDescription());
        }
        if (entity.getStorageDomainId() != null) {
            model.setStorageDomain(new StorageDomain());
            model.getStorageDomain().setId(entity.getStorageDomainId().toString());
        }
        if (entity.getQosId() != null) {
            model.setQos(new Qos());
            model.getQos().setId(entity.getQosId().toString());
        }

        return model;
    }
}
