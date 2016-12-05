package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.QcowCompat;
import org.ovirt.engine.core.compat.Guid;

public class AmendImageGroupVolumesCommandParameters extends ImagesActionsParametersBase {
    private QcowCompat qcowCompat;
    private List<Guid> imageIds;

    public AmendImageGroupVolumesCommandParameters() {
    }

    public AmendImageGroupVolumesCommandParameters(Guid imageGroupId,
            QcowCompat qcowCompat) {
        setImageGroupID(imageGroupId);
        this.qcowCompat = qcowCompat;
    }

    public List<Guid> getImageIds() {
        return imageIds;
    }

    public void setImageIds(List<Guid> imageIds) {
        this.imageIds = imageIds;
    }

    public QcowCompat getQcowCompat() {
        return qcowCompat;
    }

    public void setQcowCompat(QcowCompat qcowCompat) {
        this.qcowCompat = qcowCompat;
    }
}
