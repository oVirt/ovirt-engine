package org.ovirt.engine.core.common.vdscommands;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.LocationInfo;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class SealDisksVDSCommandParameters extends VdsIdVDSCommandParametersBase {

    private Guid entityId;
    private Guid jobId;
    private Guid storagePoolId;
    private List<LocationInfo> images = new ArrayList<>();

    public SealDisksVDSCommandParameters() {
    }

    public Guid getEntityId() {
        return entityId;
    }

    public void setEntityId(Guid entityId) {
        this.entityId = entityId;
    }

    public Guid getJobId() {
        return jobId;
    }

    public void setJobId(Guid jobId) {
        this.jobId = jobId;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public List<LocationInfo> getImages() {
        return images;
    }

    public void addImage(DiskImage diskImage) {
        images.add(new VdsmImageLocationInfo(diskImage));
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("entityId", entityId)
                .append("jobId", jobId)
                .append("images", images);
    }

}
