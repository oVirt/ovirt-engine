package org.ovirt.engine.api.restapi.types;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.DataCenterStatus;
import org.ovirt.engine.api.model.MacPool;
import org.ovirt.engine.api.model.QuotaModeType;
import org.ovirt.engine.api.model.StorageFormat;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
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
        if (model.isSetLocal()) {
            entity.setIsLocal(model.isLocal());
        }
        if (model.isSetStorageFormat()) {
            entity.setStoragePoolFormatType(StorageFormatMapper.map(model.getStorageFormat(), null));
        }
        if (model.isSetVersion() && model.getVersion().getMajor() != null && model.getVersion().getMinor() != null) {
            entity.setCompatibilityVersion(VersionMapper.map(model.getVersion()));
        }

        if (model.isSetMacPool() && model.getMacPool().isSetId()) {
            entity.setMacPoolId(GuidUtils.asGuid(model.getMacPool().getId()));
        }

        if (model.isSetQuotaMode()) {
            entity.setQuotaEnforcementType(map(model.getQuotaMode()));
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
            model.setStatus(mapDataCenterStatus(entity.getStatus()));
        }
        if (entity.getCompatibilityVersion() != null) {
            model.setVersion(VersionMapper.map(entity.getCompatibilityVersion()));
        }
        if (entity.getStoragePoolFormatType()!=null) {
            StorageFormat storageFormat = StorageFormatMapper.map(entity.getStoragePoolFormatType(), null);
            if (storageFormat!=null) {
                model.setStorageFormat(storageFormat);
            }
        }

        if (entity.getMacPoolId() != null) {
            model.setMacPool(new MacPool());
            model.getMacPool().setId(entity.getMacPoolId().toString());
        }

        if (entity.getQuotaEnforcementType() != null) {
            model.setQuotaMode(map(entity.getQuotaEnforcementType()));
        }

        return model;
    }

    private static DataCenterStatus mapDataCenterStatus(StoragePoolStatus status) {
        switch (status) {
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
        default:
            throw new IllegalArgumentException("Unknown data center status \"" + status + "\"");
        }
    }

    @Mapping(from = QuotaEnforcementTypeEnum.class, to = QuotaModeType.class)
    public static QuotaModeType map(QuotaEnforcementTypeEnum type) {
        switch (type) {
        case DISABLED:
            return QuotaModeType.DISABLED;
        case HARD_ENFORCEMENT:
            return QuotaModeType.ENABLED;
        case SOFT_ENFORCEMENT:
            return QuotaModeType.AUDIT;
        default:
            throw new IllegalArgumentException("Unknown quota enforcement type \"" + type + "\"");
        }
    }

    @Mapping(from = QuotaModeType.class, to = QuotaEnforcementTypeEnum.class)
    public static QuotaEnforcementTypeEnum map(QuotaModeType type) {
        switch (type) {
        case DISABLED:
            return QuotaEnforcementTypeEnum.DISABLED;
        case ENABLED:
            return QuotaEnforcementTypeEnum.HARD_ENFORCEMENT;
        case AUDIT:
            return QuotaEnforcementTypeEnum.SOFT_ENFORCEMENT;
        default:
            throw new IllegalArgumentException("Unknown quota mode type \"" + type + "\"");
        }
    }
}

