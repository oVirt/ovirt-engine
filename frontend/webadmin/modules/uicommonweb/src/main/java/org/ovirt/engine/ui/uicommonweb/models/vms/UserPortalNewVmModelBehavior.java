package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.Collections;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
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
    public void Initialize(SystemTreeItemModel systemTreeSelectedItem)
    {
        // Get datacenters with permitted create action
        AsyncDataProvider.GetDataCentersWithPermittedActionOnClusters(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        java.util.ArrayList<storage_pool> list = new java.util.ArrayList<storage_pool>();
                        for (storage_pool a : (java.util.ArrayList<storage_pool>) returnValue)
                        {
                            if (a.getstatus() == StoragePoolStatus.Up)
                            {
                                list.add(a);
                            }
                        }
                        model.setIsDatacenterAvailable(list.size() > 0);
                        model.SetDataCenter(model, list);

                    }
                }, getModel().getHash()), CREATE_VM);
    }

    @Override
    public void DataCenter_SelectedItemChanged()
    {
        storage_pool dataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();
        getModel().setIsHostAvailable(dataCenter.getstorage_pool_type() != StorageType.LOCALFS);

        java.util.ArrayList<VdcQueryType> queryTypeList = new java.util.ArrayList<VdcQueryType>();
        queryTypeList.add(VdcQueryType.GetClustersWithPermittedAction);
        queryTypeList.add(VdcQueryType.GetVmTemplatesWithPermittedAction);

        GetEntitiesWithPermittedActionParameters tempVar = new GetEntitiesWithPermittedActionParameters();
        tempVar.setActionGroup(CREATE_VM);
        GetEntitiesWithPermittedActionParameters getEntitiesWithPermittedActionParameters = tempVar;

        java.util.ArrayList<VdcQueryParametersBase> parametersList =
                new java.util.ArrayList<VdcQueryParametersBase>(java.util.Arrays.asList(new VdcQueryParametersBase[] {
                        getEntitiesWithPermittedActionParameters, getEntitiesWithPermittedActionParameters }));

        // Get clusters and templates
        Frontend.RunMultipleQueries(queryTypeList, parametersList, this, getModel().getHash());
    }

    @Override
    public void Executed(FrontendMultipleQueryAsyncResult result)
    {
        java.util.List<VdcQueryReturnValue> returnValueList = result.getReturnValues();
        java.util.ArrayList<VDSGroup> clusters =
                (java.util.ArrayList<VDSGroup>) returnValueList.get(0).getReturnValue();
        java.util.ArrayList<VmTemplate> templates =
                (java.util.ArrayList<VmTemplate>) returnValueList.get(1).getReturnValue();

        InitClusters(clusters);
        InitTemplates(templates);
        InitCdImage();
    }

    private void InitClusters(java.util.ArrayList<VDSGroup> clusters)
    {
        // Filter clusters list (include only clusters that belong to the selected datacenter)
        java.util.ArrayList<VDSGroup> filteredList = new java.util.ArrayList<VDSGroup>();
        storage_pool selectedDataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();

        for (VDSGroup cluster : clusters)
        {
            if (cluster.getstorage_pool_id() != null && selectedDataCenter.getId().equals(cluster.getstorage_pool_id()))
            {
                filteredList.add(cluster);
            }
        }

        Collections.sort(filteredList, new Linq.VdsGroupByNameComparer());
        getModel().SetClusters(getModel(), filteredList, null);
    }

    private void InitTemplates(java.util.ArrayList<VmTemplate> templates)
    {
        // Filter templates list (include only templates that belong to the selected datacenter)
        java.util.ArrayList<VmTemplate> templatesList = new java.util.ArrayList<VmTemplate>();
        VmTemplate blankTemplate = new VmTemplate();
        storage_pool selectedDataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();
        Guid selectedDataCenterId = selectedDataCenter.getId().getValue();

        for (VmTemplate template : templates)
        {
            Guid datacenterId =
                    template.getstorage_pool_id() == null ? NGuid.Empty : template.getstorage_pool_id().getValue();

            if (template.getId().equals(NGuid.Empty))
            {
                blankTemplate = template;
            }
            else if (!selectedDataCenterId.equals(datacenterId))
            {
                continue;
            }
            else if (template.getstatus() == VmTemplateStatus.OK)
            {
                templatesList.add(template);
            }
        }

        // Sort list and position "Blank" template as first
        Collections.sort(templatesList, new Linq.VmTemplateByNameComparer());
        if (templates.contains(blankTemplate))
        {
            templatesList.add(0, blankTemplate);
        }

        // If there was some template selected before, try select it again.
        VmTemplate oldTemplate = (VmTemplate) getModel().getTemplate().getSelectedItem();

        getModel().getTemplate().setItems(templatesList);

        getModel().getTemplate().setSelectedItem(Linq.FirstOrDefault(templatesList,
                oldTemplate != null ? new Linq.TemplatePredicate(oldTemplate.getId())
                        : new Linq.TemplatePredicate(NGuid.Empty)));

        UpdateIsDisksAvailable();
    }
}
