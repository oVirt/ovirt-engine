package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.gluster.GlusterServiceParameters;
import org.ovirt.engine.core.common.action.gluster.RemoveGlusterServerParameters;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
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
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
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

public class ClusterGeneralModel extends EntityModel {

    private Integer noOfVolumesTotal;
    private Integer noOfVolumesUp;
    private Integer noOfVolumesDown;

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

    public boolean getHasAnyAlert()
    {
        return hasAnyAlert;
    }

    public void setHasAnyAlert(boolean value)
    {
        if (hasAnyAlert != value)
        {
            hasAnyAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasAnyAlert")); //$NON-NLS-1$
        }
    }

    private boolean hasGlusterHostsAlert;

    public boolean getHasNewGlusterHostsAlert()
    {
        return hasGlusterHostsAlert;
    }

    public void setHasNewGlusterHostsAlert(boolean value)
    {
        if (hasGlusterHostsAlert != value)
        {
            hasGlusterHostsAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasNewGlusterHostsAlert")); //$NON-NLS-1$
        }
    }

    private UICommand importNewGlusterHostsCommand;

    public UICommand getImportNewGlusterHostsCommand()
    {
        return importNewGlusterHostsCommand;
    }

    private void setImportNewGlusterHostsCommand(UICommand value)
    {
        importNewGlusterHostsCommand = value;
    }

    private UICommand detachNewGlusterHostsCommand;

    public UICommand getDetachNewGlusterHostsCommand()
    {
        return detachNewGlusterHostsCommand;
    }

    private void setDetachNewGlusterHostsCommand(UICommand value)
    {
        detachNewGlusterHostsCommand = value;
    }

    private String name;
    private String description;
    private String cpuName;
    private String dataCenterName;
    private String compatibilityVersion;
    private int memoryOverCommit;
    private MigrateOnErrorOptions resiliencePolicy;
    private boolean cpuThreads;
    private ClusterType clusterType;

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

    @Override
    public VDSGroup getEntity()
    {
        return (VDSGroup) ((super.getEntity() instanceof VDSGroup) ? super.getEntity() : null);
    }

    public void setEntity(VDSGroup value)
    {
        super.setEntity(value);
    }

