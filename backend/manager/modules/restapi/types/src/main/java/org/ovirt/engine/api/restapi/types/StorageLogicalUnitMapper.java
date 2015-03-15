package org.ovirt.engine.api.restapi.types;

import java.util.ArrayList;

import org.ovirt.engine.api.model.LogicalUnit;
import org.ovirt.engine.api.model.LunStatus;
import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.utils.SizeConverter;

public class StorageLogicalUnitMapper {

    @Mapping(from = LUNs.class, to = LogicalUnit.class)
    public static LogicalUnit map(LUNs entity, LogicalUnit template) {
        LogicalUnit model = template != null ? template : new LogicalUnit();
        model.setId(entity.getLUN_id());
        if (entity.getVendorId()!=null && !entity.getVendorId().isEmpty()) {
            model.setVendorId(entity.getVendorId());
        }
        if (entity.getProductId()!=null && !entity.getProductId().isEmpty()) {
            model.setProductId(entity.getProductId());
        }
        if (entity.getSerial()!=null && !entity.getSerial().isEmpty()) {
            model.setSerial(entity.getSerial());
        }
        if (entity.getLunMapping()!=null) {
            model.setLunMapping(entity.getLunMapping());
        }
        if (entity.getvolume_group_id() != null && !entity.getvolume_group_id().isEmpty()) {
            model.setVolumeGroupId(entity.getvolume_group_id());
        }
        if (entity.getStorageDomainId() != null) {
            model.setStorageDomainId(entity.getStorageDomainId().toString());
        }
        if (entity.getDiskId() != null) {
            model.setDiskId(entity.getDiskId().toString());
        }
        if (entity.getStatus() != null) {
            model.setStatus(map(entity.getStatus(), null).value());
        }
        model.setSize(SizeConverter.convert((long)entity.getDeviceSize(),
                SizeConverter.SizeUnit.GB, SizeConverter.SizeUnit.BYTES).longValue());

        model.setPaths(entity.getPathCount());
        return model;
    }

    @Mapping(from = StorageServerConnections.class, to = LogicalUnit.class)
    public static LogicalUnit map(StorageServerConnections entity, LogicalUnit template) {
        LogicalUnit model = template != null ? template : new LogicalUnit();
        model.setAddress(entity.getconnection());
        model.setTarget(entity.getiqn());
        model.setPort(Integer.parseInt(entity.getport()));
        model.setUsername(entity.getuser_name());
        if (entity.getconnection()!=null && entity.getport()!=null && entity.getportal()!=null) {
            model.setPortal(entity.getconnection() + ":" + entity.getport() + "," + entity.getportal());
        }
        return model;
    }

    @Mapping(from = LUNs.class, to = Storage.class)
    public static Storage map(LUNs entity, Storage template) {
        Storage model = template != null ? template : new Storage();
        model.setId(entity.getLUN_id());
        model.setType(StorageDomainMapper.map(entity.getLunType(), null));
        model.getLogicalUnits().add(map(entity, (LogicalUnit)null));
        return model;
    }

    /**
     * This mapping exists for adding a lun-disk, and assumes that 'storage' entity contains exactly one lun.
     */
    @Mapping(from = Storage.class, to = LUNs.class)
    public static LUNs map(Storage model, LUNs template) {
        LUNs entity = template != null ? template : new LUNs();
        if (model.isSetLogicalUnits()) {
            LogicalUnit logicalUnit = model.getLogicalUnits().get(0);
            entity.setLUN_id(logicalUnit.getId());
            ArrayList<StorageServerConnections> connections = new ArrayList<StorageServerConnections>();
            connections.add(map(logicalUnit, null));
            entity.setLunConnections(connections);
        }
        if (model.isSetType()) {
            StorageType storageType = StorageType.fromValue(model.getType());
            if (storageType != null) {
                entity.setLunType(StorageDomainMapper.map(storageType, null));
            }
        }
        return entity;
    }

    @Mapping(from = LogicalUnit.class, to = StorageServerConnections.class)
    public static StorageServerConnections map(LogicalUnit logicalUnit, StorageServerConnections connection) {
        StorageServerConnections entity = connection != null ? connection : new StorageServerConnections();
        if (logicalUnit.isSetAddress()) {
            entity.setconnection(logicalUnit.getAddress());
        }
        if (logicalUnit.isSetTarget()) {
            entity.setiqn(logicalUnit.getTarget());
        }
        if (logicalUnit.isSetPort()) {
            entity.setport(logicalUnit.getPort().toString());
        }
        if (logicalUnit.isSetUsername()) {
            entity.setuser_name(logicalUnit.getUsername());
        }
        if (logicalUnit.isSetPassword()) {
            entity.setpassword(logicalUnit.getPassword());
        }
        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.storage.LunStatus.class, to = LunStatus.class)
    public static LunStatus map(org.ovirt.engine.core.common.businessentities.storage.LunStatus status, LunStatus template) {
        switch (status) {
        case Free:
            return LunStatus.Free;
        case Used:
            return LunStatus.Used;
        case Unusable:
            return LunStatus.Unusable;
        default:
            return null;
        }
    }
}
