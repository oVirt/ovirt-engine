package org.ovirt.engine.api.restapi.types;

import java.util.ArrayList;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskFormat;
import org.ovirt.engine.api.model.DiskInterface;
import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.api.model.DiskStatus;
import org.ovirt.engine.api.model.DiskStorageType;
import org.ovirt.engine.api.model.HostStorage;
import org.ovirt.engine.api.model.OpenStackVolumeType;
import org.ovirt.engine.api.model.Quota;
import org.ovirt.engine.api.model.ScsiGenericIO;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;

public class DiskMapper {

    @Mapping(from = Disk.class, to = org.ovirt.engine.core.common.businessentities.storage.Disk.class)
    public static org.ovirt.engine.core.common.businessentities.storage.Disk map(Disk disk, org.ovirt.engine.core.common.businessentities.storage.Disk template) {
        org.ovirt.engine.core.common.businessentities.storage.Disk engineDisk = template;
        if (engineDisk == null) {
            if (disk.isSetLunStorage()) {
                engineDisk = new LunDisk();
            } else if (disk.getStorageType() != null) {
                DiskStorageType diskStorageType = disk.getStorageType();
                switch (diskStorageType) {
                case CINDER:
                    engineDisk = new CinderDisk();
                    break;
                case IMAGE:
                    engineDisk = new DiskImage();
                    break;
                }
            }
            if (engineDisk == null) {
                engineDisk = new DiskImage();
            }
        }
        // name is depreciated, use alias instead.
        if (disk.isSetName()) {
            engineDisk.setDiskAlias(disk.getName());
        }
        if (disk.isSetAlias()) {
            engineDisk.setDiskAlias(disk.getAlias());
        }
        if (disk.isSetId()) {
            engineDisk.setId(GuidUtils.asGuid(disk.getId()));
        }
        if (disk.isSetPropagateErrors()) {
            engineDisk.setPropagateErrors(disk.isPropagateErrors() ? PropagateErrors.On
                    : PropagateErrors.Off);
        }
        if (disk.isSetWipeAfterDelete()) {
            engineDisk.setWipeAfterDelete(disk.isWipeAfterDelete());
        }
        if (disk.isSetReadOnly()) {
            engineDisk.setReadOnly(disk.isReadOnly());
        }

        if (disk.isSetLogicalName()) {
            engineDisk.setLogicalName(disk.getLogicalName());
        }

        if (disk.isSetDescription()) {
            engineDisk.setDiskDescription(disk.getDescription());
        }

        if (disk.isSetShareable()) {
            engineDisk.setShareable(disk.isShareable());
        }
        if (!engineDisk.getDiskStorageType().isInternal()) {
            if (disk.isSetLunStorage()) {
                ((LunDisk) engineDisk).setLun(StorageLogicalUnitMapper.map(disk.getLunStorage(), null));
            }
            if (disk.isSetUsesScsiReservation()) {
                ((LunDisk) engineDisk).setUsingScsiReservation(disk.isUsesScsiReservation());
            }
            if (disk.isSetSgio()) {
                engineDisk.setSgio(map(disk.getSgio(), null));
            }
        } else {
            mapDiskToDiskImageProperties(disk, (DiskImage) engineDisk);
        }
        return engineDisk;
    }

