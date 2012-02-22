package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskFormat;
import org.ovirt.engine.api.model.DiskInterface;
import org.ovirt.engine.api.model.DiskStatus;
import org.ovirt.engine.api.model.DiskType;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;

public class DiskMapper {

    @Mapping(from = Disk.class, to = DiskImage.class)
    public static DiskImage map(Disk disk, DiskImage template) {
        DiskImage diskImage = template != null ? template : new DiskImage();
        if (disk.isSetVm() && disk.getVm().isSetId()) {
            diskImage.setvm_guid(new Guid(disk.getVm().getId()));
        }
        if (disk.isSetId()) {
            diskImage.setId(new Guid(disk.getId()));
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
        if (disk.isSetInterface()) {
            DiskInterface diskInterface = DiskInterface.fromValue(disk.getInterface());
            if (diskInterface != null) {
                diskImage.setdisk_interface(map(diskInterface, null));
            }
        }
        if (disk.isSetType()) {
            DiskType diskType = DiskType.fromValue(disk.getType());
            if (diskType != null) {
                diskImage.setdisk_type(map(diskType, null));
            }
        }
        if (disk.isSetStatus()) {
            diskImage.setimageStatus(map(DiskStatus.fromValue(disk.getStatus().getState())));
        }
        if (disk.isSetSparse()) {
            diskImage.setvolume_type(disk.isSparse() ? VolumeType.Sparse : VolumeType.Preallocated);
        }
        if (disk.isSetBootable()) {
            diskImage.setboot(disk.isBootable());
        }
        if (disk.isSetPropagateErrors()) {
            diskImage.setpropagate_errors(disk.isPropagateErrors() ? PropagateErrors.On
                    : PropagateErrors.Off);
        }
        if (disk.isSetWipeAfterDelete()) {
            diskImage.setwipe_after_delete(disk.isWipeAfterDelete());
        }
        if (disk.isSetPlugged()) {
            diskImage.setPlugged(disk.isPlugged());
        }
        return diskImage;
    }

    @Mapping(from = DiskImage.class, to = Disk.class)
    public static Disk map(DiskImage entity, Disk template) {
        Disk model = template != null ? template : new Disk();
        if (!StringHelper.isNullOrEmpty(entity.getinternal_drive_mapping()) ) {
            model.setName("Disk " + entity.getinternal_drive_mapping());
        }
        if (!Guid.Empty.equals(entity.getvm_guid())) {
            model.setVm(new VM());
            model.getVm().setId(entity.getvm_guid().toString());
        }
        model.setId(entity.getId().toString());
        model.setSize(entity.getsize());
        if (entity.getvolume_format() != null) {
            model.setFormat(map(entity.getvolume_format(), null));
        }
        if (entity.getdisk_interface() != null) {
            model.setInterface(map(entity.getdisk_interface(), null));
        }
        if (entity.getdisk_type() != null) {
            model.setType(map(entity.getdisk_type(), null));
        }
        if (entity.getimageStatus() != null) {
            DiskStatus status = map(entity.getimageStatus());
            model.setStatus(StatusUtils.create(status==null ? null : status.value()));
        }
        model.setSparse(VolumeType.Sparse == entity.getvolume_type());
        model.setBootable(entity.getboot());
        model.setPropagateErrors(PropagateErrors.On == entity.getpropagate_errors());
        model.setWipeAfterDelete(entity.getwipe_after_delete());
        if(entity.getstorage_ids()!=null && entity.getstorage_ids().size() > 0){
            StorageDomain storageDomain = new StorageDomain();
            storageDomain.setId(entity.getstorage_ids().get(0).toString());
            if (!model.isSetStorageDomains()) {
                model.setStorageDomains(new StorageDomains());
            }
            model.getStorageDomains().getStorageDomains().add(storageDomain);
        }
        model.setPlugged(entity.getPlugged());
        return model;
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

    @Mapping(from = DiskType.class, to = org.ovirt.engine.core.common.businessentities.DiskType.class)
    public static org.ovirt.engine.core.common.businessentities.DiskType map(DiskType diskType,
            org.ovirt.engine.core.common.businessentities.DiskType template) {
        switch (diskType) {
        case DATA:
            return org.ovirt.engine.core.common.businessentities.DiskType.Data;
        case SYSTEM:
            return org.ovirt.engine.core.common.businessentities.DiskType.System;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.DiskType.class, to = String.class)
    public static String map(org.ovirt.engine.core.common.businessentities.DiskType diskType, String template) {
        switch (diskType) {
        case Data:
            return DiskType.DATA.value();
        case System:
            return DiskType.SYSTEM.value();
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
