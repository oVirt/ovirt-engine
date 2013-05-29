package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
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
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;

@SuppressWarnings("unused")
public class UserPortalNewVmModelBehavior extends NewVmModelBehavior implements IFrontendMultipleQueryAsyncCallback
{
    private static final ActionGroup CREATE_VM = ActionGroup.CREATE_VM;

    @Override
    public void initialize(SystemTreeItemModel systemTreeSelectedItem)
    {
        // The custom properties tab should be hidden on the User Portal
        getModel().setIsCustomPropertiesTabAvailable(false);

        getModel().getIsSoundcardEnabled().setIsChangable(true);
        getModel().getVmType().setIsChangable(true);

        // Get datacenters with permitted create action
        AsyncDataProvider.getDataCentersWithPermittedActionOnClusters(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        final List<StoragePool> dataCenters = new ArrayList<StoragePool>();
                        for (StoragePool a : (ArrayList<StoragePool>) returnValue)
                        {
                            if (a.getstatus() == StoragePoolStatus.Up)
                            {
                                dataCenters.add(a);
                            }
                        }

                        AsyncDataProvider.getClustersWithPermittedAction(
                                new AsyncQuery(getModel(), new INewAsyncCallback() {

                                    @Override
                                    public void onSuccess(Object target, Object returnValue) {
                                        UnitVmModel model = (UnitVmModel) target;
                                        model.setDataCentersAndClusters(model,
                                                dataCenters,
                                                (List<VDSGroup>) returnValue, null);
                                    }
                                }, getModel().getHash()),
                                CREATE_VM, true, false);

                    }
                }, getModel().getHash()), CREATE_VM, true, false);
    }

    @Override
    public void dataCenterWithClusterSelectedItemChanged()
    {
        super.dataCenterWithClusterSelectedItemChanged();

        ArrayList<VdcQueryType> queryTypeList = new ArrayList<VdcQueryType>();
        queryTypeList.add(VdcQueryType.GetVmTemplatesWithPermittedAction);

        GetEntitiesWithPermittedActionParameters getEntitiesWithPermittedActionParameters =
                new GetEntitiesWithPermittedActionParameters();
        getEntitiesWithPermittedActionParameters.setActionGroup(CREATE_VM);

        ArrayList<VdcQueryParametersBase> parametersList =
                new ArrayList<VdcQueryParametersBase>(Arrays.asList(new VdcQueryParametersBase[] {
                        getEntitiesWithPermittedActionParameters }));

        // Get clusters and templates
        Frontend.RunMultipleQueries(queryTypeList, parametersList, this, getModel().getHash());

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

    private void initTemplates(ArrayList<VmTemplate> templates)
    {
        // Filter templates list (include only templates that belong to the selected datacenter)
        ArrayList<VmTemplate> templatesList = new ArrayList<VmTemplate>();
        VmTemplate blankTemplate = null;
        DataCenterWithCluster dataCenterWithCluster =
                (DataCenterWithCluster) getModel().getDataCenterWithClustersList().getSelectedItem();
        StoragePool selectedDataCenter = dataCenterWithCluster.getDataCenter();
        Guid selectedDataCenterId = selectedDataCenter.getId().getValue();
        if (selectedDataCenterId == null) {
            return;
        }

        for (VmTemplate template : templates)
        {
            Guid datacenterId =
                    template.getStoragePoolId() == null ? Guid.Empty : template.getStoragePoolId().getValue();

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
        Collections.sort(templatesList, new Linq.VmTemplateByNameComparer());
        if (blankTemplate != null && templates.contains(blankTemplate))
        {
            templatesList.add(0, blankTemplate);
        }

        // If there was some template selected before, try select it again.
        VmTemplate oldTemplate = (VmTemplate) getModel().getTemplate().getSelectedItem();

        getModel().getTemplate().setItems(templatesList);

        getModel().getTemplate().setSelectedItem(Linq.firstOrDefault(templatesList,
                oldTemplate != null ? new Linq.TemplatePredicate(oldTemplate.getId())
                        : new Linq.TemplatePredicate(Guid.Empty)));

        updateIsDisksAvailable();
    }

    /**
     * Disabled to change this in userportal
     */
    @Override
    protected void updateHostPinning(MigrationSupport migrationSupport) {
    }

    /**
     * Disabled to change this in userportal
     */
    @Override
    protected void doChangeDefautlHost(Guid hostGuid) {
    }

    @Override
    protected void getHostListByCluster(VDSGroup cluster, AsyncQuery query) {
        Frontend.RunQuery(
                VdcQueryType.GetHostsByClusterId,
                new IdQueryParameters(cluster.getId()),
                query
                );
    }
}
