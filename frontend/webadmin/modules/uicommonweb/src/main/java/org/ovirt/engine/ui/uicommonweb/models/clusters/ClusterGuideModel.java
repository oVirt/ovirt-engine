package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.ApproveVdsParameters;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
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
        AsyncDataProvider.getInstance().getHostListByCluster(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        ClusterGuideModel clusterGuideModel = (ClusterGuideModel) target;
                        ArrayList<VDS> hosts = (ArrayList<VDS>) returnValue;
                        clusterGuideModel.hosts = hosts;
                        clusterGuideModel.updateOptionsNonLocalFS();
                    }
                }), getEntity().getName());

        AsyncDataProvider.getInstance().getHostList(new AsyncQuery(this,
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
            AsyncDataProvider.getInstance().isAnyHostUpInCluster(new AsyncQuery(this,
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
        AsyncDataProvider.getInstance().getLocalStorageHost(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        ClusterGuideModel clusterGuideModel = (ClusterGuideModel) target;
                        VDS localStorageHost = (VDS) returnValue;
                        clusterGuideModel.localStorageHost = localStorageHost;
                        clusterGuideModel.updateOptionsLocalFS();
                    }
                }), dataCenter.getName());
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
                    && vds.getSupportedClusterVersionsSet() != null &&
                    vds.getSupportedClusterVersionsSet().contains(getEntity().getcompatibility_version()))
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

            getNote().setIsAvailable(true);
            getNote().setEntity(ConstantsManager.getInstance().getConstants().attachLocalStorageDomainToFullyConfigure());
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

            AsyncDataProvider.getInstance().getDataCenterById(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {
                            ClusterGuideModel model = (ClusterGuideModel) target;
                            model.dataCenter = (StoragePool) returnValue;

                            if (model.dataCenter == null
                                    || !model.dataCenter.isLocal())
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
        model.setHelpTag(HelpTag.select_host);
        model.setHashName("select_host"); //$NON-NLS-1$

        // In case of local storage, only one host is allowed in the cluster so we should disable multi selection
        AsyncDataProvider.getInstance().getDataCenterById(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        dataCenter = (StoragePool) returnValue;

                        boolean isMultiHostDC = dataCenter.isLocal();
                        if (isMultiHostDC) {
                            model.setMultiSelection(false);
                        }

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
        Frontend.getInstance().runMultipleAction(VdcActionType.ChangeVDSCluster, paramerterList,
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
                                    Frontend.getInstance().runAction(VdcActionType.ApproveVds,
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
        model.setHelpTag(HelpTag.new_host);
        model.setHashName("new_host"); //$NON-NLS-1$
        model.getPort().setEntity(54321);
        model.getOverrideIpTables().setEntity(true);
        model.setSpmPriorityValue(null);

        model.getCluster().setSelectedItem(getEntity());
        model.getCluster().setIsChangable(false);

        AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery(this,
                                                                         new INewAsyncCallback() {
                                                                             @Override
                                                                             public void onSuccess(Object target, Object returnValue) {
                                                                                 ClusterGuideModel clusterGuideModel = (ClusterGuideModel) target;
                                                                                 HostModel model = (HostModel) clusterGuideModel.getWindow();

                                                                                 ArrayList<StoragePool> dataCenters = (ArrayList<StoragePool>) returnValue;
                                                                                 model.getDataCenter().setItems(dataCenters);
                                                                                 if (getEntity().getStoragePoolId() != null) {
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

        if (!model.getIsPm().getEntity()
                && ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly)
        {
            ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle(ConstantsManager.getInstance().getConstants().powerManagementConfigurationTitle());
            confirmModel.setHelpTag(HelpTag.power_management_configuration);
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
        host.setVdsName(model.getName().getEntity());
        host.setHostName(model.getHost().getEntity());
        host.setPort(model.getPort().getEntity());
        host.setSshPort(model.getAuthSshPort().getEntity());
        host.setSshUsername(model.getUserName().getEntity());
        host.setSshKeyFingerprint(model.getFetchSshFingerprint().getEntity());
        host.setVdsGroupId(model.getCluster().getSelectedItem().getId());
        host.setVdsSpmPriority(model.getSpmPriorityValue());

        // Save primary PM parameters.
        host.setManagementIp(model.getManagementIp().getEntity());
        host.setPmUser(model.getPmUserName().getEntity());
        host.setPmPassword(model.getPmPassword().getEntity());
        host.setPmType(model.getPmType().getSelectedItem());
        host.setPmOptionsMap(model.getPmOptionsMap());

        // Save secondary PM parameters.
        host.setPmSecondaryIp(model.getPmSecondaryIp().getEntity());
        host.setPmSecondaryUser(model.getPmSecondaryUserName().getEntity());
        host.setPmSecondaryPassword(model.getPmSecondaryPassword().getEntity());
        host.setPmSecondaryType(model.getPmSecondaryType().getSelectedItem());
        host.setPmSecondaryOptionsMap(model.getPmSecondaryOptionsMap());
        // Save other PM parameters.
        host.setpm_enabled(model.getIsPm().getEntity());
        host.setPmSecondaryConcurrent(model.getPmSecondaryConcurrent().getEntity());
        host.setDisablePowerManagementPolicy(model.getDisableAutomaticPowerManagement().getEntity());
        host.setPmKdumpDetection(model.getPmKdumpDetection().getEntity());

        AddVdsActionParameters vdsActionParams = new AddVdsActionParameters();
        vdsActionParams.setvds(host);
        vdsActionParams.setVdsId(host.getId());
        if (model.getUserPassword().getEntity() != null) {
            vdsActionParams.setPassword(model.getUserPassword().getEntity());
        }
        vdsActionParams.setAuthMethod(model.getAuthenticationMethod());
        vdsActionParams.setOverrideFirewall(model.getOverrideIpTables().getEntity());
        vdsActionParams.setRebootAfterInstallation(model.getCluster().getSelectedItem().supportsVirtService());

        model.startProgress(null);

        Frontend.getInstance().runAction(VdcActionType.AddVds, vdsActionParams,
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

        if ("AddHost".equals(command.getName())) //$NON-NLS-1$
        {
            addHost();
        }
        if ("OnConfirmPMHost".equals(command.getName())) //$NON-NLS-1$
        {
            onConfirmPMHost();
        }
        if ("OnAddHost".equals(command.getName())) //$NON-NLS-1$
        {
            onAddHost();
        }
        if ("SelectHost".equals(command.getName())) //$NON-NLS-1$
        {
            selectHost();
        }
        if ("OnSelectHost".equals(command.getName())) //$NON-NLS-1$
        {
            onSelectHost();
        }
        if ("Cancel".equals(command.getName())) //$NON-NLS-1$
        {
            cancel();
        }
        if ("CancelConfirm".equals(command.getName())) //$NON-NLS-1$
        {
            cancelConfirm();
        }
        if ("CancelConfirmWithFocus".equals(command.getName())) //$NON-NLS-1$
        {
            cancelConfirmWithFocus();
        }
    }
}
