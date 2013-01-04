package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;

public class EditNetworkModel extends NetworkModel {

    public static String APPLY_COMMAND_NAME = "Apply"; //$NON-NLS-1$

    public EditNetworkModel(Network network, ListModel sourceListModel) {
        super(network, sourceListModel);
        getDataCenters().setIsChangable(false);
        init();
    }

    private UICommand privateApplyCommand;

    public UICommand getApplyCommand()
    {
        return privateApplyCommand;
    }

    public void setApplyCommand(UICommand value)
    {
        privateApplyCommand = value;
    }

    private void init() {
        setApplyCommand(new UICommand(APPLY_COMMAND_NAME, getSourceListModel()));
        setTitle(ConstantsManager.getInstance().getConstants().editLogicalNetworkTitle());
        setHashName("edit_logical_network"); //$NON-NLS-1$
        getName().setEntity(getNetwork().getName());
        getDescription().setEntity(getNetwork().getDescription());
        getIsStpEnabled().setEntity(getNetwork().getStp());
        getHasVLanTag().setEntity(getNetwork().getVlanId() != null);
        getVLanTag().setEntity((getNetwork().getVlanId() == null ? 0 : getNetwork().getVlanId()));
        initMtu();
        initIsVm();
    }

    @Override
    protected void initIsVm() {
        getIsVmNetwork().setEntity(getNetwork().isVmNetwork());
    }

    @Override
    protected void initMtu() {
        getHasMtu().setEntity(getNetwork().getMtu() != 0);
        getMtu().setEntity(getNetwork().getMtu() != 0 ? String.valueOf(getNetwork().getMtu()) : null);
    }

