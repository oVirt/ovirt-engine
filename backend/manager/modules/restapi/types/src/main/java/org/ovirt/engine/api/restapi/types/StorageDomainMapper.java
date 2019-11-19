package org.ovirt.engine.api.restapi.types;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.HostStorage;
import org.ovirt.engine.api.model.NfsVersion;
import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomainStatus;
import org.ovirt.engine.api.model.StorageDomainType;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.model.VolumeGroup;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.StorageBlockSize;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.utils.SizeConverter;

public class StorageDomainMapper {

    @Mapping(from = StorageDomain.class, to = StorageDomainStatic.class)
    public static StorageDomainStatic map(StorageDomain model, StorageDomainStatic template) {
        StorageDomainStatic entity = template != null ? template : new StorageDomainStatic();
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setStorageName(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setDescription(model.getDescription());
        }
        if(model.isSetComment()) {
            entity.setComment(model.getComment());
        }
        if (model.isSetType()) {
            entity.setStorageDomainType(map(model.getType(), null));
        }
        if (model.isSetStorage() && model.getStorage().isSetType()) {
            entity.setStorageType(map(model.getStorage().getType(), null));
        }
        if (model.isSetStorageFormat()) {
            entity.setStorageFormat(StorageFormatMapper.map(model.getStorageFormat(), null));
        }
        if (model.isSetWipeAfterDelete()) {
            entity.setWipeAfterDelete(model.isWipeAfterDelete());
        }
        if (model.isSetDiscardAfterDelete()) {
            entity.setDiscardAfterDelete(model.isDiscardAfterDelete());
        }
        if (model.isSetWarningLowSpaceIndicator()) {
            entity.setWarningLowSpaceIndicator(model.getWarningLowSpaceIndicator());
        }
        if (model.isSetCriticalSpaceActionBlocker()) {
            entity.setCriticalSpaceActionBlocker(model.getCriticalSpaceActionBlocker());
        }
        if (model.isSetBackup()) {
            entity.setBackup(model.isBackup());
        }
        if (model.isSetBlockSize()) {
            entity.setBlockSize(StorageBlockSize.forValue(model.getBlockSize()));
        }
        return entity;
    }

    @Mapping(from = StorageDomain.class, to = StorageServerConnections.class)
    public static StorageServerConnections map(StorageDomain model, StorageServerConnections template) {
        StorageServerConnections entity = template != null ? template : new StorageServerConnections();
        if (model.isSetStorage() && model.getStorage().isSetType()) {
            HostStorage storage = model.getStorage();
            StorageType storageType = storage.getType();
            if (storageType != null) {
                entity.setStorageType(map(storageType, null));
                switch (storageType) {
                case ISCSI:
                    break;
                case FCP:
                    break;
                case GLANCE:
                    break;
                case NFS:
                    if(storage.isSetAddress() && storage.isSetPath()) {
                        entity.setConnection(storage.getAddress() + ":" + storage.getPath());
                    }
                    if(storage.getNfsRetrans() != null) {
                        entity.setNfsRetrans(storage.getNfsRetrans().shortValue());
                    }
                    if(storage.getNfsTimeo() != null) {
                        entity.setNfsTimeo(storage.getNfsTimeo().shortValue());
                    }
                    if(storage.getNfsVersion() != null) {
                        entity.setNfsVersion(map(storage.getNfsVersion(), null));
                    }
                    if (storage.isSetMountOptions()) {
                        entity.setMountOptions(storage.getMountOptions());
                    }
                    break;
                case LOCALFS:
                    if (storage.isSetPath()) {
                        entity.setConnection(storage.getPath());
                    }
                    break;
                case POSIXFS:
                case GLUSTERFS:
                    if (storage.isSetAddress() && storage.isSetPath()) {
                        entity.setConnection(storage.getAddress() + ":" + storage.getPath());
                    } else if (storage.isSetPath()) {
                        entity.setConnection(storage.getPath());
                    }
                    if (storage.isSetMountOptions()) {
                        entity.setMountOptions(storage.getMountOptions());
                    }
                    if (storage.isSetVfsType()) {
                        entity.setVfsType(storage.getVfsType());
                    }

                default:
                    break;
                }
            }
        }
        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.StorageDomain.class, to = StorageDomain.class)
    public static StorageDomain map(org.ovirt.engine.core.common.businessentities.StorageDomain entity,
            StorageDomain template) {
        StorageDomain model = template != null ? template : new StorageDomain();
        model.setId(entity.getId().toString());
        model.setName(entity.getStorageName());
        model.setDescription(entity.getDescription());
        model.setComment(entity.getComment());
        model.setType(map(entity.getStorageDomainType(), null));
        model.setWarningLowSpaceIndicator(entity.getWarningLowSpaceIndicator());
        model.setCriticalSpaceActionBlocker(entity.getCriticalSpaceActionBlocker());
        model.setMaster(entity.getStorageDomainType() == org.ovirt.engine.core.common.businessentities.StorageDomainType.Master);
        if (entity.getStatus() != null) {
            model.setStatus(mapStorageDomainStatus(entity.getStatus()));
        }
        if (entity.getExternalStatus() != null) {
            model.setExternalStatus(ExternalStatusMapper.map(entity.getExternalStatus()));
        }
        model.setStorage(new HostStorage());
        model.getStorage().setType(map(entity.getStorageType(), null));
        if (entity.getStorageType() == org.ovirt.engine.core.common.businessentities.storage.StorageType.ISCSI ||
                entity.getStorageType() == org.ovirt.engine.core.common.businessentities.storage.StorageType.FCP) {
            model.getStorage().setVolumeGroup(new VolumeGroup());
            model.getStorage().getVolumeGroup().setId(entity.getStorage());
        }
        if (entity.getAvailableDiskSize()!= null) {
            model.setAvailable(SizeConverter.convert(entity.getAvailableDiskSize().longValue(),
                    SizeConverter.SizeUnit.GiB, SizeConverter.SizeUnit.BYTES).longValue());
        }
        if (entity.getUsedDiskSize()!= null) {
            model.setUsed(SizeConverter.convert(entity.getUsedDiskSize().longValue(),
                    SizeConverter.SizeUnit.GiB, SizeConverter.SizeUnit.BYTES).longValue());
        }
        model.setCommitted(SizeConverter.convert(entity.getCommittedDiskSize(),
                SizeConverter.SizeUnit.GiB, SizeConverter.SizeUnit.BYTES).longValue());
        if (entity.getStorageFormat()!= null) {
            model.setStorageFormat(StorageFormatMapper.map(entity.getStorageFormat(), null));
        }
        model.setWipeAfterDelete(entity.getWipeAfterDelete());
        model.setDiscardAfterDelete(entity.getDiscardAfterDelete());
        model.setSupportsDiscard(entity.getSupportsDiscard());
        // Not supported by sysfs since kernel version 4.12, and thus deprecated.
        model.setSupportsDiscardZeroesData(false);
        model.setBackup(entity.isBackup());
        if (entity.getStorageStaticData().getBlockSize() != null) {
            model.setBlockSize(entity.getStorageStaticData().getBlockSize().getValue());
        }
        return model;
    }

