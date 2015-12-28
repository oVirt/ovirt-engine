package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateWithVersion;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewVmFromTemplateModelBehavior extends NewVmModelBehavior {

    private VmTemplate selectedTemplate;

    public NewVmFromTemplateModelBehavior(VmTemplate template) {
        this.selectedTemplate = template;
    }

    @Override
    protected void postInitTemplate(List<VmTemplate> templates) {
        DataCenterWithCluster selectedDCWithCluster = getModel().getDataCenterWithClustersList().getSelectedItem();
        Guid clusterId =
                selectedDCWithCluster != null ? selectedDCWithCluster.getCluster().getId()
                        : selectedTemplate.getClusterId();

        VmTemplate baseTemplate = null;
        if (selectedTemplate.isBaseTemplate()) {
            baseTemplate = selectedTemplate;
        }
        Guid baseTemplateId = selectedTemplate.getBaseTemplateId();

        List<VmTemplate> relatedTemplates = new ArrayList<>();
        for (VmTemplate template : templates) {
            if (template.getBaseTemplateId().equals(baseTemplateId)) {
                if (template.getClusterId() == null || template.getClusterId().equals(clusterId)) {
                    relatedTemplates.add(template);
                }

                if (baseTemplate == null) {
                    if (template.getId().equals(baseTemplateId)) {
                        baseTemplate = template;
                    }
                }
            }
        }
        if (!relatedTemplates.contains(baseTemplate)) {
            relatedTemplates.add(baseTemplate);
        }

        initTemplateWithVersion(relatedTemplates, null, false);

        if (selectedDCWithCluster != null && selectedDCWithCluster.getCluster() != null) {
            if (selectedTemplate.getClusterId() == null || selectedTemplate.getClusterId().equals(selectedDCWithCluster.getCluster().getId())) {
                TemplateWithVersion templateCouple = new TemplateWithVersion(baseTemplate, selectedTemplate);
                getModel().getTemplateWithVersion().setSelectedItem(templateCouple);
            }
        }

        updateIsDisksAvailable();
    }

    protected void loadDataCenters() {
        if (!selectedTemplate.getId().equals(Guid.Empty)) {
            AsyncDataProvider.getInstance().getDataCenterById(new AsyncQuery(getModel(),
                            new INewAsyncCallback() {
                                @Override
                                public void onSuccess(Object target, Object returnValue) {

                                    if (returnValue != null) {
                                        StoragePool dataCenter = (StoragePool) returnValue;
                                        List<StoragePool> dataCenters =
                                                new ArrayList<>(Arrays.asList(new StoragePool[] { dataCenter }));
                                        initClusters(dataCenters);
                                    } else {
                                        getModel().disableEditing(ConstantsManager.getInstance().getConstants().notAvailableWithNoUpDC());
                                    }

                                }
                            }),
                    selectedTemplate.getStoragePoolId());
        } else {
            // blank template lives on all data centers
            super.loadDataCenters();
        }

    }

    protected void initClusters(final List<StoragePool> dataCenters) {
        AsyncDataProvider.getInstance().getClusterListByService(
                new AsyncQuery(getModel(), new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        UnitVmModel model = (UnitVmModel) target;

                        List<Cluster> clusters = (List<Cluster>) returnValue;

                        List<Cluster> filteredClusters =
                                AsyncDataProvider.getInstance().filterByArchitecture(clusters,
                                        selectedTemplate.getClusterArch());

                        model.setDataCentersAndClusters(model,
                                dataCenters,
                                filteredClusters, selectedTemplate.getClusterId());
                        initCdImage();
                    }
                }),
                true,
                false);
    }

}
