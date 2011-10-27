package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.VolumeGroup;
import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_domains;

public class StorageVolumeGroupMapper {

    @Mapping(from = storage_domains.class, to = Storage.class)
    public static Storage map(storage_domains entity, Storage template) {
        Storage model = template != null ? template : new Storage();
        model.setId(entity.getstorage());
        model.setType(StorageDomainMapper.map(entity.getstorage_type(), null));
        model.setVolumeGroup(new VolumeGroup());
        model.getVolumeGroup().setId(entity.getstorage());
        return model;
    }

    @Mapping(from = Storage.class, to = storage_domains.class)
    public static storage_domains map(Storage model, storage_domains template) {
        storage_domains entity = template != null ? template : new storage_domains();
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
