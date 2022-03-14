package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.LocationInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskBackup;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;

public class ConvertDiskCommandParameters extends ImagesContainterParametersBase {
    private static final long serialVersionUID = -3948444265099244259L;

    private LocationInfo locationInfo;
    private VolumeFormat volumeFormat;
    private VolumeType preallocation;
    private Guid newVolGuid;
    private DiskBackup backup;

    private ConvertDiskPhase convertDiskPhase = ConvertDiskPhase.CREATE_TARGET_VOLUME;

    public ConvertDiskCommandParameters() {
        super();
    }

    public LocationInfo getLocationInfo() {
        return locationInfo;
    }

    public void setLocationInfo(LocationInfo locationInfo) {
        this.locationInfo = locationInfo;
    }

    public VolumeFormat getVolumeFormat() {
        return volumeFormat;
    }

    public void setVolumeFormat(VolumeFormat volumeFormat) {
        this.volumeFormat = volumeFormat;
    }

    public VolumeType getPreallocation() {
        return preallocation;
    }

    public void setPreallocation(VolumeType preallocation) {
        this.preallocation = preallocation;
    }

    public Guid getNewVolGuid() {
        return newVolGuid;
    }

    public void setNewVolGuid(Guid newVolGuid) {
        this.newVolGuid = newVolGuid;
    }

    public ConvertDiskPhase getConvertDiskPhase() {
        return convertDiskPhase;
    }

    public void setConvertDiskPhase(ConvertDiskPhase convertDiskPhase) {
        this.convertDiskPhase = convertDiskPhase;
    }

    public DiskBackup getBackup() {
        return backup;
    }

    public void setBackup(DiskBackup backup) {
        this.backup = backup;
    }

    public enum ConvertDiskPhase {
        CREATE_TARGET_VOLUME,
        CONVERT_VOLUME,
        SWITCH_IMAGE,
        REMOVE_SOURCE_VOLUME,
        COMPLETE,
    }
}
