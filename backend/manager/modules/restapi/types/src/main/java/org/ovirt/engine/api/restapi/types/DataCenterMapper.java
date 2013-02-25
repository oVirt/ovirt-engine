package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.common.util.StringUtils;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.DataCenterStatus;
import org.ovirt.engine.api.restapi.model.StorageFormat;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool;

public class DataCenterMapper {

    @Mapping(from = DataCenter.class, to = storage_pool.class)
    public static storage_pool map(DataCenter model, storage_pool template) {
        storage_pool entity = template != null ? template : new storage_pool();
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setname(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setdescription(model.getDescription());
        }
        if (model.isSetStorageType()) {
            StorageType storageType = StorageType.fromValue(model.getStorageType());
            if (storageType != null) {
                entity.setstorage_pool_type(StorageDomainMapper.map(storageType, null));
            }
        }
        if (model.isSetStorageFormat()) {
            StorageFormat storageFormat =  StorageFormat.fromValue(model.getStorageFormat());
            if (storageFormat!=null) {
                entity.setStoragePoolFormatType(StorageFormatMapper.map(storageFormat, null));
            }
        }
        if (model.isSetVersion() && model.getVersion().getMajor()!=null && model.getVersion().getMinor()!=null) {
            entity.setcompatibility_version(new org.ovirt.engine.core.compat.Version(model.getVersion().getMajor(),
                                                                                model.getVersion().getMinor()));
        }
        return entity;
    }

    @Mapping(from = storage_pool.class, to = DataCenter.class)
    public static DataCenter map(storage_pool entity, DataCenter template) {
        DataCenter model = template != null ? template : new DataCenter();
        model.setId(entity.getId().toString());
        model.setName(entity.getname());
        if (!StringUtils.isNullOrEmpty(entity.getdescription())) {
                model.setDescription(entity.getdescription());
        }
        model.setStorageType(StorageDomainMapper.map(entity.getstorage_pool_type(), null));
        if (entity.getstatus()!=null) {
            model.setStatus(StatusUtils.create(map(entity.getstatus(), null)));
        }
        if (entity.getcompatibility_version() != null) {
            model.setVersion(new Version());
            model.getVersion().setMajor(entity.getcompatibility_version().getMajor());
            model.getVersion().setMinor(entity.getcompatibility_version().getMinor());
        }
        if (entity.getStoragePoolFormatType()!=null) {
            StorageFormat storageFormat = StorageFormatMapper.map(entity.getStoragePoolFormatType(), null);
            if (storageFormat!=null) {
                model.setStorageFormat(storageFormat.value());
            }
        }
        return model;
    }

    @Mapping(from = StoragePoolStatus.class, to = DataCenterStatus.class)
    private static DataCenterStatus map(StoragePoolStatus storagePoolStatus, DataCenterStatus dataCenterStatus) {
        switch (storagePoolStatus) {
        case Contend:
            return DataCenterStatus.CONTEND;
        case Maintenance:
            return DataCenterStatus.MAINTENANCE;
        case NotOperational:
            return DataCenterStatus.NOT_OPERATIONAL;
        case Problematic:
            return DataCenterStatus.PROBLEMATIC;
        case Uninitialized:
            return DataCenterStatus.UNINITIALIZED;
        case Up:
            return DataCenterStatus.UP;
        default: throw new IllegalStateException("Enum mapping failed");
        }
    }
}