    @Mapping(from = StorageConnection.class, to = StorageServerConnections.class)
    public static StorageServerConnections map(StorageConnection model, StorageServerConnections template) {
        StorageServerConnections entity = template != null ? template : new StorageServerConnections();
        if (model.isSetId()) {
            entity.setId(model.getId());
        }
        org.ovirt.engine.core.common.businessentities.storage.StorageType storageType = null;
        if (model.getType() != null) {
           storageType = map(model.getType(), null);
        } else if (template != null) {
           storageType = template.getStorageType();
        }
        if (storageType != null) {
            entity.setStorageType(storageType);
            switch (storageType) {
            case ISCSI:
                if (model.isSetAddress()) {
                    entity.setConnection(model.getAddress());
                }
                if (model.isSetPort()) {
                    entity.setPort(model.getPort().toString());
                }
                if (model.isSetPortal()) {
                    entity.setPortal(model.getPortal());
                }
                if (model.isSetUsername()) {
                    entity.setUserName(model.getUsername());
                }
                if (model.isSetTarget()) {
                    entity.setIqn(model.getTarget());
                }
                if (model.isSetPassword()) {
                    entity.setPassword(model.getPassword());
                }
                break;
            case FCP:
                break;
            case NFS:
                // in case of update, one or both of the address/path fields might be updated.
                // thus, need to take care of their assignment separately since they are merged
                // in the backend into a single field.
                String[] parts = null;
                if (!StringUtils.isEmpty(entity.getConnection())) {
                    parts = entity.getConnection().split(":");
                }
                String address = null;
                String path = null;
                if (model.isSetAddress()) {
                    address = model.getAddress();
                } else {
                    address = parts != null ? parts[0] : "";
                }
                if (model.isSetPath()) {
                    path = model.getPath();
                } else {
                    path = parts != null ? parts[1] : "";
                }
                entity.setConnection(address + ":" + path);

                if (model.getNfsRetrans() != null) {
                    entity.setNfsRetrans(model.getNfsRetrans().shortValue());
                }
                if (model.getNfsTimeo() != null) {
                    entity.setNfsTimeo(model.getNfsTimeo().shortValue());
                }
                if (model.getNfsVersion() != null) {
                    entity.setNfsVersion(map(model.getNfsVersion(), null));
                }
                if (model.isSetMountOptions()) {
                    entity.setMountOptions(model.getMountOptions());
                }
                break;
            case LOCALFS:
                if (model.isSetPath()) {
                    entity.setConnection(model.getPath());
                }
                break;
            case POSIXFS:
            case GLUSTERFS:
                if (model.isSetAddress() && model.isSetPath()) {
                    entity.setConnection(model.getAddress() + ":" + model.getPath());
                } else if (model.isSetPath()) {
                    entity.setConnection(model.getPath());
                }
                if (model.isSetMountOptions()) {
                    entity.setMountOptions(model.getMountOptions());
                }
                if (model.isSetVfsType()) {
                    entity.setVfsType(model.getVfsType());
                }
                break;
            default:
                break;
            }
        }
        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.StorageServerConnections.class,
            to = org.ovirt.engine.api.model.StorageConnection.class)
    public static StorageConnection map(StorageServerConnections entity, StorageConnection template) {
        StorageConnection model = template != null ? template : new StorageConnection();
        model.setId(entity.getId());
        model.setType(map(entity.getStorageType(), null));
        if (entity.getStorageType() == org.ovirt.engine.core.common.businessentities.storage.StorageType.ISCSI) {
            model.setAddress(entity.getConnection());
            model.setPort(Integer.parseInt(entity.getPort()));
            model.setUsername(entity.getUserName());
            model.setTarget(entity.getIqn());
        }
        if (entity.getStorageType().isFileDomain()) {
            setPath(entity, model);
        }
        if (entity.getStorageType().equals(org.ovirt.engine.core.common.businessentities.storage.StorageType.NFS)) {
            if (entity.getNfsVersion() != null) {
                model.setNfsVersion(map(entity.getNfsVersion(), null));
            }
            if (entity.getNfsRetrans() != null) {
                model.setNfsRetrans(entity.getNfsRetrans().intValue());
            }
            if (entity.getNfsTimeo() != null) {
                model.setNfsTimeo(entity.getNfsTimeo().intValue());
            }
            if (entity.getMountOptions() != null) {
                model.setMountOptions(entity.getMountOptions());
            }
        } else if (entity.getStorageType().equals(org.ovirt.engine.core.common.businessentities.storage.StorageType.POSIXFS)
                || entity.getStorageType().equals(org.ovirt.engine.core.common.businessentities.storage.StorageType.GLUSTERFS)) {
            model.setMountOptions(entity.getMountOptions());
            model.setVfsType(entity.getVfsType());
        }
        return model;
    }

