package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.compat.Guid;

public class CopyImageVDSCommandParameters extends AllStorageAndImageIdVDSCommandParametersBase {
    public CopyImageVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid vmId, Guid imageGroupId,
            Guid srcImageId, Guid dstImageGroupId, Guid dstVolUUID, String description, Guid dstStorageDomainId,
            CopyVolumeType copyVolumeType, VolumeFormat volumeFormat, VolumeType preallocate, boolean postZero,
            boolean force, String compatibilityVersion) {
        super(storagePoolId, storageDomainId, imageGroupId, srcImageId);
        this.setdstImageGroupId(dstImageGroupId);
        setVmId(vmId);
        setDstImageId(dstVolUUID);
        setImageDescription(description);
        setPostZero(postZero);
        setForce(force);
        setDstStorageDomainId(dstStorageDomainId);
        setCopyVolumeType(copyVolumeType);
        setVolumeFormat(volumeFormat);
        setPreallocate(preallocate);
        setCompatibilityVersion(compatibilityVersion);
    }

    private Guid privatedstImageGroupId = new Guid();

    public Guid getdstImageGroupId() {
        return privatedstImageGroupId;
    }

    public void setdstImageGroupId(Guid value) {
        privatedstImageGroupId = value;
    }

    private Guid privateVmId = new Guid();

    public Guid getVmId() {
        return privateVmId;
    }

    public void setVmId(Guid value) {
        privateVmId = value;
    }

    private Guid privateDstImageId = new Guid();

    public Guid getDstImageId() {
        return privateDstImageId;
    }

    public void setDstImageId(Guid value) {
        privateDstImageId = value;
    }

    private String privateImageDescription;

    public String getImageDescription() {
        return privateImageDescription;
    }

    public void setImageDescription(String value) {
        privateImageDescription = value;
    }

    private Guid privateDstStorageDomainId = new Guid();

    public Guid getDstStorageDomainId() {
        return privateDstStorageDomainId;
    }

    public void setDstStorageDomainId(Guid value) {
        privateDstStorageDomainId = value;
    }

    private CopyVolumeType privateCopyVolumeType = CopyVolumeType.forValue(0);

    public CopyVolumeType getCopyVolumeType() {
        return privateCopyVolumeType;
    }

    public void setCopyVolumeType(CopyVolumeType value) {
        privateCopyVolumeType = value;
    }

    private VolumeFormat privateVolumeFormat = VolumeFormat.forValue(0);

    public VolumeFormat getVolumeFormat() {
        return privateVolumeFormat;
    }

    public void setVolumeFormat(VolumeFormat value) {
        privateVolumeFormat = value;
    }

    private VolumeType privatePreallocate = VolumeType.forValue(0);

    public VolumeType getPreallocate() {
        return privatePreallocate;
    }

    public void setPreallocate(VolumeType value) {
        privatePreallocate = value;
    }

    private boolean privatePostZero;

    public boolean getPostZero() {
        return privatePostZero;
    }

    public void setPostZero(boolean value) {
        privatePostZero = value;
    }

    private boolean privateForce;

    public boolean getForce() {
        return privateForce;
    }

    public void setForce(boolean value) {
        privateForce = value;
    }

    public CopyImageVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, dstImageGroupId = %s, vmId = %s, dstImageId = %s, imageDescription = %s, " +
                "dstStorageDomainId = %s, copyVolumeType = %s, volumeFormat = %s, preallocate = %s, postZero = %s, " +
                "force = %s",
                super.toString(),
                getdstImageGroupId(),
                getVmId(),
                getDstImageId(),
                getImageDescription(),
                getDstStorageDomainId(),
                getCopyVolumeType(),
                getVolumeFormat(),
                getPreallocate(),
                getPostZero(),
                getForce());
    }
}
