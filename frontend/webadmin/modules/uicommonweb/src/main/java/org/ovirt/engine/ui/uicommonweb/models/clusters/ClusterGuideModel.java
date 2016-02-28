package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.ManagementNetworkOnClusterOperationParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.hostdeploy.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.hostdeploy.ApproveVdsParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.compat.Version;
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
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.MoveHost;
import org.ovirt.engine.ui.uicommonweb.models.hosts.MoveHostData;
import org.ovirt.engine.ui.uicommonweb.models.hosts.NewHostModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

import com.google.gwt.user.client.Timer;

public class ClusterGuideModel extends GuideModel {

    //Action command names
    private static final String ADD_DATA_CENTER = "AddDataCenter"; //$NON-NLS-1$
    private static final String CANCEL_CONFIRM_WITH_FOCUS = "CancelConfirmWithFocus"; //$NON-NLS-1$
    private static final String CANCEL_CONFIRM = "CancelConfirm"; //$NON-NLS-1$
    private static final String CANCEL = "Cancel"; //$NON-NLS-1$
    private static final String ON_SELECT_HOST = "OnSelectHost"; //$NON-NLS-1$
    private static final String SELECT_HOST = "SelectHost"; //$NON-NLS-1$
    private static final String ON_ADD_HOST = "OnAddHost"; //$NON-NLS-1$
    private static final String ON_CONFIRM_PM_HOST = "OnConfirmPMHost"; //$NON-NLS-1$
    private static final String ADD_HOST = "AddHost"; //$NON-NLS-1$
    private static final String ON_ADD_DATACENTER = "OnAddDataCenter"; //$NON-NLS-1$

    //Pop-up and button titles.
    private final String clusterConfigureHostsActionTitle =
            ConstantsManager.getInstance().getConstants().configureHostClusterGuide();
    private final String clusterAddAnotherHostActionTitle =
            ConstantsManager.getInstance().getConstants().addAnotherHostClusterGuide();
    private final String selectHostsActionTitle =
            ConstantsManager.getInstance().getConstants().selectHostsClusterGuide();
    private final String addDataCenterTitle = ConstantsManager.getInstance().getConstants().addDataCenter();
    private final String noAvailableActions = ConstantsManager.getInstance().getConstants().guidePopupNoActionsLabel();

    @Override
    public Cluster getEntity() {
        return (Cluster) ((super.getEntity() instanceof Cluster) ? super.getEntity() : null);
    }

