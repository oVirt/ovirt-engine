package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.InstanceTypeManager;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.NewVmInstanceTypeManager;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

        // Get datacenters with permitted create action
        AsyncDataProvider.getInstance().getDataCentersWithPermittedActionOnClusters(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        final List<StoragePool> dataCenters = new ArrayList<StoragePool>();
                        for (StoragePool a : (ArrayList<StoragePool>) returnValue)
                        {
                            if (a.getStatus() == StoragePoolStatus.Up)
                            {
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
                                    }, getModel().getHash()),
                                    actionGroup, true, false);
                        } else {
                            getModel().disableEditing(ConstantsManager.getInstance().getConstants().notAvailableWithNoUpDC());
                        }
                    }
                }, getModel().getHash()), actionGroup, true, false);

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
        Frontend.getInstance().runMultipleQueries(queryTypeList, parametersList, this, getModel().getHash());

    }

    @Override
    public void executed(FrontendMultipleQueryAsyncResult result)
    {
        List<VdcQueryReturnValue> returnValueList = result.getReturnValues();
        ArrayList<VmTemplate> templates =
                (ArrayList<VmTemplate>) returnValueList.get(0).getReturnValue();
        initTemplates(templates);
        initCdImage();
    }

    private void initTemplates(List<VmTemplate> templates)
    {
        List<VmTemplate> rootTemplates = filterNotBaseTemplates(templates);

        // Filter templates list (include only templates that belong to the selected datacenter)
        ArrayList<VmTemplate> templatesList = new ArrayList<VmTemplate>();
        VmTemplate blankTemplate = null;
        DataCenterWithCluster dataCenterWithCluster = getModel().getDataCenterWithClustersList().getSelectedItem();
        StoragePool selectedDataCenter = dataCenterWithCluster.getDataCenter();
        Guid selectedDataCenterId = selectedDataCenter.getId();
        if (selectedDataCenterId == null) {
            return;
        }

        for (VmTemplate template : rootTemplates)
        {
            Guid datacenterId =
                    template.getStoragePoolId() == null ? Guid.Empty : template.getStoragePoolId();

            if (template.getId().equals(Guid.Empty))
            {
                blankTemplate = template;
            }
            else if (!selectedDataCenterId.equals(datacenterId))
            {
                continue;
            }
            else if (template.getStatus() == VmTemplateStatus.OK)
            {
                templatesList.add(template);
            }
        }

        // Sort list and position "Blank" template as first
        Collections.sort(templatesList, new NameableComparator());
        if (blankTemplate != null && rootTemplates.contains(blankTemplate))
        {
            templatesList.add(0, blankTemplate);
        }

        List<VmTemplate> filteredTemplates = AsyncDataProvider.getInstance().filterTemplatesByArchitecture(templatesList,
                dataCenterWithCluster.getCluster().getArchitecture());

        // If there was some template selected before, try select it again.
        VmTemplate prevBaseTemplate = getModel().getBaseTemplate().getSelectedItem();

        getModel().getBaseTemplate().setItems(filteredTemplates);

        getModel().getBaseTemplate().setSelectedItem(Linq.firstOrDefault(filteredTemplates,
                new Linq.TemplatePredicate(prevBaseTemplate != null ? prevBaseTemplate.getId() : Guid.Empty)));

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
}
