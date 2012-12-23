package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class NewNetworkModel extends NetworkModel {

    public NewNetworkModel(ListModel sourceListModel) {
        super(sourceListModel);
        init();
    }

    private void init() {
        setTitle(ConstantsManager.getInstance().getConstants().newLogicalNetworkTitle());
        setHashName("new_logical_network"); //$NON-NLS-1$
    }

    @Override
    public void postExecuteSave() {
        // New network
        final AddNetworkStoragePoolParameters parameters =
                new AddNetworkStoragePoolParameters(getSelectedDc().getId(), getNetwork());
        parameters.setPublicUse((Boolean) getPublicUse().getEntity());
        Frontend.RunAction(VdcActionType.AddNetwork,
                parameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result1) {
                        VdcReturnValueBase retVal = result1.getReturnValue();
                        boolean succeeded = false;
                        if (retVal != null && retVal.getSucceeded())
                        {
                            succeeded = true;
                        }
                        postSaveAction(succeeded ? (Guid) retVal.getActionReturnValue()
                                : null,
                                succeeded);

                    }
                },
                null);
    }

    @Override
    public void onGetClusterList(ArrayList<VDSGroup> clusterList) {
        // Cluster list
        List<NetworkClusterModel> items = new ArrayList<NetworkClusterModel>();
        for (VDSGroup cluster : clusterList)
        {
            items.add(createNetworkClusterModel(cluster));
        }
        getNetworkClusterList().setItems(items);

        if (firstInit) {
            firstInit = false;
            addCommands();
        }
    }

    @Override
    protected void addCommands() {
        UICommand tempVar = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance()
                .getConstants()
                .cancel());
        tempVar2.setIsCancel(true);
        getCommands().add(tempVar2);
    }

    protected NetworkClusterModel createNetworkClusterModel(VDSGroup cluster) {
        NetworkClusterModel networkClusterModel = new NetworkClusterModel(cluster);
        networkClusterModel.setAttached(false);

        return networkClusterModel;
    }

    @Override
    protected void initMtu() {
        getHasMtu().setEntity(false);
        getMtu().setEntity(null);
    }

    @Override
    protected void initIsVm() {
        getIsVmNetwork().setEntity(true);
    }
}
