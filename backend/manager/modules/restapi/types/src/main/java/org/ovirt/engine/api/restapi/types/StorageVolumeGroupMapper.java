package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.HostStorage;
import org.ovirt.engine.api.model.VolumeGroup;


public class StorageVolumeGroupMapper {

    @Mapping(from = org.ovirt.engine.core.common.businessentities.StorageDomain.class, to = HostStorage.class)
    public static HostStorage map(org.ovirt.engine.core.common.businessentities.StorageDomain entity, HostStorage template) {
        HostStorage model = template != null ? template : new HostStorage();
        model.setId(entity.getStorage());
        model.setType(StorageDomainMapper.map(entity.getStorageType(), null));
        model.setVolumeGroup(new VolumeGroup());
        model.getVolumeGroup().setId(entity.getStorage());
        return model;
    }

    @Mapping(from = HostStorage.class, to = org.ovirt.engine.core.common.businessentities.StorageDomain.class)
    public static org.ovirt.engine.core.common.businessentities.StorageDomain map(HostStorage model, org.ovirt.engine.core.common.businessentities.StorageDomain template) {
        org.ovirt.engine.core.common.businessentities.StorageDomain entity = template != null ? template : new org.ovirt.engine.core.common.businessentities.StorageDomain();
        entity.setStorage(model.getId());
        if (model.isSetType()) {
            entity.setStorageType(StorageDomainMapper.map(model.getType(), null));
        }
        return entity;
    }
}
