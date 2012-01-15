package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.Collections;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;

@SuppressWarnings("unused")
public class UserPortalExistingVmModelBehavior extends ExistingVmModelBehavior
{
    private static final ActionGroup EDIT_VM_PROPERTIES = ActionGroup.EDIT_VM_PROPERTIES;

    public UserPortalExistingVmModelBehavior(VM vm)
    {
        super(vm);
    }

    @Override
    public void DataCenter_SelectedItemChanged()
    {
        // Get clusters with permitted edit action
        AsyncDataProvider.GetClustersWithPermittedAction(new AsyncQuery(new Object[] { this, getModel() },
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        Object[] array = (Object[]) target;
                        ExistingVmModelBehavior behavior = (ExistingVmModelBehavior) array[0];
                        UnitVmModel model = (UnitVmModel) array[1];
                        java.util.ArrayList<VDSGroup> clusters = (java.util.ArrayList<VDSGroup>) returnValue;
                        InitClusters(clusters, model);
                        behavior.InitTemplate();
                        behavior.InitCdImage();

                    }
                }, getModel().getHash()), EDIT_VM_PROPERTIES);
    }

    private void InitClusters(java.util.ArrayList<VDSGroup> clusters, UnitVmModel model)
    {
        // Filter clusters list (include only clusters that belong to the selected datacenter)
        java.util.ArrayList<VDSGroup> filteredList = new java.util.ArrayList<VDSGroup>();
        storage_pool selectedDataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();
        boolean listContainsVmCluster = false;

        for (VDSGroup cluster : clusters)
        {
            if (cluster.getstorage_pool_id() != null && selectedDataCenter.getId().equals(cluster.getstorage_pool_id()))
            {
                filteredList.add(cluster);

                if (Guid.OpEquality(cluster.getID(), vm.getvds_group_id().getValue()))
                {
                    listContainsVmCluster = true;
                }
            }
        }

        if (!listContainsVmCluster)
        {
            // Add VM's cluster if not contained in the cluster list
            AddVmCluster(filteredList);
        }
        else
        {
            Collections.sort(filteredList, new Linq.VdsGroupByNameComparer());
            model.SetClusters(model, filteredList, vm.getvds_group_id().getValue());
        }
    }

    private void AddVmCluster(java.util.ArrayList<VDSGroup> clusters)
    {
        AsyncDataProvider.GetClusterById(new AsyncQuery(new Object[] { getModel(), clusters },
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        Object[] array = (Object[]) target;
                        UnitVmModel model = (UnitVmModel) array[0];
                        java.util.ArrayList<VDSGroup> clusterList = (java.util.ArrayList<VDSGroup>) array[1];
                        VDSGroup cluster = (VDSGroup) returnValue;
                        if (cluster != null)
                        {
                            clusterList.add(cluster);
                        }
                        Collections.sort(clusterList, new Linq.VdsGroupByNameComparer());
                        model.SetClusters(model, clusterList, vm.getvds_group_id().getValue());

                    }
                }, getModel().getHash()), vm.getvds_group_id());
    }
}