    public ClusterGeneralModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
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
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        if (getEntity() != null)
        {
            updateGlusterDetails();
            updateAlerts();
            updateConsoleAddressPartiallyOverridden(getEntity());
            updateProperties();
        }
    }

    private void updateProperties() {
        VDSGroup vdsGroup = getEntity();

        setName(vdsGroup.getname());
        setDescription(vdsGroup.getdescription());
        setCpuName(vdsGroup.getcpu_name());
        setDataCenterName(vdsGroup.getStoragePoolName());
        setMemoryOverCommit(vdsGroup.getmax_vds_memory_over_commit());
        setCpuThreads(vdsGroup.getCountThreadsAsCores());
        setResiliencePolicy(vdsGroup.getMigrateOnError());
        setCompatibilityVersion(vdsGroup.getcompatibility_version().getValue());
        generateClusterType(vdsGroup.supportsGlusterService(), vdsGroup.supportsVirtService());
    }

    private void updateConsoleAddressPartiallyOverridden(VDSGroup cluster) {

        AsyncQuery query = new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        boolean isConsistent = (Boolean) ((VdcQueryReturnValue) returnValue).getReturnValue();
                        setConsoleAddressPartiallyOverridden(!isConsistent);
                    }
                }
                );

        Frontend.RunQuery(
                VdcQueryType.IsDisplayAddressConsistentInCluster,
                new IdQueryParameters(cluster.getId()),
                query
                );
    }

    private void manageGlusterSwiftServices() {
        if (getWindow() != null || getEntity() == null) {
            return;
        }

        VDSGroup cluster = getEntity();
        ManageGlusterSwiftModel glusterSwiftModel = new ManageGlusterSwiftModel();
        glusterSwiftModel.setTitle(ConstantsManager.getInstance().getConstants().manageGlusterSwiftTitle());
        glusterSwiftModel.setHashName("manage_gluster_swift"); //$NON-NLS-1$
        setWindow(glusterSwiftModel);

        glusterSwiftModel.startProgress(null);
        glusterSwiftModel.getSwiftStatus().setEntity(getGlusterSwiftStatus());
        glusterSwiftModel.getStartSwift().setIsChangable(getGlusterSwiftStatus() == GlusterServiceStatus.STOPPED
                || getGlusterSwiftStatus() == GlusterServiceStatus.MIXED
                || getGlusterSwiftStatus() == GlusterServiceStatus.UNKNOWN);
        glusterSwiftModel.getStopSwift().setIsChangable(getGlusterSwiftStatus() == GlusterServiceStatus.RUNNING
                || getGlusterSwiftStatus() == GlusterServiceStatus.MIXED
                || getGlusterSwiftStatus() == GlusterServiceStatus.UNKNOWN);
        glusterSwiftModel.getRestartSwift().setIsChangable(getGlusterSwiftStatus() == GlusterServiceStatus.RUNNING
                || getGlusterSwiftStatus() == GlusterServiceStatus.STOPPED
                || getGlusterSwiftStatus() == GlusterServiceStatus.MIXED
                || getGlusterSwiftStatus() == GlusterServiceStatus.UNKNOWN);

        AsyncDataProvider.getGlusterSwiftServerServices(new AsyncQuery(glusterSwiftModel, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                ManageGlusterSwiftModel innerGlusterSwiftModel = (ManageGlusterSwiftModel) model;
                List<GlusterSwiftServiceModel> serviceList =
                        getGroupedGlusterSwiftServices((List<GlusterServerService>) returnValue);
                innerGlusterSwiftModel.getHostServicesList().setItems(serviceList);

                innerGlusterSwiftModel.stopProgress();

                UICommand command = new UICommand("OnManageGlusterSwift", ClusterGeneralModel.this); //$NON-NLS-1$
                command.setTitle(ConstantsManager.getInstance().getConstants().ok());
                command.setIsDefault(true);
                innerGlusterSwiftModel.getCommands().add(command);

                command = new UICommand("Cancel", ClusterGeneralModel.this); //$NON-NLS-1$
                command.setTitle(ConstantsManager.getInstance().getConstants().close());
                command.setIsCancel(true);
                innerGlusterSwiftModel.getCommands().add(command);
            }
        }), cluster.getId());
    }

    private List<GlusterSwiftServiceModel> getGroupedGlusterSwiftServices(List<GlusterServerService> serviceList) {
        Map<Guid, GlusterSwiftServiceModel> serverSwiftMap = new HashMap<Guid, GlusterSwiftServiceModel>();
        for (GlusterServerService service : serviceList) {
            GlusterSwiftServiceModel serverSwiftModel = serverSwiftMap.get(service.getServerId());
            if (serverSwiftModel == null) {
                GlusterServerService serverSwift = new GlusterServerService();
                serverSwift.setHostName(service.getHostName());
                serverSwift.setServerId(service.getServerId());
                serverSwift.setServiceType(ServiceType.GLUSTER_SWIFT);
                serverSwift.setStatus(service.getStatus());
                serverSwiftModel = new GlusterSwiftServiceModel(service);
                serverSwiftMap.put(service.getServerId(), serverSwiftModel);
            }
            serverSwiftModel.getInternalServiceList().add(service);
        }
        return new ArrayList<GlusterSwiftServiceModel>(serverSwiftMap.values());
    }

    private void onManageGlusterSwiftServices() {
        if (getWindow() == null) {
            return;
        }

        ManageGlusterSwiftModel glusterSwiftModel = (ManageGlusterSwiftModel) getWindow();
        glusterSwiftModel.startProgress(null);
        if ((Boolean) glusterSwiftModel.getIsManageServerLevel().getEntity()) {
            ArrayList<VdcActionParametersBase> parametersList = new ArrayList<VdcActionParametersBase>();
            for (Object model : glusterSwiftModel.getHostServicesList().getItems()) {
                GlusterSwiftServiceModel swiftServiceModel = (GlusterSwiftServiceModel) model;
                GlusterSwiftAction action =
                        getGlusterSwiftAction(swiftServiceModel.getEntity().getStatus(),
                                (Boolean) swiftServiceModel.getStartSwift().getEntity(),
                                (Boolean) swiftServiceModel.getStopSwift().getEntity(),
                                (Boolean) swiftServiceModel.getRestartSwift().getEntity());
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
                Frontend.RunMultipleAction(VdcActionType.ManageGlusterService,
                        parametersList,
                        true,
                        new IFrontendMultipleActionAsyncCallback() {
                            @Override
                            public void executed(FrontendMultipleActionAsyncResult result) {
                                ManageGlusterSwiftModel innerGlusterSwiftModel = (ManageGlusterSwiftModel) result.getState();
                                innerGlusterSwiftModel.stopProgress();
                                for (VdcReturnValueBase returnValueBase : result.getReturnValue()) {
                                }
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
            GlusterServiceStatus swiftStatus = (GlusterServiceStatus) glusterSwiftModel.getSwiftStatus().getEntity();
            GlusterSwiftAction action =
                    getGlusterSwiftAction(swiftStatus,
                            (Boolean) glusterSwiftModel.getStartSwift().getEntity(),
                            (Boolean) glusterSwiftModel.getStopSwift().getEntity(),
                            (Boolean) glusterSwiftModel.getRestartSwift().getEntity());
            if (action != null) {
                GlusterServiceParameters parameters =
                        new GlusterServiceParameters(getEntity().getId(),
                                null,
                                ServiceType.GLUSTER_SWIFT,
                                action.name().toLowerCase());
                Frontend.RunAction(VdcActionType.ManageGlusterService, parameters, new IFrontendActionAsyncCallback() {
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

    private GlusterSwiftAction getGlusterSwiftAction(GlusterServiceStatus currentStatus,
            boolean isStart,
            boolean isStop,
            boolean isRestart) {
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
        if (getWindow() != null)
        {
            return;
        }

        final MultipleHostsModel hostsModel = new MultipleHostsModel();
        setWindow(hostsModel);
        hostsModel.setTitle(ConstantsManager.getInstance().getConstants().addMultipleHostsTitle());
        hostsModel.setHashName("add_hosts"); //$NON-NLS-1$

        UICommand command = new UICommand("OnSaveHosts", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        hostsModel.getCommands().add(command);
        hostsModel.getHosts().setItems(new ArrayList<EntityModel>());

        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        hostsModel.getCommands().add(command);

        hostsModel.startProgress(null);

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                Map<String, String> hostMap = (Map<String, String>) result;

                if (hostMap == null || hostMap.isEmpty())
                {
                    hostsModel.setMessage(ConstantsManager.getInstance().getConstants().emptyNewGlusterHosts());
                }
                else
                {
                    ArrayList<EntityModel> list = new ArrayList<EntityModel>();
                    for (Map.Entry<String, String> host : hostMap.entrySet())
                    {
                        HostDetailModel hostModel = new HostDetailModel(host.getKey(), host.getValue());
                        hostModel.setName(host.getKey());
                        hostModel.setPassword("");//$NON-NLS-1$
                        EntityModel entityModel = new EntityModel(hostModel);
                        list.add(entityModel);
                    }
                    hostsModel.getHosts().setItems(list);
                }
                hostsModel.stopProgress();
            }
        };
        AsyncDataProvider.getGlusterHostsNewlyAdded(_asyncQuery, getEntity().getId(), true);

    }

    public void onSaveHosts() {
        final MultipleHostsModel hostsModel = (MultipleHostsModel) getWindow();
        if (hostsModel == null)
        {
            return;
        }
        if (!hostsModel.validate())
        {
            return;
        }

        hostsModel.startProgress(null);
        ArrayList<VdcActionParametersBase> parametersList = new ArrayList<VdcActionParametersBase>();
        for (Object object : hostsModel.getHosts().getItems()) {
            HostDetailModel hostDetailModel = (HostDetailModel) ((EntityModel) object).getEntity();

            VDS host = new VDS();
            host.setVdsName(hostDetailModel.getName());
            host.setHostName(hostDetailModel.getAddress());
            host.setSSHKeyFingerprint(hostDetailModel.getFingerprint());
            host.setPort(54321);

            host.setVdsGroupId(getEntity().getId());
            host.setpm_enabled(false);

            AddVdsActionParameters parameters = new AddVdsActionParameters();
            parameters.setVdsId(host.getId());
            parameters.setvds(host);
            parameters.setRootPassword(hostDetailModel.getPassword());
            parameters.setOverrideFirewall(false);
            parameters.setRebootAfterInstallation(getEntity().supportsVirtService());
            parametersList.add(parameters);
        }

        Frontend.RunMultipleAction(VdcActionType.AddVds,
                parametersList,
                true,
                new IFrontendMultipleActionAsyncCallback() {

                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        hostsModel.stopProgress();
                        boolean isAllCanDoPassed = true;
                        for (VdcReturnValueBase returnValueBase : result.getReturnValue())
                        {
                            isAllCanDoPassed = isAllCanDoPassed && returnValueBase.getCanDoAction();
                            if (!isAllCanDoPassed)
                            {
                                break;
                            }
                        }
                        if (isAllCanDoPassed)
                        {
                            updateAlerts();
                            cancel();
                        }
                    }
                }, null);
    }

    public void detachNewGlusterHosts()
    {
        if (getWindow() != null)
        {
            return;
        }

        final DetachGlusterHostsModel hostsModel = new DetachGlusterHostsModel();
        setWindow(hostsModel);
        hostsModel.setTitle(ConstantsManager.getInstance().getConstants().detachGlusterHostsTitle());
        hostsModel.setHashName("detach_gluster_hosts"); //$NON-NLS-1$

        UICommand command = new UICommand("OnDetachGlusterHosts", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        hostsModel.getCommands().add(command);
        hostsModel.getHosts().setItems(new ArrayList<EntityModel>());

        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        hostsModel.getCommands().add(command);

        hostsModel.startProgress(null);

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                Map<String, String> hostMap = (Map<String, String>) result;

                if (hostMap == null || hostMap.isEmpty())
                {
                    hostsModel.setMessage(ConstantsManager.getInstance().getConstants().emptyNewGlusterHosts());
                }
                else
                {
                    ArrayList<EntityModel> hostList = new ArrayList<EntityModel>();
                    for (String host : hostMap.keySet())
                    {
                        hostList.add(new EntityModel(host));
                    }
                    hostsModel.getHosts().setItems(hostList);
                }
                hostsModel.stopProgress();
            }
        };
        AsyncDataProvider.getGlusterHostsNewlyAdded(_asyncQuery, getEntity().getId(), true);
    }

    public void onDetachNewGlusterHosts()
    {
        if (getWindow() == null)
        {
            return;
        }

        final DetachGlusterHostsModel hostsModel = (DetachGlusterHostsModel) getWindow();
        if (!hostsModel.validate())
        {
            return;
        }
        boolean force = (Boolean) hostsModel.getForce().getEntity();
        ArrayList<VdcActionParametersBase> parametersList = new ArrayList<VdcActionParametersBase>();
        for (Object model : hostsModel.getHosts().getSelectedItems()) {
            String host = (String) ((EntityModel) model).getEntity();
            parametersList.add(new RemoveGlusterServerParameters(getEntity().getId(), host, force));
        }
        Frontend.RunMultipleAction(VdcActionType.RemoveGlusterServer, parametersList);
        cancel();
    }

    public void cancel()
    {
        setWindow(null);
    }


    private void updateGlusterDetails()
    {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                ArrayList<GlusterVolumeEntity> volumeList = (ArrayList<GlusterVolumeEntity>) result;
                int volumesUp = 0;
                int volumesDown = 0;
                for (GlusterVolumeEntity volumeEntity : volumeList)
                {
                    if (volumeEntity.getStatus() == GlusterStatus.UP)
                    {
                        volumesUp++;
                    }
                    else
                    {
                        volumesDown++;
                    }
                }
                setNoOfVolumesTotal(volumeList.size());
                setNoOfVolumesUp(volumesUp);
                setNoOfVolumesDown(volumesDown);
            }
        };
        AsyncDataProvider.getVolumeList(_asyncQuery, getEntity().getname());

        getManageGlusterSwiftCommand().setIsExecutionAllowed(getGlusterSwiftStatus() != GlusterServiceStatus.NOT_AVAILABLE);

        AsyncDataProvider.getClusterGlusterSwiftService(new AsyncQuery(this, new INewAsyncCallback() {
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

    private void updateAlerts()
    {
        if (getEntity().supportsGlusterService())
        {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object result)
                {
                    ClusterGeneralModel innerGeneralModel = (ClusterGeneralModel) model;
                    Map<String, String> serverMap = (Map<String, String>) result;
                    if (!serverMap.isEmpty())
                    {
                        innerGeneralModel.setHasNewGlusterHostsAlert(true);
                        innerGeneralModel.setHasAnyAlert(true);
                    }
                    else
                    {
                        setHasNewGlusterHostsAlert(false);
                        setHasAnyAlert(false);
                    }
                }
            };
            AsyncDataProvider.getGlusterHostsNewlyAdded(_asyncQuery, getEntity().getId(), false);
        }
        else
        {
            setHasNewGlusterHostsAlert(false);
            setHasAnyAlert(false);
        }
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getManageGlusterSwiftCommand())
        {
            manageGlusterSwiftServices();
        }
        else if (command == getImportNewGlusterHostsCommand())
        {
            fetchAndImportNewGlusterHosts();
        }
        else if (command == getDetachNewGlusterHostsCommand())
        {
            detachNewGlusterHosts();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSaveHosts")) //$NON-NLS-1$
        {
            onSaveHosts();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnDetachGlusterHosts")) //$NON-NLS-1$
        {
            onDetachNewGlusterHosts();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnManageGlusterSwift")) //$NON-NLS-1$
        {
            onManageGlusterSwiftServices();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
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

    public String getCpuName() {
        return cpuName;
    }

    public void setCpuName(String cpuName) {
        this.cpuName = cpuName;
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
}