    @Override
    protected void onEntityChanged() {
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
                        clusterGuideModel.hosts = (ArrayList<VDS>) returnValue;
                        clusterGuideModel.updateOptionsNonLocalFS();
                    }
                }), getEntity().getName());

        AsyncDataProvider.getInstance().getHostList(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object target, Object returnValue) {
                    ClusterGuideModel clusterGuideModel = (ClusterGuideModel) target;
                    clusterGuideModel.allHosts = (ArrayList<VDS>) returnValue;
                    clusterGuideModel.updateOptionsNonLocalFS();
                }
            }
        ));
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
                        clusterGuideModel.localStorageHost = (VDS) returnValue;
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
        UICommand addHostAction = new UICommand(ADD_HOST, this);

        if (hosts.size() > 1) {
            hosts.remove(0);
        }

        if (hosts.isEmpty()) {
            addHostAction.setTitle(clusterConfigureHostsActionTitle);
            getCompulsoryActions().add(addHostAction);
        } else if (isAnyUpHostInCluster()) {
            addHostAction.setTitle(clusterAddAnotherHostActionTitle);
            getOptionalActions().add(addHostAction);
        }

        if (getEntity().getStoragePoolId() == null) {
            addHostAction.getExecuteProhibitionReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .theClusterIsntAttachedToADcClusterGuide());
            addHostAction.setIsExecutionAllowed(false);
            return;
        }

        ArrayList<VDS> availableHosts = new ArrayList<>();
        for (VDS vds : allHosts) {
            if (!getEntity().getId().equals(vds.getClusterId())
                    && (vds.getStatus() == VDSStatus.Maintenance || vds.getStatus() == VDSStatus.PendingApproval)
                    && vds.getSupportedClusterVersionsSet() != null
                    && vds.getSupportedClusterVersionsSet().contains(getEntity().getCompatibilityVersion())) {
                availableHosts.add(vds);
            }
        }
        // Select host action.
        UICommand selectHostAction = new UICommand(SELECT_HOST, this);

        if (availableHosts.size() > 0) {
            if (hosts.isEmpty()) {
                selectHostAction.setTitle(selectHostsActionTitle);
                getCompulsoryActions().add(selectHostAction);
            } else if (isAnyUpHostInCluster()) {
                selectHostAction.setTitle(selectHostsActionTitle);
                getOptionalActions().add(selectHostAction);
            }
        }

        stopProgress();
    }

    private void updateOptionsLocalFS() {

        UICommand addHostAction = new UICommand(ADD_HOST, this);
        addHostAction.setTitle(clusterAddAnotherHostActionTitle);
        UICommand selectHost = new UICommand(SELECT_HOST, this);
        selectHost.setTitle(selectHostsActionTitle);

        if (localStorageHost != null) {
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

    private void updateOptions() {
        getCompulsoryActions().clear();
        getOptionalActions().clear();

        if (getEntity() == null) {
            return;
        }
        startProgress();
        if (getEntity().getStoragePoolId() != null) {
            //Datacenter associated with this cluster.
            AsyncDataProvider.getInstance().getDataCenterById(new AsyncQuery(this, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        ClusterGuideModel model = (ClusterGuideModel) target;
                        model.dataCenter = (StoragePool) returnValue;

                        if (model.dataCenter == null
                                || !model.dataCenter.isLocal()) {
                            model.updateOptionsNonLocalFSData();
                        } else {
                            model.updateOptionsLocalFSData();
                        }
                    }
                }), getEntity().getStoragePoolId());
        } else {
            //No data-center associated with this cluster.
            AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery(this, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        ClusterGuideModel model = (ClusterGuideModel) target;
                        @SuppressWarnings("unchecked")
                        List<StoragePool> dataCenters = (ArrayList<StoragePool>) returnValue;
                        final List<StoragePool> localDataCenters = new ArrayList<>();
                        boolean enableButton = false;
                        for (StoragePool dataCenter: dataCenters) {
                            //Find at least one compatible data-center, so we can show the button.
                            if (model.getEntity().getCompatibilityVersion().compareTo(
                                    dataCenter.getCompatibilityVersion()) >= 0 ) {
                                if (dataCenter.isLocal()) {
                                    //Check if there are any clusters associated with this data-center already.
                                    localDataCenters.add(dataCenter);
                                } else {
                                    enableButton = true;
                                    break;
                                }
                            }
                        }
                        if (enableButton || localDataCenters.isEmpty()) {
                            model.updateOptionsRequiredAddDataCenter(enableButton);
                        } else {
                            model.updateOptionsRequiredAddDataCenter(localDataCenters);
                        }
                    }
                }
            ));
        }
    }

    protected void updateOptionsRequiredAddDataCenter(final List<StoragePool> localDataCenters) {
        AsyncDataProvider.getInstance().getClusterList(new AsyncQuery(this,
            new INewAsyncCallback() {
                @Override
                public void onSuccess(Object target, Object returnValue) {
                    ClusterGuideModel model = (ClusterGuideModel) target;
                    List<StoragePool> localDataCenterWithCluster = new ArrayList<>();
                    @SuppressWarnings("unchecked")
                    List<Cluster> clusters = (List<Cluster>) returnValue;
                    for (StoragePool dataCenter: localDataCenters) {
                        for (Cluster cluster: clusters) {
                            if (cluster.getStoragePoolId() != null &&
                                    cluster.getStoragePoolId().equals(dataCenter.getId())) {
                                localDataCenterWithCluster.add(dataCenter);
                                break;
                            }
                        }
                    }
                    localDataCenters.removeAll(localDataCenterWithCluster);
                    model.updateOptionsRequiredAddDataCenter(!localDataCenters.isEmpty());
                }
            })
        );
    }

    protected void updateOptionsRequiredAddDataCenter(boolean enableButton) {
        if (enableButton) {
            // Add data-center action.
            UICommand addDataCenterAction = new UICommand(ADD_DATA_CENTER, this);
            addDataCenterAction.setTitle(addDataCenterTitle);
            addDataCenterAction.setIsAvailable(enableButton);
            getOptionalActions().add(addDataCenterAction);
        } else {
            setNote(new EntityModel<>(noAvailableActions));
        }
        stopProgress();
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

    public void selectHost() {
        final ArrayList<Cluster> clusters = new ArrayList<>();
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
                        model.getCluster().setSelectedItem(Linq.firstOrNull(clusters));
                        model.getCluster().setIsAvailable(false);

                        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSelectHost", ClusterGuideModel.this); //$NON-NLS-1$
                        model.getCommands().add(tempVar);
                        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", ClusterGuideModel.this); //$NON-NLS-1$
                        model.getCommands().add(tempVar2);

                    }
                }), getEntity().getStoragePoolId());
    }

    public void onSelectHost() {
        MoveHost model = (MoveHost) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (!model.validate()) {
            return;
        }

        model.setSelectedHosts(new ArrayList<MoveHostData>());
        for (EntityModel a : Linq.<EntityModel> cast(model.getItems())) {
            if (a.getIsSelected()) {
                model.getSelectedHosts().add((MoveHostData) a);
            }
        }

        Cluster cluster = model.getCluster().getSelectedItem();

        final List<VdcActionParametersBase> parameterList = new ArrayList<>();
        for (MoveHostData hostData : model.getSelectedHosts()) {
            VDS host = hostData.getEntity();
            // Try to change host's cluster as neccessary.
            if (host.getClusterId() != null && !host.getClusterId().equals(cluster.getId())) {
                parameterList.add(new ChangeVDSClusterParameters(cluster.getId(), host.getId()));
            }
        }
        model.startProgress();
        Frontend.getInstance().runMultipleAction(VdcActionType.ChangeVDSCluster, parameterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        final ClusterGuideModel clusterGuideModel = (ClusterGuideModel) result.getState();
                        List<MoveHostData> hosts = ((MoveHost) clusterGuideModel.getWindow()).getSelectedHosts();
                        List<VdcReturnValueBase> retVals = result.getReturnValue();
                        final List<VdcActionParametersBase> activateVdsParameterList = new ArrayList<>();
                        if (retVals != null && hosts.size() == retVals.size()) {
                            int i = 0;

                            for (MoveHostData selectedHostData : hosts) {
                                VDS selectedHost= selectedHostData.getEntity();
                                if (selectedHost.getStatus() == VDSStatus.PendingApproval && retVals.get(i) != null
                                        && retVals.get(i).getSucceeded()) {
                                    Frontend.getInstance().runAction(VdcActionType.ApproveVds,
                                            new ApproveVdsParameters(selectedHost.getId()));
                                } else if (selectedHostData.getActivateHost()) {
                                    activateVdsParameterList.add(new VdsActionParameters(selectedHostData.getEntity().getId()));
                                }
                                i++;
                            }
                        }

                        if (activateVdsParameterList.isEmpty()) {
                            clusterGuideModel.getWindow().stopProgress();
                            clusterGuideModel.cancel();
                            clusterGuideModel.postAction();
                        } else {
                            final String searchString = getVdsSearchString((MoveHost) clusterGuideModel.getWindow());
                            Timer timer = new Timer() {
                                public void run() {
                                    checkVdsClusterChangeSucceeded(clusterGuideModel, searchString, parameterList, activateVdsParameterList);
                                }
                            };
                            timer.schedule(2000);
                        }

                    }
                },
                this);
    }

    private void addDataCenter() {
        AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                ClusterGuideModel guideModel = (ClusterGuideModel) target;
                @SuppressWarnings("unchecked")
                List<StoragePool> allDataCenters = (List<StoragePool>) returnValue;
                List<EntityModel<StoragePool>> filteredDataCenters = new ArrayList<>();
                List<StoragePool> localDataCenters = new ArrayList<>();
                for (StoragePool dataCenter: allDataCenters) {
                    //Find at least one compatible data-center, so we can show the button.
                    if (guideModel.getEntity().getCompatibilityVersion().compareTo(
                            dataCenter.getCompatibilityVersion()) >= 0) {
                        if (dataCenter.isLocal()) {
                            //Check if there are any clusters associated with this data-center already.
                            localDataCenters.add(dataCenter);
                        } else {
                            filteredDataCenters.add(new EntityModel<>(dataCenter));
                        }
                    }
                }
                if (localDataCenters.isEmpty()) {
                    displayAddDataCenter(filteredDataCenters);
                } else {
                    verifyLocalDataCenterNoCluster(filteredDataCenters, localDataCenters);
                }
            }

        }
    ));

    }

    private void verifyLocalDataCenterNoCluster(final List<EntityModel<StoragePool>> filteredDataCenters,
            final List<StoragePool> localDataCenters) {
        AsyncDataProvider.getInstance().getClusterList(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object target, Object returnValue) {
                    List<StoragePool> localDataCenterWithCluster = new ArrayList<>();
                    @SuppressWarnings("unchecked")
                    List<Cluster> clusters = (List<Cluster>) returnValue;
                    for (StoragePool dataCenter: localDataCenters) {
                        for (Cluster cluster: clusters) {
                            if (cluster.getStoragePoolId() != null &&
                                    cluster.getStoragePoolId().equals(dataCenter.getId())) {
                                localDataCenterWithCluster.add(dataCenter);
                                break;
                            }
                        }
                    }
                    localDataCenters.removeAll(localDataCenterWithCluster);
                    for (StoragePool dataCenter: localDataCenters) {
                        filteredDataCenters.add(new EntityModel<>(dataCenter));
                    }
                    displayAddDataCenter(filteredDataCenters);
                }
            })
        );
    }

    private void displayAddDataCenter(List<EntityModel<StoragePool>> dataCenters) {
        ListModel<EntityModel<StoragePool>> dataCentersModel = new ListModel<>();
        dataCentersModel.setItems(dataCenters);
        dataCentersModel.setTitle(addDataCenterTitle);
        dataCentersModel.setHashName("add_datacenter"); //$NON-NLS-1$
        setWindow(dataCentersModel);
        UICommand tempVar = UICommand.createDefaultOkUiCommand(ON_ADD_DATACENTER, ClusterGuideModel.this);
        dataCentersModel.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand(CANCEL, ClusterGuideModel.this);
        dataCentersModel.getCommands().add(tempVar2);
    }

    public void addHost() {
        HostModel model = new NewHostModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newHostTitle());
        model.setHelpTag(HelpTag.new_host);
        model.setHashName("new_host"); //$NON-NLS-1$
        model.getPort().setEntity(54321);
        model.getOverrideIpTables().setEntity(true);
        model.setSpmPriorityValue(null);

        model.getCluster().setSelectedItem(getEntity());
        model.getCluster().setIsChangeable(false);

        AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery(this,
             new INewAsyncCallback() {
                 @Override
                 public void onSuccess(Object target, Object returnValue) {
                     ClusterGuideModel clusterGuideModel = (ClusterGuideModel) target;
                     HostModel model = (HostModel) clusterGuideModel.getWindow();

                     ArrayList<StoragePool> dataCenters = (ArrayList<StoragePool>) returnValue;
                     model.getDataCenter().setItems(dataCenters);
                     if (getEntity().getStoragePoolId() != null) {
                         model.getDataCenter().setSelectedItem(Linq.firstOrNull(dataCenters,
                               new Linq.IdPredicate<>(clusterGuideModel.getEntity().getStoragePoolId())));
                     }
                     model.getDataCenter().setIsChangeable(false);

                     UICommand tempVar = UICommand.createDefaultOkUiCommand("OnConfirmPMHost", clusterGuideModel); //$NON-NLS-1$
                     model.getCommands().add(tempVar);
                     UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", clusterGuideModel); //$NON-NLS-1$
                     model.getCommands().add(tempVar2);
                 }
             }));

        ListModel<Cluster> clusterModel = model.getCluster();
        if (clusterModel.getSelectedItem() != null) {
            Cluster cluster = clusterModel.getSelectedItem();
            if (Version.v3_6.compareTo(cluster.getCompatibilityVersion()) <= 0) {
                model.getProtocol().setIsAvailable(false);
            } else {
                model.getProtocol().setIsAvailable(true);
            }
            model.getProtocol().setEntity(true);
        }
    }

    public void onConfirmPMHost() {
        HostModel model = (HostModel) getWindow();

        if (!model.validate()) {
            return;
        }

        if (!model.getIsPm().getEntity()
                && ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly) {
            ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle(ConstantsManager.getInstance().getConstants().powerManagementConfigurationTitle());
            confirmModel.setHelpTag(HelpTag.power_management_configuration);
            confirmModel.setHashName("power_management_configuration"); //$NON-NLS-1$
            confirmModel.setMessage(ConstantsManager.getInstance().getConstants().youHavntConfigPmMsg());

            UICommand tempVar = UICommand.createDefaultOkUiCommand("OnAddHost", this); //$NON-NLS-1$
            confirmModel.getCommands().add(tempVar);
            UICommand tempVar2 = UICommand.createCancelUiCommand("CancelConfirmWithFocus", this); //$NON-NLS-1$
            confirmModel.getCommands().add(tempVar2);
        } else {
            onAddHost();
        }
    }

    public void onAddDataCenter() {

        @SuppressWarnings("unchecked")
        ListModel<EntityModel<StoragePool>> dataCentersModel = (ListModel<EntityModel<StoragePool>>)getWindow();
        EntityModel<StoragePool> dataCenter = dataCentersModel.getSelectedItem();

        if (dataCenter != null) {
            Cluster cluster = getEntity();
            cluster.setStoragePoolId(dataCenter.getEntity().getId());
            dataCentersModel.startProgress();

            Frontend.getInstance().runAction(VdcActionType.UpdateCluster, new ManagementNetworkOnClusterOperationParameters(cluster),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {

                        if (result.getReturnValue() != null && result.getReturnValue().getSucceeded()) {
                            //Succeeded, close this window.
                            ClusterGuideModel guideModel = (ClusterGuideModel) result.getState();
                            guideModel.postAction();
                        }
                        //Close popup window.
                        setWindow(null);

                    }
                },
            this);
        } else {
            setWindow(null);
        }
    }

    public void onAddHost() {
        cancelConfirm();

        HostModel model = (HostModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (!model.validate()) {
            return;
        }

        // Save changes.
        VDS host = new VDS();
        host.setVdsName(model.getName().getEntity());
        host.setHostName(model.getHost().getEntity());
        host.setPort(model.getPort().getEntity());
        host.setProtocol(model.getProtocol().getEntity() ? VdsProtocol.STOMP : VdsProtocol.XML);
        host.setSshPort(model.getAuthSshPort().getEntity());
        host.setSshUsername(model.getUserName().getEntity());
        host.setSshKeyFingerprint(model.getFetchSshFingerprint().getEntity());
        host.setClusterId(model.getCluster().getSelectedItem().getId());
        host.setVdsSpmPriority(model.getSpmPriorityValue());

        // Save other PM parameters.
        host.setPmEnabled(model.getIsPm().getEntity());
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
        vdsActionParams.setFenceAgents(model.getFenceAgentListModel().getFenceAgents());
        model.startProgress();

        Frontend.getInstance().runAction(VdcActionType.AddVds, vdsActionParams,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {

                        ClusterGuideModel localModel = (ClusterGuideModel) result.getState();
                        localModel.postOnAddHost(result.getReturnValue());

                    }
                }, this);
    }


    public void postOnAddHost(VdcReturnValueBase returnValue) {
        HostModel model = (HostModel) getWindow();

        model.stopProgress();

        if (returnValue != null && returnValue.getSucceeded()) {
            cancel();
            postAction();
        }
    }

    @Override
    protected void postAction() {
        resetData();
        updateOptions();
    }

    protected void cancel() {
        resetData();
        setWindow(null);
    }

    public void cancelConfirm() {
        setConfirmWindow(null);
    }

    public void cancelConfirmWithFocus() {
        setConfirmWindow(null);

        HostModel hostModel = (HostModel) getWindow();
        hostModel.setIsPowerManagementTabSelected(true);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (ADD_HOST.equals(command.getName())) {
            addHost();
        }
        if (ON_CONFIRM_PM_HOST.equals(command.getName())) {
            onConfirmPMHost();
        }
        if (ON_ADD_HOST.equals(command.getName())) {
            onAddHost();
        }
        if (SELECT_HOST.equals(command.getName())) {
            selectHost();
        }
        if (ON_SELECT_HOST.equals(command.getName())) {
            onSelectHost();
        }
        if (CANCEL.equals(command.getName())) {
            cancel();
        }
        if (CANCEL_CONFIRM.equals(command.getName())) {
            cancelConfirm();
        }
        if (CANCEL_CONFIRM_WITH_FOCUS.equals(command.getName())) {
            cancelConfirmWithFocus();
        }
        if (ADD_DATA_CENTER.equals(command.getName())) {
            addDataCenter();
        }
        if (ON_ADD_DATACENTER.equals(command.getName())) {
            onAddDataCenter();
        }
    }
}
