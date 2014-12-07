package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;

public class UserPortalExistingVmModelBehavior extends ExistingVmModelBehavior
{
    public UserPortalExistingVmModelBehavior(VM vm)
    {
        super(vm);
    }

    @Override
    public void initialize(SystemTreeItemModel systemTreeSelectedItem) {
        super.initialize(systemTreeSelectedItem);

        // The custom properties tab should be hidden on the User Portal
        getModel().setIsCustomPropertiesTabAvailable(false);
    }

    @Override
    protected void initClusters(final List<StoragePool> dataCenters) {
        // Get clusters with permitted edit action
        ActionGroup actionGroup = getModel().isCreateInstanceOnly() ? ActionGroup.CREATE_INSTANCE : ActionGroup.CREATE_VM;
        AsyncDataProvider.getInstance().getClustersWithPermittedAction(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        ExistingVmModelBehavior behavior = UserPortalExistingVmModelBehavior.this;
                        UnitVmModel model = (UnitVmModel) target;
                        List<VDSGroup> clusters = (List<VDSGroup>) returnValue;

                        // filter clusters by architecture
                        clusters = AsyncDataProvider.getInstance().filterByArchitecture(clusters, vm.getClusterArch());

                        if (containsVmCluster(clusters)) {
                            Collections.sort(clusters, new NameableComparator());
                            model.setDataCentersAndClusters(model, dataCenters, clusters, vm.getVdsGroupId());
                        } else {
                            // Add VM's cluster if not contained in the cluster list
                            addVmCluster(dataCenters, clusters);
                        }

                        behavior.initTemplate();
                        behavior.initCdImage();

                    }
                }), actionGroup, true, false);

    }


    private boolean containsVmCluster(List<VDSGroup> clusters) {

        for (VDSGroup cluster : clusters) {
            if (cluster.getStoragePoolId() != null) {
                if (vm.getVdsGroupId().equals(cluster.getId())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void updateCdImage() {
        updateUserCdImage(getVm().getStoragePoolId());
    }

    private void addVmCluster(final List<StoragePool> dataCenters, final List<VDSGroup> clusters)
    {
        AsyncDataProvider.getInstance().getClusterById(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        VDSGroup cluster = (VDSGroup) returnValue;
                        if (cluster != null)
                        {
                            clusters.add(cluster);
                        }
                        Collections.sort(clusters, new NameableComparator());
                        model.setDataCentersAndClusters(model, dataCenters, clusters, vm.getVdsGroupId());
                    }
                }), vm.getVdsGroupId());
    }

    /**
     * Fills the default host according to the selected host set in webadmin. Since this value can be set only in
     * webadmin and can be set also to host, which is not visible to the user in userportal, this fakes the VDS value in
     * a way, that the rest of the code can use it normally and send it back to the server as-is (like Null Object
     * Pattern).
     */
    @Override
    protected void doChangeDefautlHost(Guid hostGuid) {
        if (hostGuid != null) {
            VDS vds = new VDS();
            vds.setId(hostGuid);
            getModel().getDefaultHost().setItems(Arrays.asList(vds));
        }

        super.doChangeDefautlHost(hostGuid);
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
    protected void updateNumaEnabled() {
    }
}
