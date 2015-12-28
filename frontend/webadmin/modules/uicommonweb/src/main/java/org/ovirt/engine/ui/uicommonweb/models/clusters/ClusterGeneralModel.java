package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.gluster.GlusterServiceParameters;
import org.ovirt.engine.core.common.action.gluster.RemoveGlusterServerParameters;
import org.ovirt.engine.core.common.action.hostdeploy.AddVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterClusterService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.DetachGlusterHostsModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostDetailModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.MultipleHostsModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class ClusterGeneralModel extends EntityModel<Cluster> {

    private Integer noOfVolumesTotal;
    private Integer noOfVolumesUp;
    private Integer noOfVolumesDown;
    private Integer numberOfVms;

    // set to true, if some hosts in the cluster has the console address overridden and some not
    private Boolean consoleAddressPartiallyOverridden = Boolean.FALSE;

    public String getNoOfVolumesTotal() {
        return Integer.toString(noOfVolumesTotal);
    }

    public void setNoOfVolumesTotal(Integer noOfVolumesTotal) {
        this.noOfVolumesTotal = noOfVolumesTotal;
    }

    public String getNoOfVolumesUp() {
        return Integer.toString(noOfVolumesUp);
    }

    public void setNoOfVolumesUp(Integer noOfVolumesUp) {
        this.noOfVolumesUp = noOfVolumesUp;
    }

    public String getNoOfVolumesDown() {
        return Integer.toString(noOfVolumesDown);
    }

    public void setNoOfVolumesDown(Integer noOfVolumesDown) {
        this.noOfVolumesDown = noOfVolumesDown;
    }

    public String getNumberOfVms() {
        return numberOfVms == null ? "0" : Integer.toString(numberOfVms); //$NON-NLS-1$
    }

    public void setNumberOfVms(Integer numberOfVms) {
        if (!Objects.equals(this.numberOfVms, numberOfVms)) {
            this.numberOfVms = numberOfVms;
            onPropertyChanged(new PropertyChangedEventArgs("numberOfVms")); //$NON-NLS-1$
        }
    }

    private GlusterServiceStatus glusterSwiftStatus;

    public GlusterServiceStatus getGlusterSwiftStatus() {
        return glusterSwiftStatus;
    }

    public void setGlusterSwiftStatus(GlusterServiceStatus glusterSwiftStatus) {
        this.glusterSwiftStatus = glusterSwiftStatus;
    }

    private UICommand manageGlusterSwiftCommand;

    public UICommand getManageGlusterSwiftCommand() {
        return manageGlusterSwiftCommand;
    }

    public void setManageGlusterSwiftCommand(UICommand manageGlusterSwiftCommand) {
        this.manageGlusterSwiftCommand = manageGlusterSwiftCommand;
    }

    private boolean hasAnyAlert;

    public boolean getHasAnyAlert() {
        return hasAnyAlert;
    }

    public void setHasAnyAlert(boolean value) {
        if (hasAnyAlert != value) {
            hasAnyAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasAnyAlert")); //$NON-NLS-1$
        }
    }

    private boolean hasGlusterHostsAlert;

    public boolean getHasNewGlusterHostsAlert() {
        return hasGlusterHostsAlert;
    }

    public void setHasNewGlusterHostsAlert(boolean value) {
        if (hasGlusterHostsAlert != value) {
            hasGlusterHostsAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasNewGlusterHostsAlert")); //$NON-NLS-1$
        }
    }

    private UICommand importNewGlusterHostsCommand;

    public UICommand getImportNewGlusterHostsCommand() {
        return importNewGlusterHostsCommand;
    }

    private void setImportNewGlusterHostsCommand(UICommand value) {
        importNewGlusterHostsCommand = value;
    }

    private UICommand detachNewGlusterHostsCommand;

    public UICommand getDetachNewGlusterHostsCommand() {
        return detachNewGlusterHostsCommand;
    }

    private void setDetachNewGlusterHostsCommand(UICommand value) {
        detachNewGlusterHostsCommand = value;
    }

    private String name;
    private String description;
    private String cpuType;
    private String dataCenterName;
    private String compatibilityVersion;
    private int memoryOverCommit;
    private MigrateOnErrorOptions resiliencePolicy;
    private boolean cpuThreads;
    private ClusterType clusterType;
    private String emulatedMachine;

    public void setConsoleAddressPartiallyOverridden(Boolean consoleAddressPartiallyOverridden) {
        if (isConsoleAddressPartiallyOverridden().booleanValue() !=
            (consoleAddressPartiallyOverridden == null ? false : consoleAddressPartiallyOverridden.booleanValue())) {
            this.consoleAddressPartiallyOverridden = consoleAddressPartiallyOverridden;
            onPropertyChanged(new PropertyChangedEventArgs("consoleAddressPartiallyOverridden")); //$NON-NLS-1$
        }
    }

    public Boolean isConsoleAddressPartiallyOverridden() {
        return consoleAddressPartiallyOverridden == null ? Boolean.FALSE : consoleAddressPartiallyOverridden;
    }

    public ClusterGeneralModel() {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHelpTag(HelpTag.general);
        setHashName("general"); //$NON-NLS-1$

        setNoOfVolumesTotal(0);
        setNoOfVolumesUp(0);
        setNoOfVolumesDown(0);
        setGlusterSwiftStatus(GlusterServiceStatus.UNKNOWN);

        setConsoleAddressPartiallyOverridden(false);
        setManageGlusterSwiftCommand(new UICommand("ManageGlusterSwift", this)); //$NON-NLS-1$
        setImportNewGlusterHostsCommand(new UICommand("ImportGlusterHosts", this)); //$NON-NLS-1$
        setDetachNewGlusterHostsCommand(new UICommand("DetachGlusterHosts", this)); //$NON-NLS-1$

        getManageGlusterSwiftCommand().setIsExecutionAllowed(false);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            updateGlusterDetails();
            updateAlerts();
            updateConsoleAddressPartiallyOverridden(getEntity());
            updateProperties();
        }
    }

    private void updateProperties() {
        Cluster cluster = getEntity();

        setName(cluster.getName());
        setDescription(cluster.getDescription());
        setCpuType(cluster.getCpuName());
        setDataCenterName(cluster.getStoragePoolName());
        setMemoryOverCommit(cluster.getMaxVdsMemoryOverCommit());
        setCpuThreads(cluster.getCountThreadsAsCores());
        setResiliencePolicy(cluster.getMigrateOnError());
        setEmulatedMachine(cluster.getEmulatedMachine());
        setCompatibilityVersion(cluster.getCompatibilityVersion().getValue());
        generateClusterType(cluster.supportsGlusterService(), cluster.supportsVirtService());
        AsyncDataProvider.getInstance().getNumberOfVmsInCluster(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                setNumberOfVms((Integer) ((VdcQueryReturnValue) returnValue).getReturnValue());
            }
        }), cluster.getId());

    }

    private void updateConsoleAddressPartiallyOverridden(Cluster cluster) {

        AsyncQuery query = new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        boolean isConsistent = ((VdcQueryReturnValue) returnValue).getReturnValue();
                        setConsoleAddressPartiallyOverridden(!isConsistent);
                    }
                }
                );

        Frontend.getInstance().runQuery(
                VdcQueryType.IsDisplayAddressConsistentInCluster,
                new IdQueryParameters(cluster.getId()),
                query
                );
    }

    private void manageGlusterSwiftServices() {
        if (getWindow() != null || getEntity() == null) {
            return;
        }

        Cluster cluster = getEntity();
        ManageGlusterSwiftModel glusterSwiftModel = new ManageGlusterSwiftModel();
        glusterSwiftModel.setTitle(ConstantsManager.getInstance().getConstants().manageGlusterSwiftTitle());
        glusterSwiftModel.setHelpTag(HelpTag.manage_gluster_swift);
        glusterSwiftModel.setHashName("manage_gluster_swift"); //$NON-NLS-1$
        setWindow(glusterSwiftModel);

        glusterSwiftModel.startProgress();
        glusterSwiftModel.getSwiftStatus().setEntity(getGlusterSwiftStatus());
        glusterSwiftModel.getStartSwift().setIsChangeable(getGlusterSwiftStatus() == GlusterServiceStatus.STOPPED
                || getGlusterSwiftStatus() == GlusterServiceStatus.MIXED
                || getGlusterSwiftStatus() == GlusterServiceStatus.UNKNOWN);
        glusterSwiftModel.getStopSwift().setIsChangeable(getGlusterSwiftStatus() == GlusterServiceStatus.RUNNING
                || getGlusterSwiftStatus() == GlusterServiceStatus.MIXED
                || getGlusterSwiftStatus() == GlusterServiceStatus.UNKNOWN);
        glusterSwiftModel.getRestartSwift().setIsChangeable(getGlusterSwiftStatus() == GlusterServiceStatus.RUNNING
                || getGlusterSwiftStatus() == GlusterServiceStatus.STOPPED
                || getGlusterSwiftStatus() == GlusterServiceStatus.MIXED
                || getGlusterSwiftStatus() == GlusterServiceStatus.UNKNOWN);

        AsyncDataProvider.getInstance().getGlusterSwiftServerServices(new AsyncQuery(glusterSwiftModel, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                ManageGlusterSwiftModel innerGlusterSwiftModel = (ManageGlusterSwiftModel) model;
                List<GlusterSwiftServiceModel> serviceList =
                        getGroupedGlusterSwiftServices((List<GlusterServerService>) returnValue);
                innerGlusterSwiftModel.getHostServicesList().setItems(serviceList);

                innerGlusterSwiftModel.stopProgress();

                UICommand command = UICommand.createDefaultOkUiCommand("OnManageGlusterSwift", ClusterGeneralModel.this); //$NON-NLS-1$
                innerGlusterSwiftModel.getCommands().add(command);

                command = new UICommand("Cancel", ClusterGeneralModel.this); //$NON-NLS-1$
                command.setTitle(ConstantsManager.getInstance().getConstants().close());
                command.setIsCancel(true);
                innerGlusterSwiftModel.getCommands().add(command);
            }
        }), cluster.getId());
    }

    private List<GlusterSwiftServiceModel> getGroupedGlusterSwiftServices(List<GlusterServerService> serviceList) {
        Map<Guid, GlusterSwiftServiceModel> serverSwiftMap = new HashMap<>();
        for (GlusterServerService service : serviceList) {
            GlusterSwiftServiceModel serverSwiftModel = serverSwiftMap.get(service.getServerId());
            if (serverSwiftModel == null) {
                GlusterServerService serverSwift = new GlusterServerService();
                serverSwift.setHostName(service.getHostName());
                serverSwift.setServerId(service.getServerId());
                serverSwift.setServiceType(ServiceType.GLUSTER_SWIFT);
                serverSwift.setStatus(service.getStatus());
                serverSwiftModel = new GlusterSwiftServiceModel(serverSwift);
                serverSwiftMap.put(service.getServerId(), serverSwiftModel);
            }
            serverSwiftModel.getInternalServiceList().add(service);
        }
        return new ArrayList<>(serverSwiftMap.values());
    }

    private void onManageGlusterSwiftServices() {
        if (getWindow() == null) {
            return;
        }

        ManageGlusterSwiftModel glusterSwiftModel = (ManageGlusterSwiftModel) getWindow();
        glusterSwiftModel.startProgress();
        if (glusterSwiftModel.getIsManageServerLevel().getEntity()) {
            ArrayList<VdcActionParametersBase> parametersList = new ArrayList<>();
            for (Object model : glusterSwiftModel.getHostServicesList().getItems()) {
                GlusterSwiftServiceModel swiftServiceModel = (GlusterSwiftServiceModel) model;
                GlusterSwiftAction action =
                        getGlusterSwiftAction(swiftServiceModel.getStartSwift().getEntity(),
                                              swiftServiceModel.getStopSwift().getEntity(),
                                              swiftServiceModel.getRestartSwift().getEntity());
                if (action != null) {
                    GlusterServiceParameters parameters =
                            new GlusterServiceParameters(getEntity().getId(),
                                    swiftServiceModel.getEntity().getServerId(),
                                    ServiceType.GLUSTER_SWIFT,
                                    action.name().toLowerCase());
                    parametersList.add(parameters);
                }
            }
            if (!parametersList.isEmpty()) {
                Frontend.getInstance().runMultipleAction(VdcActionType.ManageGlusterService,
                        parametersList,
                        true,
                        new IFrontendMultipleActionAsyncCallback() {
                            @Override
                            public void executed(FrontendMultipleActionAsyncResult result) {
                                ManageGlusterSwiftModel innerGlusterSwiftModel = (ManageGlusterSwiftModel) result.getState();
                                innerGlusterSwiftModel.stopProgress();
                                cancel();
                                updateGlusterDetails();
                            }
                        },
                        glusterSwiftModel);
            }
            else {
                glusterSwiftModel.stopProgress();
                glusterSwiftModel.setMessage(ConstantsManager.getInstance()
                        .getConstants()
                        .noActionSelectedManageGlusterSwift());
            }
        }
        else {
            GlusterSwiftAction action =
                    getGlusterSwiftAction(glusterSwiftModel.getStartSwift().getEntity(),
                            glusterSwiftModel.getStopSwift().getEntity(),
                            glusterSwiftModel.getRestartSwift().getEntity());
            if (action != null) {
                GlusterServiceParameters parameters =
                        new GlusterServiceParameters(getEntity().getId(),
                                null,
                                ServiceType.GLUSTER_SWIFT,
                                action.name().toLowerCase());
                Frontend.getInstance().runAction(VdcActionType.ManageGlusterService, parameters, new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        ManageGlusterSwiftModel innerGlusterSwiftModel = (ManageGlusterSwiftModel) result.getState();
                        innerGlusterSwiftModel.stopProgress();
                        if (result.getReturnValue().getSucceeded()) {
                            cancel();
                            updateGlusterDetails();
                        }
                    }
                }, glusterSwiftModel);
            }
            else {
                glusterSwiftModel.stopProgress();
                glusterSwiftModel.setMessage(ConstantsManager.getInstance()
                        .getConstants()
                        .noActionSelectedManageGlusterSwift());
            }
        }
    }

    private GlusterSwiftAction getGlusterSwiftAction(boolean isStart, boolean isStop, boolean isRestart) {
        GlusterSwiftAction action = null;
        if (isStart) {
            action = GlusterSwiftAction.START;
        }
        else if (isStop) {
            action = GlusterSwiftAction.STOP;
        }
        else if (isRestart) {
            action = GlusterSwiftAction.RESTART;
        }
        return action;
    }

    public void fetchAndImportNewGlusterHosts() {
        if (getWindow() != null) {
            return;
        }

        final MultipleHostsModel hostsModel = new MultipleHostsModel();
        setWindow(hostsModel);
        hostsModel.setTitle(ConstantsManager.getInstance().getConstants().addMultipleHostsTitle());
        hostsModel.setHelpTag(HelpTag.add_hosts);
        hostsModel.setHashName("add_hosts"); //$NON-NLS-1$

        UICommand command = UICommand.createOkUiCommand("OnSaveHosts", this); //$NON-NLS-1$
        hostsModel.getCommands().add(command);
        hostsModel.getHosts().setItems(new ArrayList<EntityModel<HostDetailModel>>());

        hostsModel.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$

        hostsModel.startProgress();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                Map<String, String> hostMap = (Map<String, String>) result;

                if (hostMap == null || hostMap.isEmpty()) {
                    hostsModel.setMessage(ConstantsManager.getInstance().getConstants().emptyNewGlusterHosts());
                }
                else {
                    ArrayList<EntityModel<HostDetailModel>> list = new ArrayList<>();
                    for (Map.Entry<String, String> host : hostMap.entrySet()) {
                        HostDetailModel hostModel = new HostDetailModel(host.getKey(), host.getValue());
                        hostModel.setName(host.getKey());
                        hostModel.setPassword("");//$NON-NLS-1$
                        EntityModel<HostDetailModel> entityModel = new EntityModel<>(hostModel);
                        list.add(entityModel);
                    }
                    hostsModel.getHosts().setItems(list);
                }
                hostsModel.stopProgress();
            }
        };
        AsyncDataProvider.getInstance().getGlusterHostsNewlyAdded(_asyncQuery, getEntity().getId(), true);

    }

    public void onSaveHosts() {
        final MultipleHostsModel hostsModel = (MultipleHostsModel) getWindow();
        if (hostsModel == null) {
            return;
        }
        if (!hostsModel.validate()) {
            return;
        }

        hostsModel.startProgress();
        ArrayList<VdcActionParametersBase> parametersList = new ArrayList<>();
        for (Object object : hostsModel.getHosts().getItems()) {
            HostDetailModel hostDetailModel = (HostDetailModel) ((EntityModel) object).getEntity();

            VDS host = new VDS();
            host.setVdsName(hostDetailModel.getName());
            host.setHostName(hostDetailModel.getAddress());
            host.setSshKeyFingerprint(hostDetailModel.getFingerprint());
            host.setPort(54321);
            host.setSshPort(22); // TODO: get from UI, till than using defaults.
            host.setSshUsername("root"); //$NON-NLS-1$

            host.setClusterId(getEntity().getId());
            host.setPmEnabled(false);

            AddVdsActionParameters parameters = new AddVdsActionParameters();
            parameters.setVdsId(host.getId());
            parameters.setvds(host);
            parameters.setPassword(hostDetailModel.getPassword());
            parameters.setOverrideFirewall(hostsModel.isConfigureFirewall());
            parametersList.add(parameters);
        }

        Frontend.getInstance().runMultipleAction(VdcActionType.AddVds,
                parametersList,
                true,
                new IFrontendMultipleActionAsyncCallback() {

                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        hostsModel.stopProgress();
                        boolean isAllValidatePassed = true;
                        for (VdcReturnValueBase returnValueBase : result.getReturnValue()) {
                            isAllValidatePassed = isAllValidatePassed && returnValueBase.isValid();
                            if (!isAllValidatePassed) {
                                break;
                            }
                        }
                        if (isAllValidatePassed) {
                            updateAlerts();
                            cancel();
                        }
                    }
                }, null);
    }

    public void detachNewGlusterHosts() {
        if (getWindow() != null) {
            return;
        }

        final DetachGlusterHostsModel hostsModel = new DetachGlusterHostsModel();
        setWindow(hostsModel);
        hostsModel.setTitle(ConstantsManager.getInstance().getConstants().detachGlusterHostsTitle());
        hostsModel.setHelpTag(HelpTag.detach_gluster_hosts);
        hostsModel.setHashName("detach_gluster_hosts"); //$NON-NLS-1$

        UICommand command = UICommand.createOkUiCommand("OnDetachGlusterHosts", this); //$NON-NLS-1$
        hostsModel.getCommands().add(command);
        hostsModel.getHosts().setItems(new ArrayList<EntityModel<String>>());

        hostsModel.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$

        hostsModel.startProgress();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                Map<String, String> hostMap = (Map<String, String>) result;

                if (hostMap == null || hostMap.isEmpty()) {
                    hostsModel.setMessage(ConstantsManager.getInstance().getConstants().emptyNewGlusterHosts());
                }
                else {
                    ArrayList<EntityModel<String>> hostList = new ArrayList<>();
                    for (String host : hostMap.keySet()) {
                        hostList.add(new EntityModel<>(host));
                    }
                    hostsModel.getHosts().setItems(hostList);
                }
                hostsModel.stopProgress();
            }
        };
        AsyncDataProvider.getInstance().getGlusterHostsNewlyAdded(_asyncQuery, getEntity().getId(), true);
    }

    public void onDetachNewGlusterHosts() {
        if (getWindow() == null) {
            return;
        }

        final DetachGlusterHostsModel hostsModel = (DetachGlusterHostsModel) getWindow();
        if (!hostsModel.validate()) {
            return;
        }
        boolean force = hostsModel.getForce().getEntity();
        ArrayList<VdcActionParametersBase> parametersList = new ArrayList<>();
        for (Object model : hostsModel.getHosts().getSelectedItems()) {
            String host = (String) ((EntityModel) model).getEntity();
            parametersList.add(new RemoveGlusterServerParameters(getEntity().getId(), host, force));
        }
        Frontend.getInstance().runMultipleAction(VdcActionType.RemoveGlusterServer, parametersList);
        cancel();
    }

    public void cancel() {
        setWindow(null);
    }


    private void updateGlusterDetails() {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                ArrayList<GlusterVolumeEntity> volumeList = (ArrayList<GlusterVolumeEntity>) result;
                int volumesUp = 0;
                int volumesDown = 0;
                for (GlusterVolumeEntity volumeEntity : volumeList) {
                    if (volumeEntity.getStatus() == GlusterStatus.UP) {
                        volumesUp++;
                    }
                    else {
                        volumesDown++;
                    }
                }
                setNoOfVolumesTotal(volumeList.size());
                setNoOfVolumesUp(volumesUp);
                setNoOfVolumesDown(volumesDown);
            }
        };
        AsyncDataProvider.getInstance().getVolumeList(_asyncQuery, getEntity().getName());

        getManageGlusterSwiftCommand().setIsExecutionAllowed(getGlusterSwiftStatus() != GlusterServiceStatus.NOT_AVAILABLE);

        AsyncDataProvider.getInstance().getClusterGlusterSwiftService(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                GlusterClusterService swiftService = (GlusterClusterService) returnValue;
                if(swiftService != null) {
                    setGlusterSwiftStatus(swiftService.getStatus());
                }
                else {
                    setGlusterSwiftStatus(GlusterServiceStatus.UNKNOWN);
                }
            }
        }), getEntity().getId());
    }

    private void updateAlerts() {
        if (getEntity().supportsGlusterService()) {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object result) {
                    ClusterGeneralModel innerGeneralModel = (ClusterGeneralModel) model;
                    Map<String, String> serverMap = (Map<String, String>) result;
                    if (!serverMap.isEmpty()) {
                        innerGeneralModel.setHasNewGlusterHostsAlert(true);
                        innerGeneralModel.setHasAnyAlert(true);
                    }
                    else {
                        setHasNewGlusterHostsAlert(false);
                        setHasAnyAlert(false);
                    }
                }
            };
            AsyncDataProvider.getInstance().getGlusterHostsNewlyAdded(_asyncQuery, getEntity().getId(), false);
        }
        else {
            setHasNewGlusterHostsAlert(false);
            setHasAnyAlert(false);
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getManageGlusterSwiftCommand()) {
            manageGlusterSwiftServices();
        }
        else if (command == getImportNewGlusterHostsCommand()) {
            fetchAndImportNewGlusterHosts();
        }
        else if (command == getDetachNewGlusterHostsCommand()) {
            detachNewGlusterHosts();
        }
        else if ("OnSaveHosts".equals(command.getName())) { //$NON-NLS-1$
            onSaveHosts();
        }
        else if ("OnDetachGlusterHosts".equals(command.getName())) { //$NON-NLS-1$
            onDetachNewGlusterHosts();
        }
        else if ("OnManageGlusterSwift".equals(command.getName())) { //$NON-NLS-1$
            onManageGlusterSwiftServices();
        }
        else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCpuType() {
        return cpuType;
    }

    public void setCpuType(String cpuType) {
        this.cpuType = cpuType;
    }

    public String getDataCenterName() {
        return dataCenterName;
    }

    public void setDataCenterName(String dataCenterName) {
        this.dataCenterName = dataCenterName;
    }

    public Integer getMemoryOverCommit() {
        return memoryOverCommit;
    }

    public void setMemoryOverCommit(int memoryOverCommit) {
        this.memoryOverCommit = memoryOverCommit;
    }

    public MigrateOnErrorOptions getResiliencePolicy() {
        return resiliencePolicy;
    }

    public void setResiliencePolicy(MigrateOnErrorOptions resiliencePolicy) {
        this.resiliencePolicy = resiliencePolicy;
    }

    public boolean getCpuThreads() {
        return cpuThreads;
    }

    public void setCpuThreads(boolean cpuThreads) {
        this.cpuThreads = cpuThreads;
    }

    public String getCompatibilityVersion() {
        return compatibilityVersion;
    }

    public void setCompatibilityVersion(String compatibilityVersion) {
        this.compatibilityVersion = compatibilityVersion;
    }

    public ClusterType getClusterType() {
        return clusterType;
    }

    private void generateClusterType(boolean supportGluster, boolean supportVirt) {
        if (supportGluster) {
            if (supportVirt) {
                clusterType = ClusterType.BOTH;
            } else {
                clusterType = ClusterType.GLUSTER;
            }
        } else if (supportVirt) {
            clusterType = ClusterType.VIRT;
        }
    }


    public static enum ClusterType {
        GLUSTER, VIRT, BOTH
    }

    public static enum GlusterSwiftAction {
        START,
        STOP,
        RESTART
    }

    public String getEmulatedMachine() {
        return emulatedMachine;
    }

    public void setEmulatedMachine(String emulatedMachine) {
        this.emulatedMachine = emulatedMachine;
    }
}
