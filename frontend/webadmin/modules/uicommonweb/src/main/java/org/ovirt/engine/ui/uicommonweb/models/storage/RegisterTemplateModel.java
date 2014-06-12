package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.ImportVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class RegisterTemplateModel extends RegisterEntityModel {

    protected void onSave() {
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();
        for (ImportEntityData entityData : getEntities().getItems()) {
            BusinessEntity<Guid> entity = (BusinessEntity<Guid>) entityData.getEntity();
            VDSGroup vdsGroup = entityData.getCluster().getSelectedItem();

            ImportVmTemplateParameters params = new ImportVmTemplateParameters();
            params.setContainerId(entity.getId());
            params.setStorageDomainId(getStorageDomainId());
            params.setVdsGroupId(vdsGroup != null ? vdsGroup.getId() : null);
            params.setImagesExistOnTargetStorageDomain(true);

            parameters.add(params);
        }

        startProgress(null);
        Frontend.getInstance().runMultipleAction(VdcActionType.ImportVmTemplateFromConfiguration, parameters, new IFrontendMultipleActionAsyncCallback() {
            @Override
            public void executed(FrontendMultipleActionAsyncResult result) {
                stopProgress();
                cancel();
            }
        }, this);
    }

}
