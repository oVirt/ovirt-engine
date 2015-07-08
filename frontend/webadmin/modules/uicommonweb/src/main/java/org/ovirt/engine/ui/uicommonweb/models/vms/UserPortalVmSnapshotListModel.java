package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.action.AddVmFromSnapshotParameters;

public class UserPortalVmSnapshotListModel extends VmSnapshotListModel {

    @Override
    protected void setupAddVmFromSnapshotParameters(AddVmFromSnapshotParameters parameters) {
        super.setupAddVmFromSnapshotParameters(parameters);
        parameters.setMakeCreatorExplicitOwner(true);
    }

    @Override
    protected NewTemplateVmModelBehavior createNewTemplateBehavior() {
        return new UserPortalNewTemplateVmModelBehavior();
    }
}
