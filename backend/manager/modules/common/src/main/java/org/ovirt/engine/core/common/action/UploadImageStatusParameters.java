package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.storage.ImageTransferUpdates;
import org.ovirt.engine.core.compat.Guid;

public class UploadImageStatusParameters extends VdcActionParametersBase {
    private static final long serialVersionUID = 8404863745376386682L;
    private Guid uploadImageCommandId;
    private Guid diskId;
    private ImageTransferUpdates updates;

    public UploadImageStatusParameters(Guid uploadImageCommandId, ImageTransferUpdates updates) {
        this.uploadImageCommandId = uploadImageCommandId;
        this.updates = updates;
    }

    public UploadImageStatusParameters(Guid uploadImageCommandId) {
        this.uploadImageCommandId = uploadImageCommandId;
    }

    public UploadImageStatusParameters() {
    }

    public Guid getUploadImageCommandId() {
        return uploadImageCommandId;
    }

    public void setUploadImageCommandId(Guid uploadImageCommandId) {
        this.uploadImageCommandId = uploadImageCommandId;
    }

    public Guid getDiskId() {
        return diskId;
    }

    public void setDiskId(Guid diskId) {
        this.diskId = diskId;
    }

    public ImageTransferUpdates getUpdates() {
        return updates;
    }

    public void setUpdates(ImageTransferUpdates updates) {
        this.updates = updates;
    }
}
