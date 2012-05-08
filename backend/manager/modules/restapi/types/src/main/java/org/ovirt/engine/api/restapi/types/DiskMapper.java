package org.ovirt.engine.api.restapi.types;

import java.util.ArrayList;

import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskFormat;
import org.ovirt.engine.api.model.DiskInterface;
import org.ovirt.engine.api.model.DiskStatus;
import org.ovirt.engine.api.model.Quota;
import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;

public class DiskMapper {

    @Mapping(from = Disk.class, to = org.ovirt.engine.core.common.businessentities.Disk.class)
    public static org.ovirt.engine.core.common.businessentities.Disk map(Disk disk, org.ovirt.engine.core.common.businessentities.Disk template) {
        org.ovirt.engine.core.common.businessentities.Disk engineDisk = template;
        if (engineDisk == null) {
            if (disk.isSetLunStorage()) {
                engineDisk = new LunDisk();
            } else {
                engineDisk = new DiskImage();
            }
        }
        if (disk.isSetVm() && disk.getVm().isSetId()) {
            engineDisk.setvm_guid(new Guid(disk.getVm().getId()));
        }
        if (disk.isSetId()) {
            engineDisk.setId(new Guid(disk.getId()));
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
        } else {
            mapDiskToDiskImageProperties(disk, (DiskImage) engineDisk);
        }
        return engineDisk;
    }

    private static void mapDiskToDiskImageProperties(Disk disk,
            DiskImage diskImage) {
        if (disk.isSetImageId()) {
            diskImage.setImageId(new Guid(disk.getImageId()));
        }
        if (disk.isSetSize()) {
            diskImage.setsize(disk.getSize());
        }
        if (disk.isSetFormat()) {
            DiskFormat diskFormat = DiskFormat.fromValue(disk.getFormat());
            if (diskFormat != null) {
                diskImage.setvolume_format(map(diskFormat, null));
            }
        }
        if (disk.isSetStatus()) {
            diskImage.setimageStatus(map(DiskStatus.fromValue(disk.getStatus().getState())));
        }
        if (disk.isSetSparse()) {
            diskImage.setvolume_type(disk.isSparse() ? VolumeType.Sparse : VolumeType.Preallocated);
        }
        if (disk.isSetAllowSnapshot()) {
            diskImage.setAllowSnapshot(disk.isAllowSnapshot());
        }
        if (disk.isSetStorageDomains() && disk.getStorageDomains().isSetStorageDomains()
                && disk.getStorageDomains().getStorageDomains().get(0).isSetId()) {
            StorageDomain storageDomain = disk.getStorageDomains().getStorageDomains().get(0);
            diskImage.setstorage_ids(new ArrayList<Guid>());
            diskImage.getstorage_ids().add(Guid.createGuidFromString(storageDomain.getId()));
        }
        if (disk.isSetQuota() && disk.getQuota().isSetId()) {
            diskImage.setQuotaId(new Guid(disk.getQuota().getId()));
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.Disk.class, to = Disk.class)
    public static Disk map(org.ovirt.engine.core.common.businessentities.Disk entity, Disk template) {
        Disk model = template != null ? template : new Disk();
        if (!StringHelper.isNullOrEmpty(entity.getinternal_drive_mapping()) ) {
            model.setName("Disk " + entity.getinternal_drive_mapping());
        }
        if (!Guid.Empty.equals(entity.getvm_guid())) {
            model.setVm(new VM());
            model.getVm().setId(entity.getvm_guid().toString());
        }
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
        model.setShareable(entity.isShareable());
        model.setAllowSnapshot(entity.isAllowSnapshot());
        if (entity.getDiskStorageType() == DiskStorageType.IMAGE) {
            mapDiskImageToDiskFields((DiskImage) entity, model);
        } else {
            model.setLunStorage(StorageLogicalUnitMapper.map(((LunDisk) entity).getLun(), new Storage()));
        }
        return model;
    }

    private static void mapDiskImageToDiskFields(DiskImage entity, Disk model) {
        if (entity.getImageId() != null) {
            model.setImageId(entity.getImageId().toString());
        }
        model.setSize(entity.getsize());
        if (entity.getvolume_format() != null) {
            model.setFormat(map(entity.getvolume_format(), null));
        }
        if (entity.getimageStatus() != null) {
            DiskStatus status = map(entity.getimageStatus());
            model.setStatus(StatusUtils.create(status == null ? null : status.value()));
        }
        model.setSparse(VolumeType.Sparse == entity.getvolume_type());
        if (entity.getstorage_ids() != null && entity.getstorage_ids().size() > 0) {
            StorageDomain storageDomain = new StorageDomain();
            storageDomain.setId(entity.getstorage_ids().get(0).toString());
            if (!model.isSetStorageDomains()) {
                model.setStorageDomains(new StorageDomains());
            }
            model.getStorageDomains().getStorageDomains().add(storageDomain);
        }
        if (entity.getQuotaId()!=null) {
            Quota quota = new Quota();
            quota.setId(entity.getQuotaId().toString());
            model.setQuota(quota);
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

    @Mapping(from = DiskInterface.class, to = org.ovirt.engine.core.common.businessentities.DiskInterface.class)
    public static org.ovirt.engine.core.common.businessentities.DiskInterface map(
            DiskInterface diskInterface,
            org.ovirt.engine.core.common.businessentities.DiskInterface template) {
        switch (diskInterface) {
        case IDE:
            return org.ovirt.engine.core.common.businessentities.DiskInterface.IDE;
        case VIRTIO:
            return org.ovirt.engine.core.common.businessentities.DiskInterface.VirtIO;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.DiskInterface.class, to = String.class)
    public static String map(org.ovirt.engine.core.common.businessentities.DiskInterface diskInterface, String template) {
        switch (diskInterface) {
        case IDE:
            return DiskInterface.IDE.value();
        case VirtIO:
            return DiskInterface.VIRTIO.value();
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
