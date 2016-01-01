package org.ovirt.engine.core.common.vdscommands;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.compat.Guid;


public class GetDiskImageAlignmentVDSCommandParameters extends GetDiskAlignmentVDSCommandParameters {
    private Guid poolId;
    private Guid domainId;
    private Guid imageGroupId;
    private Guid imageId;

    public GetDiskImageAlignmentVDSCommandParameters(Guid vdsId, Guid vmId) {
        super(vdsId, vmId);
    }

    public GetDiskImageAlignmentVDSCommandParameters() {
    }

    public void setPoolId(Guid poolId) {
        this.poolId = poolId;
    }

    public Guid getPoolId() {
        return poolId;
    }

    public void setDomainId(Guid domainId) {
        this.domainId = domainId;
    }

    public Guid getDomainId() {
        return domainId;
    }

    public void setImageGroupId(Guid imageGroupId) {
        this.imageGroupId = imageGroupId;
    }

    public Guid getImageGroupId() {
        return imageGroupId;
    }

    public void setImageId(Guid imageId) {
        this.imageId = imageId;
    }

    public Guid getImageId() {
        return imageId;
    }

    @Override
    public Map<String, String> getDriveSpecs() {
        Map<String, String> drive = new HashMap<>();
        drive.put("device", "disk");
        drive.put("domainID", getDomainId().toString());
        drive.put("poolID", getPoolId().toString());
        drive.put("volumeID", Guid.Empty.toString());
        drive.put("imageID", getImageGroupId().toString());
        drive.put("volumeID", getImageId().toString());
        return drive;
    }
}
