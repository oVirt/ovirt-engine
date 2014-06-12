package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

import java.util.ArrayList;

public class RegisterVmModel extends RegisterEntityModel {

    protected void onSave() {
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();
        for (ImportEntityData entityData : getEntities().getItems()) {
            BusinessEntity<Guid> entity = (BusinessEntity<Guid>) entityData.getEntity();
            VDSGroup vdsGroup = entityData.getCluster().getSelectedItem();

            ImportVmParameters params = new ImportVmParameters();
            params.setContainerId(entity.getId());
            params.setStorageDomainId(getStorageDomainId());
            params.setVdsGroupId(vdsGroup != null ? vdsGroup.getId() : null);
            params.setImagesExistOnTargetStorageDomain(true);

            parameters.add(params);
        }

        startProgress(null);
        Frontend.getInstance().runMultipleAction(VdcActionType.ImportVmFromConfiguration, parameters, new IFrontendMultipleActionAsyncCallback() {
            @Override
            public void executed(FrontendMultipleActionAsyncResult result) {
                stopProgress();
                cancel();
            }
        }, this);
    }

}
