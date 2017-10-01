package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportVmTemplateFromConfParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportTemplateData;

public class RegisterTemplateModel extends RegisterEntityModel<VmTemplate, ImportTemplateData> {

    protected void onSave() {
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();
        for (ImportTemplateData entityData : getEntities().getItems()) {
            VmTemplate vmTemplate = entityData.getEntity();
            Cluster cluster = entityData.getCluster().getSelectedItem();

            ImportVmTemplateFromConfParameters params = new ImportVmTemplateFromConfParameters();
            params.setContainerId(vmTemplate.getId());
            params.setStorageDomainId(getStorageDomainId());
            params.setImagesExistOnTargetStorageDomain(true);
            params.setClusterId(cluster != null ? cluster.getId() : null);

            if (isQuotaEnabled()) {
                Quota quota = entityData.getClusterQuota().getSelectedItem();
                params.setQuotaId(quota != null ? quota.getId() : null);
                params.setDiskTemplateMap(vmTemplate.getDiskTemplateMap());
                updateDiskQuotas(new ArrayList<Disk>(params.getDiskTemplateMap().values()));
            }

            parameters.add(params);
        }

        startProgress();
        Frontend.getInstance().runMultipleAction(ActionType.ImportVmTemplateFromConfiguration, parameters,
                result -> {
                    stopProgress();
                    cancel();
                }, this);
    }

}
