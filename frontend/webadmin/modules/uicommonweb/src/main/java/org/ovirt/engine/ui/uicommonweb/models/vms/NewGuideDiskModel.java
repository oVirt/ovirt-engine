package org.ovirt.engine.ui.uicommonweb.models.vms;

public class NewGuideDiskModel extends NewDiskModel {

    private VmGuideModel sourceModel;

    public NewGuideDiskModel(VmGuideModel sourceModel) {
        this.sourceModel = sourceModel;
    }

    @Override
    protected void postSave() {
        super.postSave();
        sourceModel.updateOptions(true);
    }
}
