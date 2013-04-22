package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.GetHostsByClusterIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;

public class UserPortalExistingVmModelBehavior extends ExistingVmModelBehavior
{
    private static final ActionGroup CREATE_VM = ActionGroup.CREATE_VM;

    public UserPortalExistingVmModelBehavior(VM vm)
    {
        super(vm);
    }

    @Override
    public void Initialize(SystemTreeItemModel systemTreeSelectedItem) {
        super.Initialize(systemTreeSelectedItem);

        // The custom properties tab should be hidden on the User Portal
        getModel().setIsCustomPropertiesTabAvailable(false);
    }

    @Override
    public void DataCenter_SelectedItemChanged()
    {
        storage_pool dataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();
        getModel().setIsHostAvailable(dataCenter.getstorage_pool_type() != StorageType.LOCALFS);

        // Get clusters with permitted edit action
        AsyncDataProvider.GetClustersWithPermittedAction(new AsyncQuery(new Object[] { this, getModel() },
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        Object[] array = (Object[]) target;
                        ExistingVmModelBehavior behavior = (ExistingVmModelBehavior) array[0];
                        UnitVmModel model = (UnitVmModel) array[1];
                        ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;
                        InitClusters(clusters, model);
                        behavior.InitTemplate();
                        behavior.InitCdImage();

                    }
                }, getModel().getHash()), CREATE_VM, true, false);

        if (dataCenter.getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED) {
            getModel().getQuota().setIsAvailable(true);
        } else {
            getModel().getQuota().setIsAvailable(false);
        }
    }

    @Override
    protected void UpdateCdImage() {
        updateUserCdImage(getVm().getStoragePoolId());
    }

    private void InitClusters(ArrayList<VDSGroup> clusters, UnitVmModel model)
    {
        // Filter clusters list (include only clusters that belong to the selected datacenter)
        ArrayList<VDSGroup> filteredList = new ArrayList<VDSGroup>();
        storage_pool selectedDataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();
        boolean listContainsVmCluster = false;

        for (VDSGroup cluster : clusters)
        {
            if (cluster.getStoragePoolId() != null && selectedDataCenter.getId().equals(cluster.getStoragePoolId()))
            {
                filteredList.add(cluster);

                if (Guid.OpEquality(cluster.getId(), vm.getVdsGroupId().getValue()))
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
            model.SetClusters(model, filteredList, vm.getVdsGroupId().getValue());
        }
    }

    private void AddVmCluster(ArrayList<VDSGroup> clusters)
    {
        AsyncDataProvider.GetClusterById(new AsyncQuery(new Object[] { getModel(), clusters },
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        Object[] array = (Object[]) target;
                        UnitVmModel model = (UnitVmModel) array[0];
                        ArrayList<VDSGroup> clusterList = (ArrayList<VDSGroup>) array[1];
                        VDSGroup cluster = (VDSGroup) returnValue;
                        if (cluster != null)
                        {
                            clusterList.add(cluster);
                        }
                        Collections.sort(clusterList, new Linq.VdsGroupByNameComparer());
                        model.SetClusters(model, clusterList, vm.getVdsGroupId().getValue());

                    }
                }, getModel().getHash()), vm.getVdsGroupId());
    }

    /**
     * Fills the default host according to the selected host set in webadmin. Since this value can be set only in
     * webadmin and can be set also to host, which is not visible to the user in userportal, this fakes the VDS value in
     * a way, that the rest of the code can use it normally and send it back to the server as-is (like Null Object
     * Pattern).
     */
    @Override
    protected void doChangeDefautlHost(NGuid hostGuid) {
        if (hostGuid != null) {
            VDS vds = new VDS();
            vds.setId(hostGuid.getValue());
            getModel().getDefaultHost().setItems(Arrays.asList(vds));
        }

        super.doChangeDefautlHost(hostGuid);
    }

    @Override
    protected void getHostListByCluster(VDSGroup cluster, AsyncQuery query) {
        Frontend.RunQuery(
                VdcQueryType.GetHostsByClusterId,
                new GetHostsByClusterIdParameters(cluster.getId()),
                query
                );
    }
}
