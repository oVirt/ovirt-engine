package org.ovirt.engine.api.restapi.types;

import java.util.ArrayList;

import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskFormat;
import org.ovirt.engine.api.model.DiskInterface;
import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.api.model.DiskStatus;
import org.ovirt.engine.api.model.Quota;
import org.ovirt.engine.api.model.ScsiGenericIO;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.storage.Disk.DiskStorageType;
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
            } else {
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
        if (disk.isSetBootable()) {
            engineDisk.setBoot(disk.isBootable());
        }
        if (disk.isSetPropagateErrors()) {
            engineDisk.setPropagateErrors(disk.isPropagateErrors() ? PropagateErrors.On
                    : PropagateErrors.Off);
        }
        if (disk.isSetWipeAfterDelete()) {
            engineDisk.setWipeAfterDelete(disk.isWipeAfterDelete());
        }
        if (disk.isSetActive()) {
            engineDisk.setPlugged(disk.isActive());
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

        if (disk.isSetInterface()) {
            DiskInterface diskInterface = DiskInterface.fromValue(disk.getInterface());
            if (diskInterface != null) {
                engineDisk.setDiskInterface(map(diskInterface, null));
            }
        }
        if (disk.isSetShareable()) {
            engineDisk.setShareable(disk.isShareable());
        }
        if (disk.isSetLunStorage()) {
            ((LunDisk)engineDisk).setLun(StorageLogicalUnitMapper.map(disk.getLunStorage(), null));
            if (disk.isSetSgio() && engineDisk.getDiskInterface() == map(DiskInterface.VIRTIO_SCSI, null)) {
                ScsiGenericIO scsiGenericIO = ScsiGenericIO.fromValue(disk.getSgio());
                if (scsiGenericIO != null) {
                    engineDisk.setSgio(map(scsiGenericIO, null));
                }
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
        //Notice below:
        //Both <size> and <provisioned_size> are mapped to the same field: 'size' in the
        //Backend entity. This is not by mistake. Provisioned_size was added recently because
        //it's a more correct term, but 'size' was there initially and must be supported for
        //backwards compatibility.
        //
        //So what we do is:
        //if user gives <size> XOR <provisioned_size>, then the value the user gave will
        //simply be mapped to BE-->'size'. If user passed <size> AND <provisioned_size>,
        //then the value in <provision_size> will be dominant, since it is mapped second
        //and overrides the value in <size>
        if (disk.isSetSize()) {
            diskImage.setSize(disk.getSize());
        }
        if (disk.isSetProvisionedSize()) {
            diskImage.setSize(disk.getProvisionedSize());
        }
        if (disk.isSetFormat()) {
            DiskFormat diskFormat = DiskFormat.fromValue(disk.getFormat());
            if (diskFormat != null) {
                diskImage.setvolumeFormat(map(diskFormat, null));
            }
        }
        if (disk.isSetStatus()) {
            diskImage.setImageStatus(map(DiskStatus.fromValue(disk.getStatus().getState())));
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
            diskImage.setStorageIds(new ArrayList<Guid>());
            diskImage.getStorageIds().add(Guid.createGuidFromStringDefaultEmpty(storageDomain.getId()));
        }
        if (disk.isSetQuota() && disk.getQuota().isSetId()) {
            diskImage.setQuotaId(GuidUtils.asGuid(disk.getQuota().getId()));
        }
        if (disk.isSetDiskProfile() && disk.getDiskProfile().isSetId()) {
            diskImage.setDiskProfileId(GuidUtils.asGuid(disk.getDiskProfile().getId()));
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
        if (entity.getDiskInterface() != null) {
            model.setInterface(map(entity.getDiskInterface(), null));
        }
        model.setBootable(entity.isBoot());
        model.setPropagateErrors(PropagateErrors.On == entity.getPropagateErrors());
        model.setWipeAfterDelete(entity.isWipeAfterDelete());
        model.setActive(entity.getPlugged());
        model.setReadOnly(entity.getReadOnly());
        model.setShareable(entity.isShareable());
        model.setDescription(entity.getDiskDescription());
        model.setLogicalName(entity.getLogicalName());
        if (entity.getDiskStorageType() == DiskStorageType.IMAGE) {
            mapDiskImageToDiskFields((DiskImage) entity, model);
        } else {
            model.setLunStorage(StorageLogicalUnitMapper.map(((LunDisk) entity).getLun(), new Storage()));
            if (entity.getSgio() != null && entity.getDiskInterface() == map(DiskInterface.VIRTIO_SCSI, null)) {
                model.setSgio(map(entity.getSgio(), null));
            }
        }
        return model;
    }

    private static void mapDiskImageToDiskFields(DiskImage entity, Disk model) {
        if (entity.getImageId() != null) {
            model.setImageId(entity.getImageId().toString());
        }
        model.setSize(entity.getSize());
        model.setProvisionedSize(entity.getSize());
        model.setActualSize(entity.getActualSizeInBytes());

        if (entity.getSnapshotId() != null) {
            model.setSnapshot(new Snapshot());
            model.getSnapshot().setId(entity.getSnapshotId().toString());
        }

        if (entity.getVolumeFormat() != null) {
            model.setFormat(map(entity.getVolumeFormat(), null));
        }
        if (entity.getImageStatus() != null) {
            DiskStatus status = map(entity.getImageStatus());
            model.setStatus(StatusUtils.create(status == null ? null : status.value()));
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

    @Mapping(from = VolumeFormat.class, to = String.class)
    public static String map(VolumeFormat volumeFormat, String template) {
        switch (volumeFormat) {
        case COW:
            return DiskFormat.COW.value();
        case RAW:
            return DiskFormat.RAW.value();
        default:
            return null;
        }
    }

    @Mapping(from = DiskInterface.class, to = org.ovirt.engine.core.common.businessentities.storage.DiskInterface.class)
    public static org.ovirt.engine.core.common.businessentities.storage.DiskInterface map(
            DiskInterface diskInterface,
            org.ovirt.engine.core.common.businessentities.storage.DiskInterface template) {
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
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.storage.DiskInterface.class, to = String.class)
    public static String map(org.ovirt.engine.core.common.businessentities.storage.DiskInterface diskInterface, String template) {
        switch (diskInterface) {
        case IDE:
            return DiskInterface.IDE.value();
        case VirtIO:
            return DiskInterface.VIRTIO.value();
        case VirtIO_SCSI:
            return DiskInterface.VIRTIO_SCSI.value();
        case SPAPR_VSCSI:
            return DiskInterface.SPAPR_VSCSI.value();
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

    @Mapping(from = org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO.class, to = String.class)
    public static String map(org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO scsiGenericIO, String template) {
        switch (scsiGenericIO) {
        case FILTERED:
            return ScsiGenericIO.FILTERED.value();
        case UNFILTERED:
            return ScsiGenericIO.UNFILTERED.value();
        default:
            return null;
        }
    }

    @Mapping(from = DiskStatus.class, to = ImageStatus.class)
    public static ImageStatus map(DiskStatus diskStatus) {
        if (diskStatus==null) {
            return null;
        } else {
            switch (diskStatus) {
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
    }

    @Mapping(from = ImageStatus.class, to = DiskStatus.class)
    public static DiskStatus map(ImageStatus imageStatus) {
        switch (imageStatus) {
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
}
