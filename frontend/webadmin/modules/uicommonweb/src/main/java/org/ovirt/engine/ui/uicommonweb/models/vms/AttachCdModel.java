package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class AttachCdModel extends Model {

    private ListModel<RepoImage> privateIsoImage;

    public ListModel<RepoImage> getIsoImage() {
        return privateIsoImage;
    }

    private void setIsoImage(ListModel<RepoImage> value) {
        privateIsoImage = value;
    }

    public AttachCdModel() {
        setIsoImage(new ListModel<>());
    }
}
