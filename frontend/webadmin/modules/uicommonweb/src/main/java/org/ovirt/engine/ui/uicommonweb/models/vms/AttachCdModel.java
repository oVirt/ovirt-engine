package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericNameableComparator;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SortedListModel;

public class AttachCdModel extends Model {

    private SortedListModel<RepoImage> privateIsoImage;

    public SortedListModel<RepoImage> getIsoImage() {
        return privateIsoImage;
    }

    private void setIsoImage(SortedListModel<RepoImage> value) {
        privateIsoImage = value;
    }

    public AttachCdModel() {
        setIsoImage(new SortedListModel<>(new LexoNumericNameableComparator<>()));
    }
}
