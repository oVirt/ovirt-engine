package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class RemoveAllVmImagesParameters extends VmOperationParameterBase implements java.io.Serializable {
    private static final long serialVersionUID = 7211692656127711421L;
    public java.util.List<DiskImage> Images;

    public RemoveAllVmImagesParameters(Guid vmId, java.util.List<DiskImage> images) {
        super(vmId);
        this.Images = images;
        setForceDelete(false);
    }

    private boolean privateForceDelete;

    public boolean getForceDelete() {
        return privateForceDelete;
    }

    public void setForceDelete(boolean value) {
        privateForceDelete = value;
    }

    public RemoveAllVmImagesParameters() {
    }
}