    private static void mapDiskToDiskImageProperties(Disk disk,
            DiskImage diskImage) {
        if (disk.isSetImageId()) {
            diskImage.setImageId(GuidUtils.asGuid(disk.getImageId()));
        }
        if (disk.isSetProvisionedSize()) {
            diskImage.setSize(disk.getProvisionedSize());
        }
        if (disk.isSetFormat()) {
            diskImage.setVolumeFormat(map(disk.getFormat(), null));
        }
        if (disk.isSetStatus()) {
            diskImage.setImageStatus(mapDiskStatus(disk.getStatus()));
        }
        if (disk.isSetSnapshot() && disk.getSnapshot().isSetId()) {
            diskImage.setVmSnapshotId(GuidUtils.asGuid(disk.getSnapshot().getId()));
        }
        if (disk.isSetSparse()) {
            diskImage.setVolumeType(disk.isSparse() ? VolumeType.Sparse : VolumeType.Preallocated);
        }
        if (disk.isSetStorageDomains() && disk.getStorageDomains().isSetStorageDomains()
                && disk.getStorageDomains().getStorageDomains().get(0).isSetId()) {
            StorageDomain storageDomain = disk.getStorageDomains().getStorageDomains().get(0);
            diskImage.setStorageIds(new ArrayList<>());
            diskImage.getStorageIds().add(Guid.createGuidFromStringDefaultEmpty(storageDomain.getId()));
        }
        if (disk.isSetQuota() && disk.getQuota().isSetId()) {
            diskImage.setQuotaId(GuidUtils.asGuid(disk.getQuota().getId()));
        }
        if (disk.isSetDiskProfile() && disk.getDiskProfile().isSetId()) {
            diskImage.setDiskProfileId(GuidUtils.asGuid(disk.getDiskProfile().getId()));
        }
        if (disk.isSetOpenstackVolumeType() && disk.getOpenstackVolumeType().isSetName()) {
            diskImage.setCinderVolumeType(disk.getOpenstackVolumeType().getName());
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.storage.Disk.class, to = Disk.class)
    public static Disk map(org.ovirt.engine.core.common.businessentities.storage.Disk entity, Disk template) {
        Disk model = template != null ? template : new Disk();
        // name is depreciated, use alias instead.
        model.setName(entity.getDiskAlias());
        model.setAlias(entity.getDiskAlias());
        if (entity.getId() != null) {
            model.setId(entity.getId().toString());
        }
        model.setPropagateErrors(PropagateErrors.On == entity.getPropagateErrors());
        model.setWipeAfterDelete(entity.isWipeAfterDelete());
        model.setReadOnly(entity.getReadOnly());
        model.setShareable(entity.isShareable());
        model.setDescription(entity.getDiskDescription());
        model.setLogicalName(entity.getLogicalName());
        model.setStorageType(map(entity.getDiskStorageType()));
        if (entity.getDiskStorageType() == org.ovirt.engine.core.common.businessentities.storage.DiskStorageType.IMAGE ||
                entity.getDiskStorageType() == org.ovirt.engine.core.common.businessentities.storage.DiskStorageType.CINDER) {
            mapDiskImageToDiskFields((DiskImage) entity, model);
        } else {
            model.setLunStorage(StorageLogicalUnitMapper.map(((LunDisk) entity).getLun(), new HostStorage()));
            model.setUsesScsiReservation(((LunDisk) entity).isUsingScsiReservation());
            if (entity.getSgio() != null) {
                model.setSgio(map(entity.getSgio(), null));
            }
        }
        return model;
    }

    private static void mapDiskImageToDiskFields(DiskImage entity, Disk model) {
        if (entity.getImageId() != null) {
            model.setImageId(entity.getImageId().toString());
        }
        model.setProvisionedSize(entity.getSize());
        if (entity.hasActualSize()) {
            model.setActualSize(entity.getActualSizeInBytes());
        }

        if (entity.getSnapshotId() != null) {
            model.setSnapshot(new Snapshot());
            model.getSnapshot().setId(entity.getSnapshotId().toString());
        }

        if (entity.getVolumeFormat() != null) {
            model.setFormat(map(entity.getVolumeFormat(), null));
        }
        if (entity.getImageStatus() != null) {
            model.setStatus(mapDiskStatus(entity.getImageStatus()));
        }
        model.setSparse(VolumeType.Sparse == entity.getVolumeType());
        if (entity.getStorageIds() != null && entity.getStorageIds().size() > 0) {
            if (!model.isSetStorageDomains()) {
                model.setStorageDomains(new StorageDomains());
            }
            for (Guid id : entity.getStorageIds()){
                StorageDomain storageDomain = new StorageDomain();
                storageDomain.setId(id.toString());
                model.getStorageDomains().getStorageDomains().add(storageDomain);
            }
        }
        if (entity.getQuotaId()!=null) {
            Quota quota = new Quota();
            quota.setId(entity.getQuotaId().toString());
            model.setQuota(quota);
        }
        if (entity.getDiskProfileId() != null) {
            DiskProfile diskProfile = new DiskProfile();
            diskProfile.setId(entity.getDiskProfileId().toString());
            model.setDiskProfile(diskProfile);
        }
        if (entity.getCinderVolumeType() != null) {
            OpenStackVolumeType volumeType = model.getOpenstackVolumeType();
            if (volumeType == null) {
                volumeType = new OpenStackVolumeType();
                model.setOpenstackVolumeType(volumeType);
            }
            volumeType.setName(entity.getCinderVolumeType());
        }
    }

    @Mapping(from = DiskFormat.class, to = String.class)
    public static VolumeFormat map(DiskFormat diskFormat, VolumeFormat template) {
        switch (diskFormat) {
        case COW:
            return VolumeFormat.COW;
        case RAW:
            return VolumeFormat.RAW;
        default:
            return VolumeFormat.Unassigned;
        }
    }

    @Mapping(from = VolumeFormat.class, to = DiskFormat.class)
    public static DiskFormat map(VolumeFormat volumeFormat, DiskFormat template) {
        switch (volumeFormat) {
        case COW:
            return DiskFormat.COW;
        case RAW:
            return DiskFormat.RAW;
        default:
            return null;
        }
    }

    @Mapping(from = ScsiGenericIO.class, to = org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO.class)
    public static org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO map(
            ScsiGenericIO scsiGenericIO,
            org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO template) {
        switch (scsiGenericIO) {
        case FILTERED:
            return org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO.FILTERED;
        case UNFILTERED:
            return org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO.UNFILTERED;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO.class, to = ScsiGenericIO.class)
    public static ScsiGenericIO map(org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO scsiGenericIO, ScsiGenericIO template) {
        switch (scsiGenericIO) {
        case FILTERED:
            return ScsiGenericIO.FILTERED;
        case UNFILTERED:
            return ScsiGenericIO.UNFILTERED;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.storage.DiskStorageType.class, to = DiskStorageType.class)
    public static DiskStorageType map(org.ovirt.engine.core.common.businessentities.storage.DiskStorageType diskStorageType) {
        switch (diskStorageType) {
            case IMAGE:
                return DiskStorageType.IMAGE;
            case CINDER:
                return DiskStorageType.CINDER;
            case LUN:
                return DiskStorageType.LUN;
            default:
                return null;
        }
    }

    private static ImageStatus mapDiskStatus(DiskStatus status) {
        if (status == null) {
            return null;
        }
        switch (status) {
        case ILLEGAL:
            return ImageStatus.ILLEGAL;
        case LOCKED:
            return ImageStatus.LOCKED;
        case OK:
            return ImageStatus.OK;
        default:
            return null;
        }
    }

    private static DiskStatus mapDiskStatus(ImageStatus status) {
        switch (status) {
        case ILLEGAL:
            return DiskStatus.ILLEGAL;
        case LOCKED:
            return DiskStatus.LOCKED;
        case OK:
            return DiskStatus.OK;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.StorageDomainType.class, to = DiskStorageType.class)
    public static DiskStorageType map(org.ovirt.engine.core.common.businessentities.StorageDomainType storageDomainType) {
        switch (storageDomainType) {
            case Volume:
                return DiskStorageType.CINDER;
            default:
                return DiskStorageType.IMAGE;
        }
    }

    public static org.ovirt.engine.core.common.businessentities.storage.DiskInterface mapInterface(DiskInterface diskInterface) {
        if (diskInterface == null) {
            return null;
        }
        switch (diskInterface) {
        case IDE:
            return org.ovirt.engine.core.common.businessentities.storage.DiskInterface.IDE;
        case VIRTIO:
            return org.ovirt.engine.core.common.businessentities.storage.DiskInterface.VirtIO;
        case VIRTIO_SCSI:
            return org.ovirt.engine.core.common.businessentities.storage.DiskInterface.VirtIO_SCSI;
        case SPAPR_VSCSI:
            return org.ovirt.engine.core.common.businessentities.storage.DiskInterface.SPAPR_VSCSI;
        default:
            throw new IllegalArgumentException("Unknown disk interface \"" + diskInterface + "\"");
        }
    }

    public static DiskInterface mapInterface(org.ovirt.engine.core.common.businessentities.storage.DiskInterface diskInterface) {
        if (diskInterface == null) {
            return null;
        }
        switch (diskInterface) {
        case IDE:
            return DiskInterface.IDE;
        case VirtIO:
            return DiskInterface.VIRTIO;
        case VirtIO_SCSI:
            return DiskInterface.VIRTIO_SCSI;
        case SPAPR_VSCSI:
            return DiskInterface.SPAPR_VSCSI;
        default:
            throw new IllegalArgumentException("Unknown disk interface \"" + diskInterface + "\"");
        }
    }
}