    @Override
    public void postExecuteSave() {

        if ((Boolean) getIsEnabled().getEntity())
        {
            Frontend.RunAction(VdcActionType.UpdateNetwork,
                    new AddNetworkStoragePoolParameters(getSelectedDc().getId(), getNetwork()),
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendActionAsyncResult result1) {
                            VdcReturnValueBase retVal = result1.getReturnValue();
                            postSaveAction(null,
                                    retVal != null && retVal.getSucceeded());

                        }
                    },
                    null);
        }
        else
        {
            postSaveAction(null, true);
        }
    }

    @Override
    public void onGetClusterList(ArrayList<VDSGroup> clusterList) {
        ArrayList<VdcQueryParametersBase> parametersList =
                new ArrayList<VdcQueryParametersBase>();
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<VdcQueryType>();
        List<NetworkClusterModel> items = new ArrayList<NetworkClusterModel>();
        for (VDSGroup vdsGroup : clusterList)
        {
            queryTypeList.add(VdcQueryType.GetAllNetworksByClusterId);
            parametersList.add(new IdQueryParameters(vdsGroup.getId()));
            NetworkClusterModel tempVar = new NetworkClusterModel(vdsGroup);
            tempVar.setAttached(false);
            items.add(tempVar);
        }
        getNetworkClusterList().setItems(items);
        Frontend.RunMultipleQueries(queryTypeList,
                parametersList,
                new IFrontendMultipleQueryAsyncCallback() {

                    @Override
                    public void Executed(FrontendMultipleQueryAsyncResult result) {
                        onGetAllNetworksByClusterId(result);

                    }

                });
    }

    public void onGetAllNetworksByClusterId(FrontendMultipleQueryAsyncResult result)
    {
        List<VdcQueryReturnValue> returnValueList = result.getReturnValues();
        List<Network> clusterNetworkList = null;
        final List<NetworkClusterModel> networkClusterList =
                (List<NetworkClusterModel>) getNetworkClusterList().getItems();
        boolean networkHasAttachedClusters = false;
        for (int i = 0; i < returnValueList.size(); i++)
        {
            VdcQueryReturnValue returnValue = returnValueList.get(i);
            if (returnValue.getSucceeded() && returnValue.getReturnValue() != null)
            {
                clusterNetworkList = (List<Network>) returnValue.getReturnValue();
                for (Network clusterNetwork : clusterNetworkList)
                {
                    if (clusterNetwork.getId().equals(getNetwork().getId()))
                    {
                        getOriginalClusters().add(networkClusterList.get(i).getEntity());
                        networkClusterList.get(i).setAttached(true);
                        networkHasAttachedClusters = true;
                        break;
                    }
                }
            }
        }

        if (networkHasAttachedClusters)
        {
            getIsEnabled().setEntity(false);
        } else {
            getIsEnabled().setEntity(true);
            if (isManagemet()) {
                getName().setIsChangable(false);
            }
        }

        if (isManagemet())
        {
            // cannot detach engine networks from clusters
            for (Object item : getNetworkClusterList()
                    .getItems())
            {
                ((NetworkClusterModel) item).setIsChangable(false);
            }
            getApplyCommand().setIsExecutionAllowed(false);
            getName().setIsChangable(false);
            setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .cannotDetachManagementNetworkFromClustersMsg());
        }

        refreshClustersTable();

        if (firstInit) {
            firstInit = false;
            addCommands();
        }
    }

    @Override
    protected void addCommands() {
        if (isManagemet()
                && ((List<NetworkClusterModel>) getNetworkClusterList().getItems()).size() > 0)
        {
            UICommand tempVar = new UICommand("Cancel", this); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
            tempVar.setIsDefault(true);
            tempVar.setIsCancel(true);
            getCommands().add(tempVar);
        }
        else
        {
            UICommand tempVar2 = new UICommand("OnSave", this); //$NON-NLS-1$
            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().ok());
            tempVar2.setIsDefault(true);
            getCommands().add(tempVar2);
            UICommand tempVar3 = new UICommand("Cancel", this); //$NON-NLS-1$
            tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
            tempVar3.setIsCancel(true);
            getCommands().add(tempVar3);
        }
    }

    public void apply() {
        ConfirmationModel confirmModel = new ConfirmationModel();
        getSourceListModel().setConfirmWindow(confirmModel);
        confirmModel.setTitle(ConstantsManager.getInstance().getConstants().attachDetachNetworkToFromClustersTitle());
        confirmModel.setHashName("attach_detach_network_to_from_clusters"); //$NON-NLS-1$
        confirmModel.setMessage(ConstantsManager.getInstance()
                .getConstants()
                .youAreAboutToAttachDetachNetworkToFromTheClustersMsg());
        confirmModel.getLatch().setIsAvailable(true);
        UICommand tempVar = new UICommand("OnApply", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        confirmModel.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("CancelConfirmation", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        confirmModel.getCommands().add(tempVar2);
    }

    boolean firstFinished;

    public void onApply()
    {
        final ConfirmationModel confirmationModel = (ConfirmationModel) getSourceListModel().getConfirmWindow();

        if (!confirmationModel.Validate())
        {
            return;
        }

        // Init default NetworkCluster values (required, display, status)
        getNetwork().setCluster(new NetworkCluster());

        final ArrayList<VDSGroup> detachNetworkFromClusters =
                Linq.Except(getOriginalClusters(), getnewClusters());
        final ArrayList<VdcActionParametersBase> toDetach =
                new ArrayList<VdcActionParametersBase>();

        for (VDSGroup detachNetworkFromCluster : detachNetworkFromClusters)
        {
            toDetach.add(new AttachNetworkToVdsGroupParameter(detachNetworkFromCluster,
                    getNetwork()));
        }

        final ArrayList<VDSGroup> attachNetworkToClusters =
                Linq.Except(getnewClusters(), getOriginalClusters());
        final ArrayList<VdcActionParametersBase> toAttach =
                new ArrayList<VdcActionParametersBase>();

        for (VDSGroup attachNetworkToCluster : attachNetworkToClusters)
        {
            toAttach.add(new AttachNetworkToVdsGroupParameter(attachNetworkToCluster, getNetwork()));
        }

        if (!toAttach.isEmpty() || !toDetach.isEmpty()) {
            confirmationModel.StartProgress(null);
        } else {
            cancelConfirmation();
        }
        ;

        getOriginalClusters().clear();
        getOriginalClusters().addAll(attachNetworkToClusters);
        firstFinished = toAttach.isEmpty() || toDetach.isEmpty();

        if (!toAttach.isEmpty()) {
            Frontend.RunMultipleAction(VdcActionType.AttachNetworkToVdsGroup,
                    toAttach,
                    new IFrontendMultipleActionAsyncCallback() {

                        @Override
                        public void Executed(FrontendMultipleActionAsyncResult result) {
                            executedAttachDetach(attachNetworkToClusters, result);

                        }
                    },
                    null);
        }

        if (!toDetach.isEmpty()) {
            Frontend.RunMultipleAction(VdcActionType.DetachNetworkToVdsGroup,
                    toDetach,
                    new IFrontendMultipleActionAsyncCallback() {

                        @Override
                        public void Executed(FrontendMultipleActionAsyncResult result) {
                            executedAttachDetach(detachNetworkFromClusters, result);

                        }
                    },
                    null);
        }
    }

    private void executedAttachDetach(ArrayList<VDSGroup> clustersList, FrontendMultipleActionAsyncResult result) {
        // Check if actions succeeded
        List<VdcReturnValueBase> returnValueList = result.getReturnValue();

        // The multiple action failed- roll back all the actions
        if (returnValueList == null) {
            for (VDSGroup cluster : clustersList) {
                // Roll back the assigned value of the cluster
                NetworkClusterModel networkClusterModel = findNetworkClusterModel(cluster);
                networkClusterModel.setAttached(!networkClusterModel.isAttached());

                if (networkClusterModel.isAttached()) {
                    getOriginalClusters().add(networkClusterModel.getEntity());
                } else {
                    getOriginalClusters().remove(networkClusterModel.getEntity());
                }
            }

            // Call setItems to raise the itemsChanged events
            getNetworkClusterList()
                    .getItemsChangedEvent()
                    .raise(getNetworkClusterList(), EventArgs.Empty);

        } else { // Check if some of the actions failed
            boolean itemsUpdated = false;
            for (int i = 0; i < returnValueList.size(); i++)
            {
                VdcReturnValueBase returnValue = returnValueList.get(i);
                if (returnValue == null || !returnValue.getCanDoAction())
                {
                    // Roll back the assigned value of the cluster
                    NetworkClusterModel networkClusterModel = findNetworkClusterModel(clustersList.get(i));
                    networkClusterModel.setAttached(!networkClusterModel.isAttached());

                    if (networkClusterModel.isAttached()) {
                        getOriginalClusters().add(networkClusterModel.getEntity());
                    } else {
                        getOriginalClusters().remove(networkClusterModel.getEntity());
                    }

                    itemsUpdated = true;
                }
                // Call setItems to raise the itemsChanged events
                if (itemsUpdated) {
                    refreshClustersTable();
                }
            }
        }
        if (!firstFinished) {
            firstFinished = true;
        } else {
            getSourceListModel().getConfirmWindow().StopProgress();
            cancelConfirmation();

            // Check if network has attached cluster
            boolean hasAttachedCluster = false;
            for (Object item : getNetworkClusterList().getItems()) {
                NetworkClusterModel networkClusterModel = (NetworkClusterModel) item;
                if (networkClusterModel.isAttached()) {
                    hasAttachedCluster = true;
                    break;
                }
            }

            if (hasAttachedCluster) {
                getIsEnabled().setEntity(false);
            } else {
                getIsEnabled().setEntity(true);
            }
        }
    }

    private void cancelConfirmation() {
        getSourceListModel().setConfirmWindow(null);
    }

    @Override
    protected void onIsEnableChange() {
        super.onIsEnableChange();
        if (!isManagemet()) {
            getApplyCommand().setIsExecutionAllowed(!(Boolean) (getIsEnabled().getEntity()));
        }
    }

    @Override
    public void ExecuteCommand(UICommand command) {
        super.ExecuteCommand(command);
        if (StringHelper.stringsEqual(command.getName(), "OnApply")) //$NON-NLS-1$
        {
            onApply();
        }
        else if (StringHelper.stringsEqual(command.getName(), "CancelConfirmation")) //$NON-NLS-1$
        {
            cancelConfirmation();
        }
    }
}
