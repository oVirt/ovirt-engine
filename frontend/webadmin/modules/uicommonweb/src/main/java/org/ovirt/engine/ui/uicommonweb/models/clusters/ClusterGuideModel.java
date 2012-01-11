package org.ovirt.engine.ui.uicommonweb.models.clusters;

import org.ovirt.engine.core.common.action.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.ApproveVdsParameters;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Extensions;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.GuideModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.MoveHost;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

@SuppressWarnings("unused")
public class ClusterGuideModel extends GuideModel
{

    public final String ClusterConfigureHostsAction = "Configure Host";
    public final String ClusterAddAnotherHostAction = "Add another Host";
    public final String SelectHostsAction = "Select Hosts";

    @Override
    public VDSGroup getEntity()
    {
        return (VDSGroup) ((super.getEntity() instanceof VDSGroup) ? super.getEntity() : null);
    }

    public void setEntity(VDSGroup value)
    {
        super.setEntity(value);
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();
        UpdateOptions();
    }

    private java.util.ArrayList<VDS> hosts;
    private java.util.ArrayList<VDS> allHosts;
    private java.util.ArrayList<VDSGroup> clusters;
    private VDS localStorageHost;
    private storage_pool dataCenter;

    private void UpdateOptionsNonLocalFSData() {
        AsyncDataProvider.GetHostListByCluster(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        ClusterGuideModel clusterGuideModel = (ClusterGuideModel) target;
                        java.util.ArrayList<VDS> hosts = (java.util.ArrayList<VDS>) returnValue;
                        ;
                        clusterGuideModel.hosts = hosts;
                        clusterGuideModel.UpdateOptionsNonLocalFS();
                    }
                }), getEntity().getname());

        AsyncDataProvider.GetClusterList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        ClusterGuideModel clusterGuideModel = (ClusterGuideModel) target;
                        java.util.ArrayList<VDSGroup> clusters = (java.util.ArrayList<VDSGroup>) returnValue;
                        ;
                        clusterGuideModel.clusters = clusters;
                        clusterGuideModel.UpdateOptionsNonLocalFS();
                    }
                }), getEntity().getstorage_pool_id().getValue());

        AsyncDataProvider.GetHostList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        ClusterGuideModel clusterGuideModel = (ClusterGuideModel) target;
                        java.util.ArrayList<VDS> hosts = (java.util.ArrayList<VDS>) returnValue;
                        ;
                        clusterGuideModel.allHosts = hosts;
                        clusterGuideModel.UpdateOptionsNonLocalFS();
                    }
                }));
    }

    private void UpdateOptionsLocalFSData() {
        AsyncDataProvider.GetLocalStorageHost(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        ClusterGuideModel clusterGuideModel = (ClusterGuideModel) target;
                        VDS localStorageHost = (VDS) returnValue;
                        ;
                        clusterGuideModel.localStorageHost = localStorageHost;
                        clusterGuideModel.UpdateOptionsLocalFS();
                    }
                }), dataCenter.getname());
    }

    private void UpdateOptionsNonLocalFS() {
        if (clusters == null || hosts == null || allHosts == null) {
            return;
        }

        // Add host action.
        UICommand addHostAction = new UICommand("AddHost", this);

        if (hosts.size() > 1)
        {
            hosts.remove(0);
        }

        if (hosts.isEmpty())
        {
            addHostAction.setTitle(ClusterConfigureHostsAction);
            getCompulsoryActions().add(addHostAction);
        }
        else
        {
            addHostAction.setTitle(ClusterAddAnotherHostAction);
            getOptionalActions().add(addHostAction);
        }
        if (getEntity().getstorage_pool_id() == null)
        {
            addHostAction.setIsExecutionAllowed(false);
            addHostAction.getExecuteProhibitionReasons().add("The Cluster isn't attached to a Data Center");
            return;
        }

        Version minimalClusterVersion = Linq.GetMinVersionByClusters(clusters);
        java.util.ArrayList<VDS> availableHosts = new java.util.ArrayList<VDS>();
        for (VDS vds : allHosts)
        {
            if ((!Linq.IsHostBelongsToAnyOfClusters(clusters, vds))
                    && (vds.getstatus() == VDSStatus.Maintenance || vds.getstatus() == VDSStatus.PendingApproval)
                    && (vds.getVersion().getFullVersion() == null || Extensions.GetFriendlyVersion(vds.getVersion()
                            .getFullVersion()).compareTo(minimalClusterVersion) >= 0))
            {
                availableHosts.add(vds);
            }
        }
        // Select host action.
        UICommand selectHostAction = new UICommand("SelectHost", this);

        if (availableHosts.size() > 0 && clusters.size() > 0)
        {
            if (hosts.isEmpty())
            {
                selectHostAction.setTitle(SelectHostsAction);
                getCompulsoryActions().add(selectHostAction);
            }
            else
            {
                selectHostAction.setTitle(SelectHostsAction);
                getOptionalActions().add(selectHostAction);
            }
        }

        StopProgress();
    }

    private void UpdateOptionsLocalFS() {
        UICommand tempVar = new UICommand("AddHost", this);
        tempVar.setTitle(ClusterAddAnotherHostAction);
        UICommand addHostAction = tempVar;
        UICommand tempVar2 = new UICommand("SelectHost", this);
        tempVar2.setTitle(SelectHostsAction);
        UICommand selectHost = tempVar2;

        if (localStorageHost != null)
        {
            addHostAction.setIsExecutionAllowed(false);
            selectHost.setIsExecutionAllowed(false);
            String hasHostReason = "This Cluster belongs to a Local Data Center which already contain a Host";
            addHostAction.getExecuteProhibitionReasons().add(hasHostReason);
            selectHost.getExecuteProhibitionReasons().add(hasHostReason);
        }

        getCompulsoryActions().add(addHostAction);
        getOptionalActions().add(selectHost);

        StopProgress();
    }

    private void UpdateOptions()
    {
        getCompulsoryActions().clear();
        getOptionalActions().clear();

        if (getEntity() != null)
        {
            StartProgress(null);

            AsyncDataProvider.GetDataCenterById(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {
                            ClusterGuideModel model = (ClusterGuideModel) target;
                            model.dataCenter = (storage_pool) returnValue;

                            if (model.dataCenter == null
                                    || model.dataCenter.getstorage_pool_type() != StorageType.LOCALFS)
                            {
                                model.UpdateOptionsNonLocalFSData();
                            }
                            else
                            {
                                model.UpdateOptionsLocalFSData();
                            }
                        }
                    }), getEntity().getstorage_pool_id().getValue());
        }
    }

    private void ResetData() {
        hosts = null;
        allHosts = null;
        clusters = null;
        localStorageHost = null;
        dataCenter = null;
    }

    public void SelectHost()
    {
        java.util.ArrayList<VDSGroup> clusters = new java.util.ArrayList<VDSGroup>();
        clusters.add(getEntity());

        MoveHost model = new MoveHost();
        model.setTitle("Select Host");
        model.setHashName("select_host");
        setWindow(model);
        model.getCluster().setItems(clusters);
        model.getCluster().setSelectedItem(Linq.FirstOrDefault(clusters));
        model.getCluster().setIsAvailable(false);

        UICommand tempVar = new UICommand("OnSelectHost", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnSelectHost()
    {
        MoveHost model = (MoveHost) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.Validate())
        {
            return;
        }

        model.setSelectedHosts(new java.util.ArrayList<VDS>());
        for (EntityModel a : Linq.<EntityModel> Cast(model.getItems()))
        {
            if (a.getIsSelected())
            {
                model.getSelectedHosts().add((VDS) a.getEntity());
            }
        }

        VDSGroup cluster = (VDSGroup) model.getCluster().getSelectedItem();

        java.util.ArrayList<VdcActionParametersBase> paramerterList =
                new java.util.ArrayList<VdcActionParametersBase>();
        for (VDS host : model.getSelectedHosts())
        {
            // Try to change host's cluster as neccessary.
            if (host.getvds_group_id() != null && !host.getvds_group_id().equals(cluster.getID()))
            {
                paramerterList.add(new ChangeVDSClusterParameters(cluster.getID(), host.getvds_id()));

            }
        }
        model.StartProgress(null);
        Frontend.RunMultipleAction(VdcActionType.ChangeVDSCluster, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        ClusterGuideModel clusterGuideModel = (ClusterGuideModel) result.getState();
                        java.util.ArrayList<VDS> hosts = ((MoveHost) clusterGuideModel.getWindow()).getSelectedHosts();
                        java.util.ArrayList<VdcReturnValueBase> retVals =
                                (java.util.ArrayList<VdcReturnValueBase>) result.getReturnValue();
                        if (retVals != null && hosts.size() == retVals.size())
                        {
                            int i = 0;
                            for (VDS selectedHost : hosts)
                            {
                                if (selectedHost.getstatus() == VDSStatus.PendingApproval && retVals.get(i) != null
                                        && retVals.get(i).getSucceeded())
                                {
                                    Frontend.RunAction(VdcActionType.ApproveVds,
                                            new ApproveVdsParameters(selectedHost.getvds_id()));
                                }
                            }
                            i++;
                        }
                        clusterGuideModel.getWindow().StopProgress();
                        clusterGuideModel.Cancel();
                        clusterGuideModel.PostAction();

                    }
                },
                this);
    }

    public void AddHost()
    {
        HostModel model = new HostModel();
        setWindow(model);
        model.setTitle("New Host");
        model.setHashName("new_host");
        model.getPort().setEntity(54321);
        model.getOverrideIpTables().setEntity(true);

        model.getCluster().setSelectedItem(getEntity());
        model.getCluster().setIsChangable(false);

        AsyncDataProvider.GetDataCenterList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        ClusterGuideModel clusterGuideModel = (ClusterGuideModel) target;
                        HostModel model = (HostModel) clusterGuideModel.getWindow();

                        java.util.ArrayList<storage_pool> dataCenters = (java.util.ArrayList<storage_pool>) returnValue;
                        ;
                        model.getDataCenter().setItems(dataCenters);
                        if (getEntity().getstorage_pool_id() != null)
                        {
                            model.getDataCenter().setSelectedItem(Linq.FirstOrDefault(dataCenters,
                                    new Linq.DataCenterPredicate(clusterGuideModel.getEntity()
                                            .getstorage_pool_id()
                                            .getValue())));
                        }
                        model.getDataCenter().setIsChangable(false);

                        UICommand tempVar = new UICommand("OnConfirmPMHost", clusterGuideModel);
                        tempVar.setTitle("OK");
                        tempVar.setIsDefault(true);
                        model.getCommands().add(tempVar);
                        UICommand tempVar2 = new UICommand("Cancel", clusterGuideModel);
                        tempVar2.setTitle("Cancel");
                        tempVar2.setIsCancel(true);
                        model.getCommands().add(tempVar2);
                    }
                }));
    }

    public void OnConfirmPMHost()
    {
        HostModel model = (HostModel) getWindow();

        if (!model.Validate())
        {
            return;
        }

        if (!((Boolean) model.getIsPm().getEntity()))
        {
            ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle("Power Management Configuration");
            confirmModel.setHashName("power_management_configuration");
            confirmModel.setMessage("You haven't configured Power Management for this Host. Are you sure you want to continue?");

            UICommand tempVar = new UICommand("OnAddHost", this);
            tempVar.setTitle("OK");
            tempVar.setIsDefault(true);
            confirmModel.getCommands().add(tempVar);
            UICommand tempVar2 = new UICommand("CancelConfirmWithFocus", this);
            tempVar2.setTitle("Cancel");
            tempVar2.setIsCancel(true);
            confirmModel.getCommands().add(tempVar2);
        }
        else
        {
            OnAddHost();
        }
    }

    public void OnAddHost()
    {
        CancelConfirm();

        HostModel model = (HostModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.Validate())
        {
            return;
        }

        // Save changes.
        VDS host = new VDS();
        host.setvds_name((String) model.getName().getEntity());
        host.sethost_name((String) model.getHost().getEntity());
        host.setManagmentIp((String) model.getManagementIp().getEntity());
        host.setport((Integer) model.getPort().getEntity());
        host.setvds_group_id(((VDSGroup) model.getCluster().getSelectedItem()).getID());
        host.setpm_enabled((Boolean) model.getIsPm().getEntity());
        host.setpm_user((Boolean) model.getIsPm().getEntity() ? (String) model.getPmUserName().getEntity() : null);
        host.setpm_password((Boolean) model.getIsPm().getEntity() ? (String) model.getPmPassword().getEntity() : null);
        host.setpm_type((Boolean) model.getIsPm().getEntity() ? (String) model.getPmType().getSelectedItem() : null);
        host.setPmOptionsMap(new ValueObjectMap(model.getPmOptionsMap(), false));

        AddVdsActionParameters vdsActionParams = new AddVdsActionParameters();
        vdsActionParams.setvds(host);
        vdsActionParams.setVdsId(host.getvds_id());
        vdsActionParams.setRootPassword((String) model.getRootPassword().getEntity());

        model.StartProgress(null);

        Frontend.RunAction(VdcActionType.AddVds, vdsActionParams,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        ClusterGuideModel localModel = (ClusterGuideModel) result.getState();
                        localModel.PostOnAddHost(result.getReturnValue());

                    }
                }, this);
    }

    public void PostOnAddHost(VdcReturnValueBase returnValue)
    {
        HostModel model = (HostModel) getWindow();

        model.StopProgress();

        if (returnValue != null && returnValue.getSucceeded())
        {
            Cancel();
            PostAction();
        }
    }

    private void PostAction()
    {
        ResetData();
        UpdateOptions();
    }

    public void Cancel()
    {
        ResetData();
        setWindow(null);
    }

    public void CancelConfirm()
    {
        setConfirmWindow(null);
    }

    public void CancelConfirmWithFocus()
    {
        setConfirmWindow(null);

        HostModel hostModel = (HostModel) getWindow();
        hostModel.setIsPowerManagementSelected(true);
        hostModel.getIsPm().setEntity(true);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (StringHelper.stringsEqual(command.getName(), "AddHost"))
        {
            AddHost();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnConfirmPMHost"))
        {
            OnConfirmPMHost();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnAddHost"))
        {
            OnAddHost();
        }
        if (StringHelper.stringsEqual(command.getName(), "SelectHost"))
        {
            SelectHost();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnSelectHost"))
        {
            OnSelectHost();
        }
        if (StringHelper.stringsEqual(command.getName(), "Cancel"))
        {
            Cancel();
        }
        if (StringHelper.stringsEqual(command.getName(), "CancelConfirm"))
        {
            CancelConfirm();
        }
        if (StringHelper.stringsEqual(command.getName(), "CancelConfirmWithFocus"))
        {
            CancelConfirmWithFocus();
        }
    }
}