    private static void setPath(StorageServerConnections entity, StorageConnection model) {
        if (entity.getConnection().startsWith("[")) {
            String[] parts = entity.getConnection().split("]:");
            model.setAddress(parts[0].concat("]"));
            model.setPath(parts[1]);
        } else if (entity.getConnection().contains(":")) {
            String[] parts = entity.getConnection().split(":");
            model.setAddress(parts[0]);
            model.setPath(parts[1]);
        } else {
            model.setPath(entity.getConnection());
        }
    }

    @Mapping(from = StorageType.class, to = org.ovirt.engine.core.common.businessentities.storage.StorageType.class)
    public static org.ovirt.engine.core.common.businessentities.storage.StorageType map(StorageType storageType,
            org.ovirt.engine.core.common.businessentities.storage.StorageType template) {
        switch (storageType) {
        case ISCSI:
            return org.ovirt.engine.core.common.businessentities.storage.StorageType.ISCSI;
        case FCP:
            return org.ovirt.engine.core.common.businessentities.storage.StorageType.FCP;
        case NFS:
            return org.ovirt.engine.core.common.businessentities.storage.StorageType.NFS;
        case LOCALFS:
            return org.ovirt.engine.core.common.businessentities.storage.StorageType.LOCALFS;
        case POSIXFS:
            return org.ovirt.engine.core.common.businessentities.storage.StorageType.POSIXFS;
        case GLUSTERFS:
            return org.ovirt.engine.core.common.businessentities.storage.StorageType.GLUSTERFS;
        case GLANCE:
            return org.ovirt.engine.core.common.businessentities.storage.StorageType.GLANCE;
        case CINDER:
            return org.ovirt.engine.core.common.businessentities.storage.StorageType.CINDER;
        case MANAGED_BLOCK_STORAGE:
            return org.ovirt.engine.core.common.businessentities.storage.StorageType.MANAGED_BLOCK_STORAGE;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.storage.StorageType.class, to = StorageType.class)
    public static StorageType map(org.ovirt.engine.core.common.businessentities.storage.StorageType storageType, StorageType template) {
        switch (storageType) {
        case ISCSI:
            return StorageType.ISCSI;
        case FCP:
            return StorageType.FCP;
        case NFS:
            return StorageType.NFS;
        case LOCALFS:
            return StorageType.LOCALFS;
        case POSIXFS:
            return StorageType.POSIXFS;
        case GLUSTERFS:
            return StorageType.GLUSTERFS;
        case GLANCE:
            return StorageType.GLANCE;
        case CINDER:
            return StorageType.CINDER;
        case MANAGED_BLOCK_STORAGE:
            return StorageType.MANAGED_BLOCK_STORAGE;
        default:
            return null;
        }
    }

    @Mapping(from = StorageDomainType.class, to = org.ovirt.engine.core.common.businessentities.StorageDomainType.class)
    public static org.ovirt.engine.core.common.businessentities.StorageDomainType map(
            StorageDomainType storageDomainType,
            org.ovirt.engine.core.common.businessentities.StorageDomainType template) {
        switch (storageDomainType) {
        case DATA:
            return org.ovirt.engine.core.common.businessentities.StorageDomainType.Data;
        case ISO:
            return org.ovirt.engine.core.common.businessentities.StorageDomainType.ISO;
        case EXPORT:
            return org.ovirt.engine.core.common.businessentities.StorageDomainType.ImportExport;
        case IMAGE:
            return org.ovirt.engine.core.common.businessentities.StorageDomainType.Image;
        case VOLUME:
            return org.ovirt.engine.core.common.businessentities.StorageDomainType.Volume;
        case MANAGED_BLOCK_STORAGE:
            return org.ovirt.engine.core.common.businessentities.StorageDomainType.ManagedBlockStorage;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.StorageDomainType.class, to = StorageDomainType.class)
    public static StorageDomainType map(org.ovirt.engine.core.common.businessentities.StorageDomainType storageDomainType,
            StorageDomainType template) {
        switch (storageDomainType) {
        case Master:
            return StorageDomainType.DATA;
        case Data:
            return StorageDomainType.DATA;
        case ISO:
            return StorageDomainType.ISO;
        case ImportExport:
            return StorageDomainType.EXPORT;
        case Image:
            return StorageDomainType.IMAGE;
        case Volume:
            return StorageDomainType.VOLUME;
        case Unknown:
        default:
            return null;
        }
    }

    public static StorageDomainStatus mapStorageDomainStatus(org.ovirt.engine.core.common.businessentities.StorageDomainStatus status) {
        switch (status) {
        case Unattached:
            return StorageDomainStatus.UNATTACHED;
        case Activating:
            return StorageDomainStatus.ACTIVATING;
        case Active:
            return StorageDomainStatus.ACTIVE;
        case Inactive:
            return StorageDomainStatus.INACTIVE;
        case Locked:
            return StorageDomainStatus.LOCKED;
        case PreparingForMaintenance:
            return StorageDomainStatus.PREPARING_FOR_MAINTENANCE;
        case Detaching:
            return StorageDomainStatus.DETACHING;
        case Maintenance:
            return StorageDomainStatus.MAINTENANCE;
        case Unknown:
            return StorageDomainStatus.UNKNOWN;
        case Uninitialized:
            return null;
        default:
            return null;
        }
    }

    @Mapping(from = NfsVersion.class, to = org.ovirt.engine.core.common.businessentities.NfsVersion.class)
    public static org.ovirt.engine.core.common.businessentities.NfsVersion map(NfsVersion version,
            org.ovirt.engine.core.common.businessentities.NfsVersion outgoing) {
        switch (version) {
        case V3:
            return org.ovirt.engine.core.common.businessentities.NfsVersion.V3;
        case V4:
            return org.ovirt.engine.core.common.businessentities.NfsVersion.V4;
        case V4_0:
            return org.ovirt.engine.core.common.businessentities.NfsVersion.V4_0;
        case V4_1:
            return org.ovirt.engine.core.common.businessentities.NfsVersion.V4_1;
        case V4_2:
            return org.ovirt.engine.core.common.businessentities.NfsVersion.V4_2;
        case AUTO:
            return org.ovirt.engine.core.common.businessentities.NfsVersion.AUTO;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.NfsVersion.class, to = NfsVersion.class)
    public static NfsVersion map(org.ovirt.engine.core.common.businessentities.NfsVersion version, NfsVersion outgoing) {
        switch(version) {
        case V3:
            return NfsVersion.V3;
        case V4:
            return NfsVersion.V4;
        case V4_0:
            return NfsVersion.V4_0;
        case V4_1:
            return NfsVersion.V4_1;
        case V4_2:
            return NfsVersion.V4_2;
        case AUTO:
            return NfsVersion.AUTO;
        default:
            return null;
        }
    }
}
