package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.common.businessentities.storage.DiskImageBase;
import org.ovirt.engine.core.compat.Guid;

public class CreateCloneOfTemplateParameters extends CreateSnapshotFromTemplateParameters implements
        Serializable {

    private static final long serialVersionUID = 3513412319261236990L;
    private DiskImageBase privateDiskImageBase;

    public DiskImageBase getDiskImageBase() {
        return privateDiskImageBase;
    }

    private void setDiskImageBase(DiskImageBase value) {
        privateDiskImageBase = value;
    }

    public CreateCloneOfTemplateParameters(Guid imageId, Guid vmId, DiskImageBase diskImageBase) {
        super(imageId, vmId);
        setDiskImageBase(diskImageBase);
    }

    public CreateCloneOfTemplateParameters() {
    }
}
