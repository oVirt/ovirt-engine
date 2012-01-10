package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.common.util.SizeConverter;
import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomainStatus;
import org.ovirt.engine.api.model.StorageDomainType;

import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.model.VolumeGroup;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.api.restapi.model.StorageFormat;

public class StorageDomainMapper {

    @Mapping(from = StorageDomain.class, to = storage_domain_static.class)
    public static storage_domain_static map(StorageDomain model, storage_domain_static template) {
        storage_domain_static entity = template != null ? template : new storage_domain_static();
        if (model.isSetId()) {
            entity.setId(new Guid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setstorage_name(model.getName());
        }
        // REVIST No descriptions for storage domains
        // if (model.isSetDescription()) {
        // entity.setdescription(model.getDescription());
        // }
        if (model.isSetType()) {
            StorageDomainType storageDomainType = StorageDomainType.fromValue(model.getType());
            if (storageDomainType != null) {
                entity.setstorage_domain_type(map(storageDomainType, null));
            }
        }
        if (model.isSetStorage() && model.getStorage().isSetType()) {
            StorageType storageType = StorageType.fromValue(model.getStorage().getType());
            if (storageType != null) {
                entity.setstorage_type(map(storageType, null));
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

    @Mapping(from = StorageDomain.class, to = storage_server_connections.class)
    public static storage_server_connections map(StorageDomain model, storage_server_connections template) {
        storage_server_connections entity = template != null ? template : new storage_server_connections();
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
                case NFS:
                    if (storage.isSetAddress() && storage.isSetPath()) {
                        entity.setconnection(storage.getAddress() + ":" + storage.getPath());
                    }
                    break;
                case LOCALFS:
                    if (storage.isSetPath()) {
                        entity.setconnection(storage.getPath());
                    }
                default:
                    break;
                }
            }
        }
        return entity;
    }

    @Mapping(from = storage_domains.class, to = StorageDomain.class)
    public static StorageDomain map(storage_domains entity, StorageDomain template) {
        StorageDomain model = template != null ? template : new StorageDomain();
        model.setId(entity.getid().toString());
        model.setName(entity.getstorage_name());
        // REVIST No descriptions for storage domains
        // model.setDescription(entity.getdescription());
        model.setType(map(entity.getstorage_domain_type(), null));
        model.setMaster(entity.getstorage_domain_type() == org.ovirt.engine.core.common.businessentities.StorageDomainType.Master);
        if (entity.getstatus() != null) {
            StorageDomainStatus status = map(entity.getstatus(), null);
            model.setStatus(status==null ? null : StatusUtils.create(status));
        }
        model.setStorage(new Storage());
        model.getStorage().setType(map(entity.getstorage_type(), null));
        if (entity.getstorage_type() == org.ovirt.engine.core.common.businessentities.StorageType.ISCSI ||
            entity.getstorage_type() == org.ovirt.engine.core.common.businessentities.StorageType.FCP) {
            model.getStorage().setVolumeGroup(new VolumeGroup());
            model.getStorage().getVolumeGroup().setId(entity.getstorage());
        }
        if (entity.getavailable_disk_size()!=null) {
            model.setAvailable(SizeConverter.gigasToBytes(entity.getavailable_disk_size().longValue()));
        }
        if (entity.getused_disk_size()!=null) {
            model.setUsed(SizeConverter.gigasToBytes(entity.getused_disk_size().longValue()));
        }
        model.setCommitted(SizeConverter.gigasToBytes(entity.getcommitted_disk_size()));
        if (entity.getStorageFormat()!=null) {
            String storageForamt = StorageFormatMapper.map(entity.getStorageFormat(), null).value();
            if (storageForamt!=null) {
                model.setStorageFormat(storageForamt);
            }
        }
        return model;
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
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.StorageDomainType.class, to = String.class)
    public static String map(org.ovirt.engine.core.common.businessentities.StorageDomainType storageDomainType, String template) {
        switch (storageDomainType) {
        case Master:
            return StorageDomainType.DATA.value();
        case Data:
            return StorageDomainType.DATA.value();
        case ISO:
            return StorageDomainType.ISO.value();
        case ImportExport:
            return StorageDomainType.EXPORT.value();
        case Unknown:
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.StorageDomainStatus.class, to = StorageDomainStatus.class)
    public static StorageDomainStatus map(
            org.ovirt.engine.core.common.businessentities.StorageDomainStatus status,
            StorageDomainStatus template) {
        switch (status) {
        case Unattached:
            return StorageDomainStatus.UNATTACHED;
        case Active:
            return StorageDomainStatus.ACTIVE;
        case InActive:
            return StorageDomainStatus.INACTIVE;
        case Locked:
            return StorageDomainStatus.LOCKED;
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
}
