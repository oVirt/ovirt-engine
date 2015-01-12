package org.ovirt.engine.api.restapi.types;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.DataCenterStatus;
import org.ovirt.engine.api.model.MacPool;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.restapi.model.StorageFormat;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;

public class DataCenterMapper {

    @Mapping(from = DataCenter.class, to = StoragePool.class)
    public static StoragePool map(DataCenter model, StoragePool template) {
        StoragePool entity = template != null ? template : new StoragePool();
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setdescription(model.getDescription());
        }
        if (model.isSetComment()) {
            entity.setComment(model.getComment());
        }
        if (model.isSetStorageType()) {
            StorageType storageType = StorageType.fromValue(model.getStorageType());
            if (storageType != null) {
                entity.setIsLocal(StorageDomainMapper.map(storageType, null) == org.ovirt.engine.core.common.businessentities.StorageType.LOCALFS);
            }
        }
        if (model.isSetLocal()) {
            entity.setIsLocal(model.isLocal());
        }
        if (model.isSetStorageFormat()) {
            StorageFormat storageFormat =  StorageFormat.fromValue(model.getStorageFormat());
            if (storageFormat!=null) {
                entity.setStoragePoolFormatType(StorageFormatMapper.map(storageFormat, null));
            }
        }
        if (model.isSetVersion() && model.getVersion().getMajor()!=null && model.getVersion().getMinor()!=null) {
            entity.setCompatibilityVersion(new org.ovirt.engine.core.compat.Version(model.getVersion().getMajor(),
                    model.getVersion().getMinor()));
        }

        if (model.isSetMacPool() && model.getMacPool().isSetId()) {
            entity.setMacPoolId(GuidUtils.asGuid(model.getMacPool().getId()));
        }

        return entity;
    }

    @Mapping(from = StoragePool.class, to = DataCenter.class)
    public static DataCenter map(StoragePool entity, DataCenter template) {
        DataCenter model = template != null ? template : new DataCenter();
        model.setId(entity.getId().toString());
        model.setName(entity.getName());
        model.setLocal(entity.isLocal());

        if (!StringUtils.isEmpty(entity.getdescription())) {
                model.setDescription(entity.getdescription());
        }
        if (!StringUtils.isEmpty(entity.getComment())) {
            model.setComment(entity.getComment());
        }
        if (entity.getStatus()!=null) {
            model.setStatus(StatusUtils.create(map(entity.getStatus(), null)));
        }
        if (entity.getCompatibilityVersion() != null) {
            model.setVersion(new Version());
            model.getVersion().setMajor(entity.getCompatibilityVersion().getMajor());
            model.getVersion().setMinor(entity.getCompatibilityVersion().getMinor());
        }
        if (entity.getStoragePoolFormatType()!=null) {
            StorageFormat storageFormat = StorageFormatMapper.map(entity.getStoragePoolFormatType(), null);
            if (storageFormat!=null) {
                model.setStorageFormat(storageFormat.value());
            }
        }

        if (entity.getMacPoolId() != null) {
            model.setMacPool(new MacPool());
            model.getMacPool().setId(entity.getMacPoolId().toString());
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
        case NonResponsive:
            return DataCenterStatus.PROBLEMATIC;
        case Uninitialized:
            return DataCenterStatus.UNINITIALIZED;
        case Up:
            return DataCenterStatus.UP;
        default: throw new IllegalStateException("Enum mapping failed");
        }
    }
}

