package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class NewNetworkModel extends NetworkModel {

    private ListModel privateNetworkClusterList;

    public NewNetworkModel(ListModel sourceListModel) {
        super(sourceListModel);
        setNetworkClusterList(new ListModel());
        init();
    }

    public ListModel getNetworkClusterList()
    {
        return privateNetworkClusterList;
    }

    public void setNetworkClusterList(ListModel value)
    {
        privateNetworkClusterList = value;
    }

    private void init() {
        setTitle(ConstantsManager.getInstance().getConstants().newLogicalNetworkTitle());
        setHashName("new_logical_network"); //$NON-NLS-1$
    }

    @Override
    public void syncWithBackend() {
        super.syncWithBackend();
        // Get dc- cluster list
        AsyncDataProvider.getClusterList(new AsyncQuery(NewNetworkModel.this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object ReturnValue)
                    {
                        onGetClusterList((ArrayList<VDSGroup>) ReturnValue);
                    }
                }), getSelectedDc().getId());
    }

    protected void onGetClusterList(ArrayList<VDSGroup> clusterList) {
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

    protected NetworkClusterModel createNetworkClusterModel(VDSGroup cluster) {
        NetworkClusterModel networkClusterModel = new NetworkClusterModel(cluster);
        networkClusterModel.setAttached(true);

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

    @Override
    protected void onExportChanged() {
        boolean externalNetwork = (Boolean) getExport().getEntity();
        getExternalProviders().setIsChangable(externalNetwork);
        getNetworkLabel().setIsChangable(externalNetwork);
        getIsVmNetwork().setIsChangable(!externalNetwork);
        getHasMtu().setIsChangable(!externalNetwork);
        if (externalNetwork) {
            getIsVmNetwork().setEntity(true);
            getHasMtu().setEntity(false);
        }
    }

    @Override
    protected void executeSave() {
        IFrontendActionAsyncCallback addNetworkCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result1) {
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
        };

        // New network
        if ((Boolean) getExport().getEntity()) {
            Provider externalProvider = (Provider) getExternalProviders().getSelectedItem();
            ProviderNetwork providerNetwork = new ProviderNetwork();
            providerNetwork.setProviderId(externalProvider.getId());
            getNetwork().setProvidedBy(providerNetwork);
            final AddNetworkStoragePoolParameters parameters =
                    new AddNetworkStoragePoolParameters(getSelectedDc().getId(), getNetwork());
            Frontend.RunAction(VdcActionType.AddNetworkOnProvider,
                    parameters, addNetworkCallback, null);
        } else {
            final AddNetworkStoragePoolParameters parameters =
                    new AddNetworkStoragePoolParameters(getSelectedDc().getId(), getNetwork());
            Frontend.RunAction(VdcActionType.AddNetwork,
                    parameters,
                    addNetworkCallback,
                    null);
        }
    }

    @Override
    protected void postSaveAction(Guid networkGuid, boolean succeeded) {
        super.postSaveAction(networkGuid, succeeded);

        if (!succeeded) {
            return;
        }

        Guid networkId = getNetwork().getId() == null ? networkGuid : getNetwork().getId();
        ArrayList<VdcActionParametersBase> actionParameters1 =
                new ArrayList<VdcActionParametersBase>();

        for (VDSGroup attachNetworkToCluster : getClustersToAttach())
        {
            Network tempVar = new Network();
            tempVar.setId(networkId);
            tempVar.setName(getNetwork().getName());

            // Init default NetworkCluster values (required, display, status)
            NetworkCluster networkCluster = new NetworkCluster();
            networkCluster.setRequired(!((Boolean) getExport().getEntity()));
            tempVar.setCluster(networkCluster);

            actionParameters1.add(new AttachNetworkToVdsGroupParameter(attachNetworkToCluster, tempVar));
        }

        Frontend.RunMultipleAction(VdcActionType.AttachNetworkToVdsGroup, actionParameters1);
    }

    public ArrayList<VDSGroup> getClustersToAttach()
    {
        ArrayList<VDSGroup> clusterToAttach = new ArrayList<VDSGroup>();

        for (Object item : getNetworkClusterList().getItems())
        {
            NetworkClusterModel networkClusterModel = (NetworkClusterModel) item;
            if (networkClusterModel.isAttached())
            {
                clusterToAttach.add(networkClusterModel.getEntity());
            }
        }
        return clusterToAttach;
    }
}
