package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterServiceParameters;
import org.ovirt.engine.core.common.action.gluster.RemoveGlusterServerParameters;
import org.ovirt.engine.core.common.action.hostdeploy.AddVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.ClusterEmulatedMachines;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.DetachGlusterHostsModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostDetailModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.MultipleHostsModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class ClusterGeneralModel extends EntityModel<Cluster> {

    public static final String CPU_VERB_PROPERTY_CHANGE = "cpuVerb";//$NON-NLS-1$

    public static final String CONFIGURED_CPU_VERB_PROPERTY_CHANGE = "configuredCpuVerb";//$NON-NLS-1$

    public static final String ARCHITECTURE_PROPERTY_CHANGE = "architecture";//$NON-NLS-1$

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
    private ArchitectureType architecture;
    private String cpuType;
    private String cpuVerb;
    private String configuredCpuVerb;
    private String dataCenterName;
    private String compatibilityVersion;
    private int memoryOverCommit;
    private MigrateOnErrorOptions resiliencePolicy;
    private boolean cpuThreads;
    private ClusterType clusterType;
    private String emulatedMachine;
    private BiosType biosType;
    private String clusterId;

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

    public boolean isCpuConfigurationOutdated() {
        return !Objects.equals(cpuVerb, configuredCpuVerb);
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
        setArchitecture(cluster.getArchitecture());
        setClusterId(cluster.getId().toString());
        setCpuType(cluster.getCpuName());
        setCpuVerb(cluster.getCpuVerb());
        setConfiguredCpuVerb(cluster.getConfiguredCpuVerb());
        setDataCenterName(cluster.getStoragePoolName());
        setMemoryOverCommit(cluster.getMaxVdsMemoryOverCommit());
        setCpuThreads(cluster.getCountThreadsAsCores());
        setResiliencePolicy(cluster.getMigrateOnError());
        ChipsetType chipsetType = cluster.getBiosType() != null ? cluster.getBiosType().getChipsetType() : null;
        String emulatedMachine = ClusterEmulatedMachines.forChipset(cluster.getEmulatedMachine(), chipsetType);
        setEmulatedMachine(emulatedMachine);
        setBiosType(cluster.getBiosType());
        setCompatibilityVersion(cluster.getCompatibilityVersion().getValue());
        generateClusterType(cluster.supportsGlusterService(), cluster.supportsVirtService());
        AsyncDataProvider.getInstance().getNumberOfVmsInCluster(new AsyncQuery<>(
                (QueryReturnValue returnValue) -> setNumberOfVms((Integer) returnValue.getReturnValue())), cluster.getId());
    }

    private void updateConsoleAddressPartiallyOverridden(Cluster cluster) {

        AsyncQuery<QueryReturnValue> query = new AsyncQuery<>(returnValue -> {
                boolean isConsistent = returnValue.getReturnValue();
                setConsoleAddressPartiallyOverridden(!isConsistent);
            }
        );

        Frontend.getInstance().runQuery(
                QueryType.IsDisplayAddressConsistentInCluster,
                new IdQueryParameters(cluster.getId()),
                query
                );
    }

    private void manageGlusterSwiftServices() {
        if (getWindow() != null || getEntity() == null) {
            return;
        }

        Cluster cluster = getEntity();
        final ManageGlusterSwiftModel glusterSwiftModel = new ManageGlusterSwiftModel();
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

        AsyncDataProvider.getInstance().getGlusterSwiftServerServices(glusterSwiftModel.asyncQuery(returnValue -> {
            List<GlusterSwiftServiceModel> serviceList = getGroupedGlusterSwiftServices(returnValue);
            glusterSwiftModel.getHostServicesList().setItems(serviceList);

            glusterSwiftModel.stopProgress();

            UICommand command = UICommand.createDefaultOkUiCommand("OnManageGlusterSwift", ClusterGeneralModel.this); //$NON-NLS-1$
            glusterSwiftModel.getCommands().add(command);

            command = new UICommand("Cancel", ClusterGeneralModel.this); //$NON-NLS-1$
            command.setTitle(ConstantsManager.getInstance().getConstants().close());
            command.setIsCancel(true);
            glusterSwiftModel.getCommands().add(command);
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
            ArrayList<ActionParametersBase> parametersList = new ArrayList<>();
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

            // Todo: calling the runMultipleAction() with isRunOnlyIfAllValidationPass=false
            // becuase this flag is now supported.
            // should check what is the required behaviour and return to true if required.
            if (!parametersList.isEmpty()) {
                Frontend.getInstance().runMultipleAction(ActionType.ManageGlusterService,
                        parametersList,
                        false,
                        result -> {
                            ManageGlusterSwiftModel innerGlusterSwiftModel = (ManageGlusterSwiftModel) result.getState();
                            innerGlusterSwiftModel.stopProgress();
                            cancel();
                            updateGlusterDetails();
                        },
                        glusterSwiftModel);
            } else {
                glusterSwiftModel.stopProgress();
                glusterSwiftModel.setMessage(ConstantsManager.getInstance()
                        .getConstants()
                        .noActionSelectedManageGlusterSwift());
            }
        } else {
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
                Frontend.getInstance().runAction(ActionType.ManageGlusterService, parameters, result -> {
                    ManageGlusterSwiftModel innerGlusterSwiftModel = (ManageGlusterSwiftModel) result.getState();
                    innerGlusterSwiftModel.stopProgress();
                    if (result.getReturnValue().getSucceeded()) {
                        cancel();
                        updateGlusterDetails();
                    }
                }, glusterSwiftModel);
            } else {
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
        } else if (isStop) {
            action = GlusterSwiftAction.STOP;
        } else if (isRestart) {
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

        AsyncDataProvider.getInstance().getGlusterHostsNewlyAdded(new AsyncQuery<>(hostMap -> {
            if (hostMap == null || hostMap.isEmpty()) {
                hostsModel.setMessage(ConstantsManager.getInstance().getConstants().emptyNewGlusterHosts());
            } else {
                ArrayList<EntityModel<HostDetailModel>> list = new ArrayList<>();
                for (Map.Entry<String, Pair<String, String>> host : hostMap.entrySet()) {
                    String sshPublicKey = host.getValue().getSecond();

                    HostDetailModel hostModel = new HostDetailModel(host.getKey(), sshPublicKey);
                    hostModel.setName(host.getKey());
                    hostModel.setPassword("");//$NON-NLS-1$
                    EntityModel<HostDetailModel> entityModel = new EntityModel<>(hostModel);
                    list.add(entityModel);
                }
                hostsModel.getHosts().setItems(list);
            }
            hostsModel.stopProgress();
        }), getEntity().getId(), true);

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
        ArrayList<ActionParametersBase> parametersList = new ArrayList<>();
        for (Object object : hostsModel.getHosts().getItems()) {
            HostDetailModel hostDetailModel = (HostDetailModel) ((EntityModel) object).getEntity();

            VDS host = new VDS();
            host.setVdsName(hostDetailModel.getName());
            host.setHostName(hostDetailModel.getAddress());
            host.setSshPublicKey(hostDetailModel.getSshPublicKey());
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

        // Todo: calling the runMultipleAction() with isRunOnlyIfAllValidationPass=false
        // becuase this flag is now supported.
        // should check what is the required behaviour and return to true if required.
        Frontend.getInstance().runMultipleAction(ActionType.AddVds,
                parametersList,
                false,
                result -> {
                    hostsModel.stopProgress();
                    boolean isAllValidatePassed = true;
                    for (ActionReturnValue returnValueBase : result.getReturnValue()) {
                        isAllValidatePassed = isAllValidatePassed && returnValueBase.isValid();
                        if (!isAllValidatePassed) {
                            break;
                        }
                    }
                    if (isAllValidatePassed) {
                        updateAlerts();
                        cancel();
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

        AsyncDataProvider.getInstance().getGlusterHostsNewlyAdded(new AsyncQuery<>(hostMap -> {
            if (hostMap == null || hostMap.isEmpty()) {
                hostsModel.setMessage(ConstantsManager.getInstance().getConstants().emptyNewGlusterHosts());
            } else {
                ArrayList<EntityModel<String>> hostList = new ArrayList<>();
                for (String host : hostMap.keySet()) {
                    hostList.add(new EntityModel<>(host));
                }
                hostsModel.getHosts().setItems(hostList);
            }
            hostsModel.stopProgress();
        }), getEntity().getId(), true);
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
        ArrayList<ActionParametersBase> parametersList = new ArrayList<>();
        for (Object model : hostsModel.getHosts().getSelectedItems()) {
            String host = (String) ((EntityModel) model).getEntity();
            parametersList.add(new RemoveGlusterServerParameters(getEntity().getId(), host, force));
        }
        Frontend.getInstance().runMultipleAction(ActionType.RemoveGlusterServer, parametersList);
        cancel();
    }

    public void cancel() {
        setWindow(null);
    }


    private void updateGlusterDetails() {
        AsyncDataProvider.getInstance().getVolumeList(new AsyncQuery<>(volumeList -> {
            int volumesUp = 0;
            int volumesDown = 0;
            for (GlusterVolumeEntity volumeEntity : volumeList) {
                if (volumeEntity.getStatus() == GlusterStatus.UP) {
                    volumesUp++;
                } else {
                    volumesDown++;
                }
            }
            setNoOfVolumesTotal(volumeList.size());
            setNoOfVolumesUp(volumesUp);
            setNoOfVolumesDown(volumesDown);
        }), getEntity().getName());

        getManageGlusterSwiftCommand().setIsExecutionAllowed(getGlusterSwiftStatus() != GlusterServiceStatus.NOT_AVAILABLE);

        AsyncDataProvider.getInstance().getClusterGlusterSwiftService(new AsyncQuery<>(swiftService -> {
            if(swiftService != null) {
                setGlusterSwiftStatus(swiftService.getStatus());
            } else {
                setGlusterSwiftStatus(GlusterServiceStatus.UNKNOWN);
            }
        }), getEntity().getId());
    }

    private void updateAlerts() {
        if (getEntity().supportsGlusterService()) {
            AsyncDataProvider.getInstance().getGlusterHostsNewlyAdded(new AsyncQuery<>(serverMap -> {
                if (!serverMap.isEmpty()) {
                    setHasNewGlusterHostsAlert(true);
                    setHasAnyAlert(true);
                } else {
                    setHasNewGlusterHostsAlert(false);
                    setHasAnyAlert(false);
                }
            }), getEntity().getId(), false);
        } else {
            setHasNewGlusterHostsAlert(false);
            setHasAnyAlert(false);
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getManageGlusterSwiftCommand()) {
            manageGlusterSwiftServices();
        } else if (command == getImportNewGlusterHostsCommand()) {
            fetchAndImportNewGlusterHosts();
        } else if (command == getDetachNewGlusterHostsCommand()) {
            detachNewGlusterHosts();
        } else if ("OnSaveHosts".equals(command.getName())) { //$NON-NLS-1$
            onSaveHosts();
        } else if ("OnDetachGlusterHosts".equals(command.getName())) { //$NON-NLS-1$
            onDetachNewGlusterHosts();
        } else if ("OnManageGlusterSwift".equals(command.getName())) { //$NON-NLS-1$
            onManageGlusterSwiftServices();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArchitectureType getArchitecture() {
        return architecture;
    }

    public void setArchitecture(ArchitectureType value) {
        if (architecture != value) {
            architecture = value;
            onPropertyChanged(ARCHITECTURE_PROPERTY_CHANGE);
        }
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

    public void setCpuVerb(String cpuVerb) {
        if (!Objects.equals(this.cpuVerb, cpuVerb)) {
            this.cpuVerb = cpuVerb;
            onPropertyChanged(CPU_VERB_PROPERTY_CHANGE);
        }
    }

    public String getCpuVerb() {
        return cpuVerb;
    }

    public String getConfiguredCpuVerb() {
        return configuredCpuVerb;
    }

    public void setConfiguredCpuVerb(String cpuVerb) {
        if (!Objects.equals(this.configuredCpuVerb, cpuVerb)) {
            this.configuredCpuVerb = cpuVerb;
            onPropertyChanged(CONFIGURED_CPU_VERB_PROPERTY_CHANGE);
        }
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


    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String value) {
        if (!Objects.equals(clusterId, value)) {
            clusterId = value;
            onPropertyChanged(new PropertyChangedEventArgs("clusterId")); //$NON-NLS-1$
        }
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

    public BiosType getBiosType() {
        return biosType;
    }

    public void setBiosType(BiosType biosType) {
        this.biosType = biosType;
    }
}
