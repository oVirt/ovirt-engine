package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

import java.util.ArrayList;

public class RegisterVmModel extends RegisterEntityModel<VM> {

    protected void onSave() {
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();
        for (ImportEntityData<VM> entityData : getEntities().getItems()) {
            VM vm = entityData.getEntity();
            VDSGroup vdsGroup = entityData.getCluster().getSelectedItem();

            ImportVmParameters params = new ImportVmParameters();
            params.setContainerId(vm.getId());
            params.setStorageDomainId(getStorageDomainId());
            params.setImagesExistOnTargetStorageDomain(true);
            params.setVdsGroupId(vdsGroup != null ? vdsGroup.getId() : null);

            if (isQuotaEnabled()) {
                Quota quota = entityData.getClusterQuota().getSelectedItem();
                params.setQuotaId(quota != null ? quota.getId() : null);
                params.setDiskMap(vm.getDiskMap());
                updateDiskQuotas(new ArrayList<Disk>(params.getDiskMap().values()));
            }

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
