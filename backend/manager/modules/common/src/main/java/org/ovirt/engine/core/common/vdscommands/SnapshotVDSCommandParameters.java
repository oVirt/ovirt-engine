package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class SnapshotVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    private List<DiskImage> images;

    public SnapshotVDSCommandParameters(Guid vdsId, Guid vmId, List<DiskImage> images) {
        super(vdsId, vmId);
        this.images = images;
    }

    public List<DiskImage> getImages() {
        return images;
    }
}
