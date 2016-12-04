package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.storage.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class CopyImageVDSCommandParameters
        extends AllStorageAndImageIdVDSCommandParametersBase implements PostDeleteAction {
    public CopyImageVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid vmId, Guid imageGroupId,
            Guid srcImageId, Guid dstImageGroupId, Guid dstVolUUID, String description, Guid dstStorageDomainId,
            CopyVolumeType copyVolumeType, VolumeFormat volumeFormat, VolumeType preallocate, boolean postZero,
            boolean discard, boolean force) {
        super(storagePoolId, storageDomainId, imageGroupId, srcImageId);
        this.setdstImageGroupId(dstImageGroupId);
        setVmId(vmId);
        setDstImageId(dstVolUUID);
        setImageDescription(description);
        setPostZero(postZero);
        setDiscard(discard);
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

    private boolean discard;

    public boolean isDiscard() {
        return discard;
    }

    public void setDiscard(boolean discard) {
        this.discard = discard;
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
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("dstImageGroupId", getdstImageGroupId())
                .append("vmId", getVmId())
                .append("dstImageId", getDstImageId())
                .append("imageDescription", getImageDescription())
                .append("dstStorageDomainId", getDstStorageDomainId())
                .append("copyVolumeType", getCopyVolumeType())
                .append("volumeFormat", getVolumeFormat())
                .append("preallocate", getPreallocate())
                .append("postZero", getPostZero())
                .append("discard", isDiscard())
                .append("force", getForce());
    }
}
