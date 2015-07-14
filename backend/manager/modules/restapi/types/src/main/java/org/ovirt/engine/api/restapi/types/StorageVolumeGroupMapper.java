package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.model.VolumeGroup;


public class StorageVolumeGroupMapper {

    @Mapping(from = org.ovirt.engine.core.common.businessentities.StorageDomain.class, to = Storage.class)
    public static Storage map(org.ovirt.engine.core.common.businessentities.StorageDomain entity, Storage template) {
        Storage model = template != null ? template : new Storage();
        model.setId(entity.getStorage());
        model.setType(StorageDomainMapper.map(entity.getStorageType(), null));
        model.setVolumeGroup(new VolumeGroup());
        model.getVolumeGroup().setId(entity.getStorage());
        return model;
    }

    @Mapping(from = Storage.class, to = org.ovirt.engine.core.common.businessentities.StorageDomain.class)
    public static org.ovirt.engine.core.common.businessentities.StorageDomain map(Storage model, org.ovirt.engine.core.common.businessentities.StorageDomain template) {
        org.ovirt.engine.core.common.businessentities.StorageDomain entity = template != null ? template : new org.ovirt.engine.core.common.businessentities.StorageDomain();
        entity.setStorage(model.getId());
        if (model.isSetType()) {
            StorageType storageType = StorageType.fromValue(model.getType());
            if (storageType != null) {
                entity.setStorageType(StorageDomainMapper.map(storageType, null));
            }
        }
        return entity;
    }
}
