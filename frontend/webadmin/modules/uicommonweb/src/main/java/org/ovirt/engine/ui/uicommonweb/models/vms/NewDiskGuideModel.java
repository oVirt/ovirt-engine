package org.ovirt.engine.ui.uicommonweb.models.vms;

public class NewDiskGuideModel extends NewDiskModel {

    private VmGuideModel sourceModel;

    public NewDiskGuideModel(VmGuideModel sourceModel) {
        this.sourceModel = sourceModel;
    }

    @Override
    protected void postSave() {
        super.postSave();
        sourceModel.updateOptions(true);
    }
}
