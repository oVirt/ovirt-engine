package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.compat.Guid;

public class CopyImageVDSCommandParameters
        extends AllStorageAndImageIdVDSCommandParametersBase implements PostZero {
    public CopyImageVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid vmId, Guid imageGroupId,
            Guid srcImageId, Guid dstImageGroupId, Guid dstVolUUID, String description, Guid dstStorageDomainId,
            CopyVolumeType copyVolumeType, VolumeFormat volumeFormat, VolumeType preallocate, boolean postZero,
            boolean force) {
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
    }

    private Guid privatedstImageGroupId;

    public Guid getdstImageGroupId() {
        return privatedstImageGroupId;
    }

    public void setdstImageGroupId(Guid value) {
        privatedstImageGroupId = value;
    }

    private Guid privateVmId;

    public Guid getVmId() {
        return privateVmId;
    }

    public void setVmId(Guid value) {
        privateVmId = value;
    }

    private Guid privateDstImageId;

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

    private Guid privateDstStorageDomainId;

    public Guid getDstStorageDomainId() {
        return privateDstStorageDomainId;
    }

    public void setDstStorageDomainId(Guid value) {
        privateDstStorageDomainId = value;
    }

    private CopyVolumeType privateCopyVolumeType;

    public CopyVolumeType getCopyVolumeType() {
        return privateCopyVolumeType;
    }

    public void setCopyVolumeType(CopyVolumeType value) {
        privateCopyVolumeType = value;
    }

    private VolumeFormat privateVolumeFormat;

    public VolumeFormat getVolumeFormat() {
        return privateVolumeFormat;
    }

    public void setVolumeFormat(VolumeFormat value) {
        privateVolumeFormat = value;
    }

    private VolumeType privatePreallocate;

    public VolumeType getPreallocate() {
        return privatePreallocate;
    }

    public void setPreallocate(VolumeType value) {
        privatePreallocate = value;
    }

    private boolean privatePostZero;

    @Override
    public boolean getPostZero() {
        return privatePostZero;
    }

    @Override
    public void setPostZero(boolean postZero) {
        privatePostZero = postZero;
    }

    private boolean privateForce;

    public boolean getForce() {
        return privateForce;
    }

    public void setForce(boolean value) {
        privateForce = value;
    }

    public CopyImageVDSCommandParameters() {
        privatedstImageGroupId = Guid.Empty;
        privateDstImageId = Guid.Empty;
        privateDstStorageDomainId = Guid.Empty;
        privateVmId = Guid.Empty;
        privateVolumeFormat = VolumeFormat.UNUSED0;
        privatePreallocate = VolumeType.Unassigned;
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
