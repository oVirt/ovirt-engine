package org.ovirt.engine.api.restapi.types;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.NfsVersion;
import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomainStatus;
import org.ovirt.engine.api.model.StorageDomainType;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.model.VolumeGroup;
import org.ovirt.engine.api.restapi.model.StorageFormat;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
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
            StorageDomainType storageDomainType = StorageDomainType.fromValue(model.getType());
            if (storageDomainType != null) {
                entity.setStorageDomainType(map(storageDomainType, null));
            }
        }
        if (model.isSetStorage() && model.getStorage().isSetType()) {
            StorageType storageType = StorageType.fromValue(model.getStorage().getType());
            if (storageType != null) {
                entity.setStorageType(map(storageType, null));
            }
        }
        if (model.isSetStorageFormat()) {
            StorageFormat storageFormat = StorageFormat.fromValue(model.getStorageFormat());
            if (storageFormat != null) {
                entity.setStorageFormat(StorageFormatMapper.map(storageFormat, null));
            }
        }
        return entity;
    }

    @Mapping(from = StorageDomain.class, to = StorageServerConnections.class)
    public static StorageServerConnections map(StorageDomain model, StorageServerConnections template) {
        StorageServerConnections entity = template != null ? template : new StorageServerConnections();
        if (model.isSetStorage() && model.getStorage().isSetType()) {
            Storage storage = model.getStorage();
            StorageType storageType = StorageType.fromValue(storage.getType());
            if (storageType != null) {
                entity.setstorage_type(map(storageType, null));
                switch (storageType) {
                case ISCSI:
                    break;
                case FCP:
                    break;
                case GLANCE:
                    break;
                case NFS:
                    if(storage.isSetAddress() && storage.isSetPath()) {
                        entity.setconnection(storage.getAddress() + ":" + storage.getPath());
                    }
                    if(storage.getNfsRetrans() != null) {
                        entity.setNfsRetrans(storage.getNfsRetrans().shortValue());
                    }
                    if(storage.getNfsTimeo() != null) {
                        entity.setNfsTimeo(storage.getNfsTimeo().shortValue());
                    }
                    if(storage.getNfsVersion() != null) {
                        NfsVersion nfsVersion = NfsVersion.fromValue(storage.getNfsVersion());
                        if (nfsVersion != null) {
                            entity.setNfsVersion(map(nfsVersion, null));
                        }
                    }
                    if (storage.isSetMountOptions()) {
                        entity.setMountOptions(storage.getMountOptions());
                    }
                    break;
                case LOCALFS:
                    if (storage.isSetPath()) {
                        entity.setconnection(storage.getPath());
                    }
                    break;
                case POSIXFS:
                case GLUSTERFS:
                    if (storage.isSetAddress() && storage.isSetPath()) {
                        entity.setconnection(storage.getAddress() + ":" + storage.getPath());
                    } else if (storage.isSetPath()) {
                        entity.setconnection(storage.getPath());
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
        model.setMaster(entity.getStorageDomainType() == org.ovirt.engine.core.common.businessentities.StorageDomainType.Master);
        if (entity.getStatus() != null) {
            StorageDomainStatus status = map(entity.getStatus(), null);
            model.setStatus(status==null ? null : StatusUtils.create(status));
        }
        model.setStorage(new Storage());
        model.getStorage().setType(map(entity.getStorageType(), null));
        if (entity.getStorageType() == org.ovirt.engine.core.common.businessentities.StorageType.ISCSI ||
                entity.getStorageType() == org.ovirt.engine.core.common.businessentities.StorageType.FCP) {
            model.getStorage().setVolumeGroup(new VolumeGroup());
            model.getStorage().getVolumeGroup().setId(entity.getStorage());
        }
        if (entity.getAvailableDiskSize()!= null) {
            model.setAvailable(SizeConverter.convert(entity.getAvailableDiskSize().longValue(),
                    SizeConverter.SizeUnit.GB, SizeConverter.SizeUnit.BYTES).longValue());
        }
        if (entity.getUsedDiskSize()!= null) {
            model.setUsed(SizeConverter.convert(entity.getUsedDiskSize().longValue(),
                    SizeConverter.SizeUnit.GB, SizeConverter.SizeUnit.BYTES).longValue());
        }
        model.setCommitted(SizeConverter.convert(entity.getCommittedDiskSize(),
                SizeConverter.SizeUnit.GB, SizeConverter.SizeUnit.BYTES).longValue());
        if (entity.getStorageFormat()!= null) {
            String storageFormat = StorageFormatMapper.map(entity.getStorageFormat(), null).value();
            if (storageFormat != null) {
                model.setStorageFormat(storageFormat);
            }
        }
        return model;
    }

    @Mapping(from = StorageConnection.class, to = StorageServerConnections.class)
    public static StorageServerConnections map(StorageConnection model, StorageServerConnections template) {
        StorageServerConnections entity = template != null ? template : new StorageServerConnections();
        if (model.isSetId()) {
            entity.setid(model.getId());
        }
        org.ovirt.engine.core.common.businessentities.StorageType storageType = null;
        if (model.getType() != null) {
           storageType = map(StorageType.fromValue(model.getType()), null);
        }
        else if (template != null) {
           storageType = template.getstorage_type();
        }
        if (storageType != null) {
            entity.setstorage_type(storageType);
            switch (storageType) {
            case ISCSI:
                if (model.isSetAddress()) {
                    entity.setconnection(model.getAddress());
                }
                if (model.isSetPort()) {
                    entity.setport(model.getPort().toString());
                }
                if (model.isSetUsername()) {
                    entity.setuser_name(model.getUsername());
                }
                if (model.isSetTarget()) {
                    entity.setiqn(model.getTarget());
                }
                if (model.isSetPassword()) {
                    entity.setpassword(model.getPassword());
                }
                break;
            case FCP:
                break;
            case NFS:
                // in case of update, one or both of the address/path fields might be updated.
                // thus, need to take care of their assignment separately since they are merged
                // in the backend into a single field.
                String[] parts = null;
                if (!StringUtils.isEmpty(entity.getconnection())) {
                    parts = entity.getconnection().split(":");
                }
                String address = null;
                String path = null;
                if (model.isSetAddress()) {
                    address = model.getAddress();
                }
                else {
                    address = parts != null ? parts[0] : "";
                }
                if (model.isSetPath()) {
                    path = model.getPath();
                }
                else {
                    path = parts != null ? parts[1] : "";
                }
                entity.setconnection(address + ":" + path);

                if (model.getNfsRetrans() != null) {
                    entity.setNfsRetrans(model.getNfsRetrans().shortValue());
                }
                if (model.getNfsTimeo() != null) {
                    entity.setNfsTimeo(model.getNfsTimeo().shortValue());
                }
                if (model.getNfsVersion() != null) {
                    NfsVersion nfsVersion = NfsVersion.fromValue(model.getNfsVersion());
                    if (nfsVersion != null) {
                        entity.setNfsVersion(map(nfsVersion, null));
                    }
                }
                if (model.isSetMountOptions()) {
                    entity.setMountOptions(model.getMountOptions());
                }
                break;
            case LOCALFS:
                if (model.isSetPath()) {
                    entity.setconnection(model.getPath());
                }
                break;
            case POSIXFS:
            case GLUSTERFS:
                if (model.isSetAddress() && model.isSetPath()) {
                    entity.setconnection(model.getAddress() + ":" + model.getPath());
                } else if (model.isSetPath()) {
                    entity.setconnection(model.getPath());
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
        model.setId(entity.getid());
        model.setType(map(entity.getstorage_type(), null));
        if (entity.getstorage_type() == org.ovirt.engine.core.common.businessentities.StorageType.ISCSI) {
            model.setAddress(entity.getconnection());
            model.setPort(Integer.parseInt(entity.getport()));
            model.setUsername(entity.getuser_name());
            model.setTarget(entity.getiqn());
        }
        if (entity.getstorage_type().isFileDomain()) {
            setPath(entity, model);
        }
        if (entity.getstorage_type().equals(org.ovirt.engine.core.common.businessentities.StorageType.NFS)) {
            if (entity.getNfsVersion() != null) {
                model.setNfsVersion(entity.getNfsVersion().toString());
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
        }
        else if (entity.getstorage_type().equals(org.ovirt.engine.core.common.businessentities.StorageType.POSIXFS)
                || entity.getstorage_type().equals(org.ovirt.engine.core.common.businessentities.StorageType.GLUSTERFS)) {
            model.setMountOptions(entity.getMountOptions());
            model.setVfsType(entity.getVfsType());
        }
        return model;
    }

    private static void setPath(StorageServerConnections entity, StorageConnection model) {
        if (entity.getconnection().contains(":")) {
            String[] parts = entity.getconnection().split(":");
            model.setAddress(parts[0]);
            model.setPath(parts[1]);
        }
        else {
            model.setPath(entity.getconnection());
        }
    }

    @Mapping(from = StorageType.class, to = org.ovirt.engine.core.common.businessentities.StorageType.class)
    public static org.ovirt.engine.core.common.businessentities.StorageType map(StorageType storageType,
            org.ovirt.engine.core.common.businessentities.StorageType template) {
        switch (storageType) {
        case ISCSI:
            return org.ovirt.engine.core.common.businessentities.StorageType.ISCSI;
        case FCP:
            return org.ovirt.engine.core.common.businessentities.StorageType.FCP;
        case NFS:
            return org.ovirt.engine.core.common.businessentities.StorageType.NFS;
        case LOCALFS:
            return org.ovirt.engine.core.common.businessentities.StorageType.LOCALFS;
        case POSIXFS:
            return org.ovirt.engine.core.common.businessentities.StorageType.POSIXFS;
        case GLUSTERFS:
            return org.ovirt.engine.core.common.businessentities.StorageType.GLUSTERFS;
        case GLANCE:
            return org.ovirt.engine.core.common.businessentities.StorageType.GLANCE;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.StorageType.class, to = String.class)
    public static String map(org.ovirt.engine.core.common.businessentities.StorageType storageType, String template) {
        switch (storageType) {
        case ISCSI:
            return StorageType.ISCSI.value();
        case FCP:
            return StorageType.FCP.value();
        case NFS:
            return StorageType.NFS.value();
        case LOCALFS:
            return StorageType.LOCALFS.value();
        case POSIXFS:
            return StorageType.POSIXFS.value();
        case GLUSTERFS:
            return StorageType.GLUSTERFS.value();
        case GLANCE:
            return StorageType.GLANCE.value();
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
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.StorageDomainType.class, to = String.class)
    public static String map(org.ovirt.engine.core.common.businessentities.StorageDomainType storageDomainType,
            String template) {
        switch (storageDomainType) {
        case Master:
            return StorageDomainType.DATA.value();
        case Data:
            return StorageDomainType.DATA.value();
        case ISO:
            return StorageDomainType.ISO.value();
        case ImportExport:
            return StorageDomainType.EXPORT.value();
        case Image:
            return StorageDomainType.IMAGE.value();
        case Unknown:
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.StorageDomainStatus.class,
            to = StorageDomainStatus.class)
    public static StorageDomainStatus map(
            org.ovirt.engine.core.common.businessentities.StorageDomainStatus status,
            StorageDomainStatus template) {
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
        case AUTO:
            return org.ovirt.engine.core.common.businessentities.NfsVersion.AUTO;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.NfsVersion.class, to = String.class)
    public static String map(org.ovirt.engine.core.common.businessentities.NfsVersion version, String outgoing) {
        switch(version) {
        case V3:
            return NfsVersion.V3.value();
        case V4:
            return NfsVersion.V4.value();
        case AUTO:
            return NfsVersion.AUTO.value();
        default:
            return null;
        }
    }
}
