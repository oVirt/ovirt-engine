package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.ApproveVdsParameters;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.GuideModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.MoveHost;
import org.ovirt.engine.ui.uicommonweb.models.hosts.NewHostModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

@SuppressWarnings("unused")
public class ClusterGuideModel extends GuideModel
{

    public final String ClusterConfigureHostsAction = ConstantsManager.getInstance()
            .getConstants()
            .configureHostClusterGuide();
    public final String ClusterAddAnotherHostAction = ConstantsManager.getInstance()
            .getConstants()
            .addAnotherHostClusterGuide();
    public final String SelectHostsAction = ConstantsManager.getInstance().getConstants().selectHostsClusterGuide();

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
    protected void onEntityChanged()
    {
        super.onEntityChanged();
        updateOptions();
    }

    private ArrayList<VDS> hosts;
    private ArrayList<VDS> allHosts;
    private Boolean isAnyHostUpInCluster;
    private VDS localStorageHost;
    private StoragePool dataCenter;

    private void updateOptionsNonLocalFSData() {
        AsyncDataProvider.getHostListByCluster(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        ClusterGuideModel clusterGuideModel = (ClusterGuideModel) target;
                        ArrayList<VDS> hosts = (ArrayList<VDS>) returnValue;
                        clusterGuideModel.hosts = hosts;
                        clusterGuideModel.updateOptionsNonLocalFS();
                    }
                }), getEntity().getName());

        AsyncDataProvider.getHostList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        ClusterGuideModel clusterGuideModel = (ClusterGuideModel) target;
                        ArrayList<VDS> hosts = (ArrayList<VDS>) returnValue;
                        clusterGuideModel.allHosts = hosts;
                        clusterGuideModel.updateOptionsNonLocalFS();
                    }
                }));
        if (getEntity().supportsGlusterService()) {
            AsyncDataProvider.isAnyHostUpInCluster(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {
                            ClusterGuideModel clusterGuideModel = (ClusterGuideModel) target;
                            isAnyHostUpInCluster = (Boolean) returnValue;
                            clusterGuideModel.updateOptionsNonLocalFS();
                        }
                    }), getEntity().getName());
        }
    }

    private void updateOptionsLocalFSData() {
        AsyncDataProvider.getLocalStorageHost(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        ClusterGuideModel clusterGuideModel = (ClusterGuideModel) target;
                        VDS localStorageHost = (VDS) returnValue;
                        clusterGuideModel.localStorageHost = localStorageHost;
                        clusterGuideModel.updateOptionsLocalFS();
                    }
                }), dataCenter.getname());
    }

    private void updateOptionsNonLocalFS() {
        if (hosts == null || allHosts == null || !isUpHostCheckCompleted()) {
            return;
        }
        if (getEntity() == null) {
            stopProgress();
            setWindow(null);
            return;
        }
        // Add host action.
        UICommand addHostAction = new UICommand("AddHost", this); //$NON-NLS-1$

        if (hosts.size() > 1)
        {
            hosts.remove(0);
        }

        if (hosts.isEmpty())
        {
            addHostAction.setTitle(ClusterConfigureHostsAction);
            getCompulsoryActions().add(addHostAction);
        }
        else if (isAnyUpHostInCluster())
        {
            addHostAction.setTitle(ClusterAddAnotherHostAction);
            getOptionalActions().add(addHostAction);
        }

        if (getEntity().getStoragePoolId() == null)
        {
            addHostAction.getExecuteProhibitionReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .theClusterIsntAttachedToADcClusterGuide());
            addHostAction.setIsExecutionAllowed(false);
            return;
        }

        ArrayList<VDS> availableHosts = new ArrayList<VDS>();
        for (VDS vds : allHosts)
        {
            if (!getEntity().getId().equals(vds.getVdsGroupId())
                    && (vds.getStatus() == VDSStatus.Maintenance || vds.getStatus() == VDSStatus.PendingApproval)
                    && vds.getSupportedClusterVersionsSet().contains(getEntity().getcompatibility_version()))
            {
                availableHosts.add(vds);
            }
        }
        // Select host action.
        UICommand selectHostAction = new UICommand("SelectHost", this); //$NON-NLS-1$

        if (availableHosts.size() > 0)
        {
            if (hosts.isEmpty())
            {
                selectHostAction.setTitle(SelectHostsAction);
                getCompulsoryActions().add(selectHostAction);
            }
            else if (isAnyUpHostInCluster())
            {
                selectHostAction.setTitle(SelectHostsAction);
                getOptionalActions().add(selectHostAction);
            }
        }

        stopProgress();
    }

    private void updateOptionsLocalFS() {

        UICommand addHostAction = new UICommand("AddHost", this); //$NON-NLS-1$
        addHostAction.setTitle(ClusterAddAnotherHostAction);
        UICommand selectHost = new UICommand("SelectHost", this); //$NON-NLS-1$
        selectHost.setTitle(SelectHostsAction);

        if (localStorageHost != null)
        {
            String hasHostReason =
                ConstantsManager.getInstance()
                    .getConstants()
                    .thisClusterBelongsToALocalDcWhichAlreadyContainHostClusterGuide();

            addHostAction.getExecuteProhibitionReasons().add(hasHostReason);
            addHostAction.setIsExecutionAllowed(false);
            selectHost.getExecuteProhibitionReasons().add(hasHostReason);
            selectHost.setIsExecutionAllowed(false);
        }

        getCompulsoryActions().add(addHostAction);
        getOptionalActions().add(selectHost);

        stopProgress();
    }

    private void updateOptions()
    {
        getCompulsoryActions().clear();
        getOptionalActions().clear();

        if (getEntity() != null && getEntity().getStoragePoolId() != null)
        {
            startProgress(null);

            AsyncDataProvider.getDataCenterById(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {
                            ClusterGuideModel model = (ClusterGuideModel) target;
                            model.dataCenter = (StoragePool) returnValue;

                            if (model.dataCenter == null
                                    || model.dataCenter.getstorage_pool_type() != StorageType.LOCALFS)
                            {
                                model.updateOptionsNonLocalFSData();
                            }
                            else
                            {
                                model.updateOptionsLocalFSData();
                            }
                        }
                    }), getEntity().getStoragePoolId());
        }
    }

    private boolean isUpHostCheckCompleted() {
        if (!getEntity().supportsGlusterService()) {
            return true;
        }
        return isAnyHostUpInCluster != null;
    }

    private boolean isAnyUpHostInCluster() {
        if (!getEntity().supportsGlusterService()) {
            return true;
        }
        return isAnyHostUpInCluster;
    }

    private void resetData() {
        hosts = null;
        allHosts = null;
        localStorageHost = null;
        dataCenter = null;
        isAnyHostUpInCluster = null;
    }

    public void selectHost()
    {
        final ArrayList<VDSGroup> clusters = new ArrayList<VDSGroup>();
        clusters.add(getEntity());

        final MoveHost model = new MoveHost();
        model.setTitle(ConstantsManager.getInstance().getConstants().selectHostTitle());
        model.setHashName("select_host"); //$NON-NLS-1$

        // In case of local storage, only one host is allowed in the cluster so we should disable multi selection
        AsyncDataProvider.getDataCenterById(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        dataCenter = (StoragePool) returnValue;

                        boolean isMultiHostDC = dataCenter.getstorage_pool_type() == StorageType.LOCALFS;
                        if (isMultiHostDC)
                            model.setMultiSelection(false);

                        setWindow(model);
                        model.getCluster().setItems(clusters);
                        model.getCluster().setSelectedItem(Linq.firstOrDefault(clusters));
                        model.getCluster().setIsAvailable(false);

                        UICommand tempVar = new UICommand("OnSelectHost", ClusterGuideModel.this); //$NON-NLS-1$
                        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
                        tempVar.setIsDefault(true);
                        model.getCommands().add(tempVar);
                        UICommand tempVar2 = new UICommand("Cancel", ClusterGuideModel.this); //$NON-NLS-1$
                        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                        tempVar2.setIsCancel(true);
                        model.getCommands().add(tempVar2);

                    }
                }), getEntity().getStoragePoolId());
    }

    public void onSelectHost()
    {
        MoveHost model = (MoveHost) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.validate())
        {
            return;
        }

        model.setSelectedHosts(new ArrayList<VDS>());
        for (EntityModel a : Linq.<EntityModel> cast(model.getItems()))
        {
            if (a.getIsSelected())
            {
                model.getSelectedHosts().add((VDS) a.getEntity());
            }
        }

        VDSGroup cluster = (VDSGroup) model.getCluster().getSelectedItem();

        ArrayList<VdcActionParametersBase> paramerterList =
                new ArrayList<VdcActionParametersBase>();
        for (VDS host : model.getSelectedHosts())
        {
            // Try to change host's cluster as neccessary.
            if (host.getVdsGroupId() != null && !host.getVdsGroupId().equals(cluster.getId()))
            {
                paramerterList.add(new ChangeVDSClusterParameters(cluster.getId(), host.getId()));

            }
        }
        model.startProgress(null);
        Frontend.RunMultipleAction(VdcActionType.ChangeVDSCluster, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        ClusterGuideModel clusterGuideModel = (ClusterGuideModel) result.getState();
                        ArrayList<VDS> hosts = ((MoveHost) clusterGuideModel.getWindow()).getSelectedHosts();
                        ArrayList<VdcReturnValueBase> retVals =
                                (ArrayList<VdcReturnValueBase>) result.getReturnValue();
                        if (retVals != null && hosts.size() == retVals.size())
                        {
                            int i = 0;
                            for (VDS selectedHost : hosts)
                            {
                                if (selectedHost.getStatus() == VDSStatus.PendingApproval && retVals.get(i) != null
                                        && retVals.get(i).getSucceeded())
                                {
                                    Frontend.RunAction(VdcActionType.ApproveVds,
                                            new ApproveVdsParameters(selectedHost.getId()));
                                }
                                i++;
                            }
                        }
                        clusterGuideModel.getWindow().stopProgress();
                        clusterGuideModel.cancel();
                        clusterGuideModel.postAction();

                    }
                },
                this);
    }

    public void addHost()
    {
        HostModel model = new NewHostModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newHostTitle());
        model.setHashName("new_host"); //$NON-NLS-1$
        model.getPort().setEntity(54321);
        model.getOverrideIpTables().setEntity(true);
        model.setSpmPriorityValue(null);

        model.getCluster().setSelectedItem(getEntity());
        model.getCluster().setIsChangable(false);

        AsyncDataProvider.getDataCenterList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        ClusterGuideModel clusterGuideModel = (ClusterGuideModel) target;
                        HostModel model = (HostModel) clusterGuideModel.getWindow();

                        ArrayList<StoragePool> dataCenters = (ArrayList<StoragePool>) returnValue;
                        model.getDataCenter().setItems(dataCenters);
                        if (getEntity().getStoragePoolId() != null)
                        {
                            model.getDataCenter().setSelectedItem(Linq.firstOrDefault(dataCenters,
                                    new Linq.DataCenterPredicate(clusterGuideModel.getEntity()
                                            .getStoragePoolId())));
                        }
                        model.getDataCenter().setIsChangable(false);

                        UICommand tempVar = new UICommand("OnConfirmPMHost", clusterGuideModel); //$NON-NLS-1$
                        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
                        tempVar.setIsDefault(true);
                        model.getCommands().add(tempVar);
                        UICommand tempVar2 = new UICommand("Cancel", clusterGuideModel); //$NON-NLS-1$
                        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                        tempVar2.setIsCancel(true);
                        model.getCommands().add(tempVar2);
                    }
                }));
    }

    public void onConfirmPMHost()
    {
        HostModel model = (HostModel) getWindow();

        if (!model.validate())
        {
            return;
        }

        if (!((Boolean) model.getIsPm().getEntity())
                && ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly)
        {
            ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle(ConstantsManager.getInstance().getConstants().powerManagementConfigurationTitle());
            confirmModel.setHashName("power_management_configuration"); //$NON-NLS-1$
            confirmModel.setMessage(ConstantsManager.getInstance().getConstants().youHavntConfigPmMsg());

            UICommand tempVar = new UICommand("OnAddHost", this); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
            tempVar.setIsDefault(true);
            confirmModel.getCommands().add(tempVar);
            UICommand tempVar2 = new UICommand("CancelConfirmWithFocus", this); //$NON-NLS-1$
            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
            tempVar2.setIsCancel(true);
            confirmModel.getCommands().add(tempVar2);
        }
        else
        {
            onAddHost();
        }
    }

    public void onAddHost()
    {
        cancelConfirm();

        HostModel model = (HostModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.validate())
        {
            return;
        }

        // Save changes.
        VDS host = new VDS();
        host.setVdsName((String) model.getName().getEntity());
        host.setHostName((String) model.getHost().getEntity());
        host.setPort((Integer) model.getPort().getEntity());
        host.setVdsGroupId(((VDSGroup) model.getCluster().getSelectedItem()).getId());
        host.setVdsSpmPriority(model.getSpmPriorityValue());

        // Save primary PM parameters.
        host.setManagementIp((String) model.getManagementIp().getEntity());
        host.setPmUser((String) model.getPmUserName().getEntity());
        host.setPmPassword((String) model.getPmPassword().getEntity());
        host.setPmType((String) model.getPmType().getSelectedItem());
        host.setPmOptionsMap(new ValueObjectMap(model.getPmOptionsMap(), false));

        // Save secondary PM parameters.
        host.setPmSecondaryIp((String) model.getPmSecondaryIp().getEntity());
        host.setPmSecondaryUser((String) model.getPmSecondaryUserName().getEntity());
        host.setPmSecondaryPassword((String) model.getPmSecondaryPassword().getEntity());
        host.setPmSecondaryType((String) model.getPmSecondaryType().getSelectedItem());
        host.setPmSecondaryOptionsMap(new ValueObjectMap(model.getPmSecondaryOptionsMap(), false));

        // Save other PM parameters.
        host.setpm_enabled((Boolean) model.getIsPm().getEntity());
        host.setPmSecondaryConcurrent((Boolean) model.getPmSecondaryConcurrent().getEntity());


        AddVdsActionParameters vdsActionParams = new AddVdsActionParameters();
        vdsActionParams.setvds(host);
        vdsActionParams.setVdsId(host.getId());
        vdsActionParams.setRootPassword((String) model.getRootPassword().getEntity());
        vdsActionParams.setRebootAfterInstallation(((VDSGroup) model.getCluster().getSelectedItem()).supportsVirtService());

        model.startProgress(null);

        Frontend.RunAction(VdcActionType.AddVds, vdsActionParams,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {

                        ClusterGuideModel localModel = (ClusterGuideModel) result.getState();
                        localModel.postOnAddHost(result.getReturnValue());

                    }
                }, this);
    }

    public void postOnAddHost(VdcReturnValueBase returnValue)
    {
        HostModel model = (HostModel) getWindow();

        model.stopProgress();

        if (returnValue != null && returnValue.getSucceeded())
        {
            cancel();
            postAction();
        }
    }

    private void postAction()
    {
        resetData();
        updateOptions();
    }

    public void cancel()
    {
        resetData();
        setWindow(null);
    }

    public void cancelConfirm()
    {
        setConfirmWindow(null);
    }

    public void cancelConfirmWithFocus()
    {
        setConfirmWindow(null);

        HostModel hostModel = (HostModel) getWindow();
        hostModel.setIsPowerManagementTabSelected(true);
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (StringHelper.stringsEqual(command.getName(), "AddHost")) //$NON-NLS-1$
        {
            addHost();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnConfirmPMHost")) //$NON-NLS-1$
        {
            onConfirmPMHost();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnAddHost")) //$NON-NLS-1$
        {
            onAddHost();
        }
        if (StringHelper.stringsEqual(command.getName(), "SelectHost")) //$NON-NLS-1$
        {
            selectHost();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnSelectHost")) //$NON-NLS-1$
        {
            onSelectHost();
        }
        if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            cancel();
        }
        if (StringHelper.stringsEqual(command.getName(), "CancelConfirm")) //$NON-NLS-1$
        {
            cancelConfirm();
        }
        if (StringHelper.stringsEqual(command.getName(), "CancelConfirmWithFocus")) //$NON-NLS-1$
        {
            cancelConfirmWithFocus();
        }
    }
}
