package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.VolumeGroup;
import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.model.StorageType;


public class StorageVolumeGroupMapper {

    @Mapping(from = org.ovirt.engine.core.common.businessentities.StorageDomain.class, to = Storage.class)
    public static Storage map(org.ovirt.engine.core.common.businessentities.StorageDomain entity, Storage template) {
        Storage model = template != null ? template : new Storage();
        model.setId(entity.getstorage());
        model.setType(StorageDomainMapper.map(entity.getstorage_type(), null));
        model.setVolumeGroup(new VolumeGroup());
        model.getVolumeGroup().setId(entity.getstorage());
        return model;
    }

    @Mapping(from = Storage.class, to = org.ovirt.engine.core.common.businessentities.StorageDomain.class)
    public static org.ovirt.engine.core.common.businessentities.StorageDomain map(Storage model, org.ovirt.engine.core.common.businessentities.StorageDomain template) {
        org.ovirt.engine.core.common.businessentities.StorageDomain entity = template != null ? template : new org.ovirt.engine.core.common.businessentities.StorageDomain();
        entity.setstorage(model.getId());
        if (model.isSetType()) {
            StorageType storageType = StorageType.fromValue(model.getType());
            if (storageType != null) {
                entity.setstorage_type(StorageDomainMapper.map(storageType, null));
            }
        }
        return entity;
    }
}
