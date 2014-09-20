package org.ovirt.engine.ui.uicommonweb.models.vms;

public class AttachDiskGuideModel extends AttachDiskModel {

    private VmGuideModel sourceModel;

    public AttachDiskGuideModel(VmGuideModel sourceModel) {
        this.sourceModel = sourceModel;
    }

    @Override
    protected void postSave() {
        super.postSave();
        sourceModel.updateOptions(true);
    }
}
