package org.ovirt.engine.api.restapi.types;

import java.util.ArrayList;

import org.ovirt.engine.api.model.HostStorage;
import org.ovirt.engine.api.model.LogicalUnit;
import org.ovirt.engine.api.model.LogicalUnits;
import org.ovirt.engine.api.model.LunStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.utils.SizeConverter;

public class StorageLogicalUnitMapper {

    @Mapping(from = LUNs.class, to = LogicalUnit.class)
    public static LogicalUnit map(LUNs entity, LogicalUnit template) {
        LogicalUnit model = template != null ? template : new LogicalUnit();
        model.setId(entity.getLUNId());
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
        if (entity.getVolumeGroupId() != null && !entity.getVolumeGroupId().isEmpty()) {
            model.setVolumeGroupId(entity.getVolumeGroupId());
        }
        if (entity.getStorageDomainId() != null) {
            model.setStorageDomainId(entity.getStorageDomainId().toString());
        }
        if (entity.getDiskId() != null) {
            model.setDiskId(entity.getDiskId().toString());
        }
        if (entity.getStatus() != null) {
            model.setStatus(map(entity.getStatus(), null));
        }
        if (entity.getDiscardMaxSize() != null) {
            model.setDiscardMaxSize(entity.getDiscardMaxSize());
        }
        // Not supported by sysfs since kernel version 4.12, and thus deprecated.
        model.setDiscardZeroesData(false);
        model.setSize(SizeConverter.convert((long)entity.getDeviceSize(),
                SizeConverter.SizeUnit.GiB, SizeConverter.SizeUnit.BYTES).longValue());

        model.setPaths(entity.getPathCount());
        return model;
    }

    @Mapping(from = StorageServerConnections.class, to = LogicalUnit.class)
    public static LogicalUnit map(StorageServerConnections entity, LogicalUnit template) {
        LogicalUnit model = template != null ? template : new LogicalUnit();
        model.setAddress(entity.getConnection());
        model.setTarget(entity.getIqn());
        model.setPort(Integer.parseInt(entity.getPort()));
        model.setUsername(entity.getUserName());
        if (entity.getConnection()!=null && entity.getPort()!=null && entity.getPortal()!=null) {
            model.setPortal(entity.getConnection() + ":" + entity.getPort() + "," + entity.getPortal());
        }
        return model;
    }

    @Mapping(from = LUNs.class, to = HostStorage.class)
    public static HostStorage map(LUNs entity, HostStorage template) {
        HostStorage model = template != null ? template : new HostStorage();
        model.setId(entity.getLUNId());
        model.setType(StorageDomainMapper.map(entity.getLunType(), null));
        model.setLogicalUnits(new LogicalUnits());
        model.getLogicalUnits().getLogicalUnits().add(map(entity, (LogicalUnit) null));
        return model;
    }

    /**
     * This mapping exists for adding a lun-disk, and assumes that 'storage' entity contains exactly one lun.
     */
    @Mapping(from = HostStorage.class, to = LUNs.class)
    public static LUNs map(HostStorage model, LUNs template) {
        LUNs entity = template != null ? template : new LUNs();
        if (model.isSetLogicalUnits() && model.getLogicalUnits().isSetLogicalUnits()) {
            LogicalUnit logicalUnit = model.getLogicalUnits().getLogicalUnits().get(0);
            entity.setLUNId(logicalUnit.getId());
            ArrayList<StorageServerConnections> connections = new ArrayList<>();
            connections.add(map(logicalUnit, null));
            entity.setLunConnections(connections);
        }
        if (model.isSetType()) {
            entity.setLunType(StorageDomainMapper.map(model.getType(), null));
        }
        return entity;
    }

    @Mapping(from = LogicalUnit.class, to = StorageServerConnections.class)
    public static StorageServerConnections map(LogicalUnit logicalUnit, StorageServerConnections connection) {
        StorageServerConnections entity = connection != null ? connection : new StorageServerConnections();
        if (logicalUnit.isSetAddress()) {
            entity.setConnection(logicalUnit.getAddress());
        }
        if (logicalUnit.isSetTarget()) {
            entity.setIqn(logicalUnit.getTarget());
        }
        if (logicalUnit.isSetPort()) {
            entity.setPort(logicalUnit.getPort().toString());
        }
        if (logicalUnit.isSetUsername()) {
            entity.setUserName(logicalUnit.getUsername());
        }
        if (logicalUnit.isSetPassword()) {
            entity.setPassword(logicalUnit.getPassword());
        }
        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.storage.LunStatus.class, to = LunStatus.class)
    public static LunStatus map(org.ovirt.engine.core.common.businessentities.storage.LunStatus status, LunStatus template) {
        switch (status) {
        case Free:
            return LunStatus.FREE;
        case Used:
            return LunStatus.USED;
        case Unusable:
            return LunStatus.UNUSABLE;
        default:
            return null;
        }
    }
}
