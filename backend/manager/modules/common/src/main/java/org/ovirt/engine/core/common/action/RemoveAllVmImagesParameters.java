package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class RemoveAllVmImagesParameters extends VmOperationParameterBase implements Serializable {
    private static final long serialVersionUID = 7211692656127711421L;
    private List<DiskImage> images;

    public RemoveAllVmImagesParameters(Guid vmId, List<DiskImage> images) {
        super(vmId);
        this.setImages(images);
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

    public List<DiskImage> getImages() {
        return images;
    }

    public void setImages(List<DiskImage> images) {
        this.images = images;
    }

}
