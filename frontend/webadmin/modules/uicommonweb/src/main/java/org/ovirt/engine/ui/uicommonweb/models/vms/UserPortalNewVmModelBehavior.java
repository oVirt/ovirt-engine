package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.InstanceTypeManager;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.NewVmInstanceTypeManager;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class UserPortalNewVmModelBehavior extends NewVmModelBehavior implements IFrontendMultipleQueryAsyncCallback
{
    private InstanceTypeManager instanceTypeManager;

    private ActionGroup actionGroup;

    @Override
    public void initialize(SystemTreeItemModel systemTreeSelectedItem)
    {
        actionGroup = getModel().isCreateInstanceOnly() ? ActionGroup.CREATE_INSTANCE : ActionGroup.CREATE_VM;

        commonInitialize();
        // The custom properties tab should be hidden on the User Portal
        getModel().setIsCustomPropertiesTabAvailable(false);

        getModel().getIsSoundcardEnabled().setIsChangable(true);
        getModel().getVmType().setIsChangable(true);
        getModel().getVmId().setIsAvailable(true);

        // Get datacenters with permitted create action
        AsyncDataProvider.getInstance().getDataCentersWithPermittedActionOnClusters(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        final List<StoragePool> dataCenters = new ArrayList<StoragePool>();
                        for (StoragePool a : (ArrayList<StoragePool>) returnValue) {
                            if (a.getStatus() == StoragePoolStatus.Up) {
                                dataCenters.add(a);
                            }
                        }

                        if (!dataCenters.isEmpty()) {
                            AsyncDataProvider.getInstance().getClustersWithPermittedAction(
                                    new AsyncQuery(getModel(), new INewAsyncCallback() {

                                        @Override
                                        public void onSuccess(Object target, Object returnValue) {
                                            UnitVmModel model = (UnitVmModel) target;
                                            List<VDSGroup> clusters = (List<VDSGroup>) returnValue;
                                            // filter clusters without cpu name
                                            clusters = AsyncDataProvider.getInstance().filterClustersWithoutArchitecture(clusters);
                                            model.setDataCentersAndClusters(model,
                                                    dataCenters,
                                                    clusters, null);
                                        }
                                    }),
                                    actionGroup, true, false);
                        } else {
                            getModel().disableEditing(ConstantsManager.getInstance().getConstants().notAvailableWithNoUpDC());
                        }
                    }
                }), actionGroup, true, false);

        instanceTypeManager = new NewVmInstanceTypeManager(getModel());
    }

    @Override
    public void dataCenterWithClusterSelectedItemChanged()
    {
        super.dataCenterWithClusterSelectedItemChanged();

        ArrayList<VdcQueryType> queryTypeList = new ArrayList<VdcQueryType>();
        queryTypeList.add(VdcQueryType.GetVmTemplatesWithPermittedAction);

        GetEntitiesWithPermittedActionParameters getEntitiesWithPermittedActionParameters =
                new GetEntitiesWithPermittedActionParameters();
        getEntitiesWithPermittedActionParameters.setActionGroup(actionGroup);

        ArrayList<VdcQueryParametersBase> parametersList =
                new ArrayList<VdcQueryParametersBase>(Arrays.asList(new VdcQueryParametersBase[] {
                        getEntitiesWithPermittedActionParameters }));

        // Get clusters and templates
        Frontend.getInstance().runMultipleQueries(queryTypeList, parametersList, this, getModel());

    }

    @Override
    public void executed(FrontendMultipleQueryAsyncResult result)
    {
        List<VdcQueryReturnValue> returnValueList = result.getReturnValues();
        List<VmTemplate> templates =
                (ArrayList<VmTemplate>) returnValueList.get(0).getReturnValue();
        initTemplateWithVersion(templates);
        initCdImage();
    }

    @Override
    protected void initTemplateWithVersion(List<VmTemplate> templates) {
        DataCenterWithCluster dataCenterWithCluster = this.getModel().getDataCenterWithClustersList().getSelectedItem();
        StoragePool selectedDataCenter = dataCenterWithCluster.getDataCenter();
        Guid selectedDataCenterId = selectedDataCenter.getId();
        if (selectedDataCenterId == null) {
            return;
        }
        List<VmTemplate> properArchitectureTemplates = AsyncDataProvider.getInstance()
                .filterTemplatesByArchitecture(templates, dataCenterWithCluster.getCluster().getArchitecture());
        List<VmTemplate> properStateTemplates = new ArrayList<>();
        for (VmTemplate template : properArchitectureTemplates) {
            if (template.getStatus().equals(VmTemplateStatus.OK)) {
                properStateTemplates.add(template);
            }
        }
        super.initTemplateWithVersion(properStateTemplates);
        updateIsDisksAvailable();
    }

    /**
     * Disabled to change this in userportal
     */
    @Override
    protected void doChangeDefautlHost(Guid hostGuid) {
    }

    @Override
    protected void getHostListByCluster(VDSGroup cluster, AsyncQuery query) {
        Frontend.getInstance().runQuery(
                VdcQueryType.GetHostsByClusterId,
                new IdQueryParameters(cluster.getId()),
                query
                );
    }

    @Override
    public InstanceTypeManager getInstanceTypeManager() {
        return instanceTypeManager;
    }

    @Override
    protected void updateNumaEnabled() {
    }
}
