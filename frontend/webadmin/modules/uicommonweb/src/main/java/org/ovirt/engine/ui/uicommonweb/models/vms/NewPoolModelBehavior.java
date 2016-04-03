package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.InstanceTypeManager;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.NewPoolInstanceTypeManager;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NewPoolNameLengthValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;


public class NewPoolModelBehavior extends PoolModelBehaviorBase {

    private InstanceTypeManager instanceTypeManager;

    @Override
    public void initialize(SystemTreeItemModel systemTreeSelectedItem) {
        super.initialize(systemTreeSelectedItem);

        getModel().getVmType().setIsChangeable(true);
        getModel().getPoolStateful().setIsChangeable(true);

        templateValidate();

        instanceTypeManager = new NewPoolInstanceTypeManager(getModel());

        getModel().getVmInitModel().init(null);
    }

    @Override
    protected void commonInitialize() {
        super.commonInitialize();

        getModel().getPoolStateful().getEntityChangedEvent().addListener(new UpdateTemplateWithVersionListener() {
            @Override
            protected boolean isAddLatestVersion() {
                return !getModel().getPoolStateful().getEntity();
            }
        });
    }

    @Override
    protected void postDataCentersLoaded(List<StoragePool> dataCenters) {
        if (!dataCenters.isEmpty()) {
            super.postDataCentersLoaded(dataCenters);
        } else {
            getModel().disableEditing(ConstantsManager.getInstance().getConstants().notAvailableWithNoUpDC());
        }
    }

    @Override
    public void postDataCenterWithClusterSelectedItemChanged() {
        super.postDataCenterWithClusterSelectedItemChanged();

        final DataCenterWithCluster dataCenterWithCluster = getModel().getDataCenterWithClustersList().getSelectedItem();
        StoragePool dataCenter = getModel().getSelectedDataCenter();
        if (dataCenter == null) {
            return;
        }

        AsyncDataProvider.getInstance().getTemplateListByDataCenter(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target1, Object returnValue1) {

                List<VmTemplate> templatesByDataCenter = (List<VmTemplate>) returnValue1;
                List<VmTemplate> properArchitectureTemplates =
                        AsyncDataProvider.getInstance().filterTemplatesByArchitecture(templatesByDataCenter,
                                dataCenterWithCluster.getCluster().getArchitecture());
                List<VmTemplate> templatesWithoutBlank = new ArrayList<>();
                for (VmTemplate template : properArchitectureTemplates) {
                    final boolean isBlankOrVersionOfBlank = template.getId().equals(Guid.Empty)
                            || template.getBaseTemplateId().equals(Guid.Empty);
                    if (!isBlankOrVersionOfBlank) {
                        templatesWithoutBlank.add(template);
                    }
                }
                initTemplateWithVersion(templatesWithoutBlank, null, false);
            }
        }), dataCenter.getId());

        instanceTypeManager.updateAll();
    }

    @Override
    public void templateWithVersion_SelectedItemChanged() {
        super.templateWithVersion_SelectedItemChanged();
        VmTemplate template = getModel().getTemplateWithVersion().getSelectedItem() != null
                ? getModel().getTemplateWithVersion().getSelectedItem().getTemplateVersion()
                : null;

        if (template == null) {
            return;
        }

        setupWindowModelFrom(template);
        doChangeDefaultHost(template.getDedicatedVmForVdsList());
        updateRngDevice(template.getId());
        getModel().getCustomPropertySheet().deserialize(template.getCustomProperties());
    }

    @Override
    public boolean validate() {
        boolean parentValidation = super.validate();
        if (getModel().getName().getIsValid()) {
            getModel().getName().validateEntity(new IValidation[] { new NewPoolNameLengthValidation(
                    getModel().getName().getEntity(),
                    getModel().getNumOfDesktops().getEntity(),
                    getModel().getOSType().getSelectedItem()
                    ) });

            final boolean isNameValid = getModel().getName().getIsValid();
            getModel().setValidTab(TabName.GENERAL_TAB, getModel().isValidTab(TabName.GENERAL_TAB) && isNameValid);
            return getModel().getName().getIsValid() && parentValidation;
        }

        return parentValidation;
    }

    private void templateValidate() {
        AsyncDataProvider.getInstance().countAllTemplates(new AsyncQuery(getModel(), new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                int count = (Integer) returnValue;
                if (count <= 1) {
                    getModel().disableEditing(ConstantsManager.getInstance().getConstants().notAvailableWithNoTemplates());
                }
            }
        }));
    }

    @Override
    protected List<Cluster> filterClusters(List<Cluster> clusters) {
        return AsyncDataProvider.getInstance().filterClustersWithoutArchitecture(clusters);
    }

    @Override
    public InstanceTypeManager getInstanceTypeManager() {
        return instanceTypeManager;
    }

    @Override
    public void enableSinglePCI(boolean enabled) {
        super.enableSinglePCI(enabled);
        getModel().getIsSingleQxlEnabled().setEntity(enabled);
    }
}
