package org.ovirt.engine.ui.uicommonweb.models.volumes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.gluster.CreateGlusterVolumeParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeActionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeOptionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRebalanceParameters;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeBrickListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeParameterListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.ObservableCollection;

public class VolumeListModel extends ListWithDetailsModel implements ISupportSystemTreeContext {

    public static Integer REPLICATE_COUNT_DEFAULT = 2;
    public static Integer STRIPE_COUNT_DEFAULT = 4;

    private UICommand createVolumeCommand;

    public UICommand getCreateVolumeCommand()
    {
        return createVolumeCommand;
    }

    private void setCreateVolumeCommand(UICommand value)
    {
        createVolumeCommand = value;
    }

    private UICommand removeVolumeCommand;

    public UICommand getRemoveVolumeCommand()
    {
        return removeVolumeCommand;
    }

    private void setRemoveVolumeCommand(UICommand value)
    {
        removeVolumeCommand = value;
    }

    private UICommand startCommand;
    private UICommand stopCommand;
    private UICommand rebalanceCommand;
    private UICommand optimizeForVirtStoreCommand;

    public UICommand getRebalanceCommand() {
        return rebalanceCommand;
    }

    public void setRebalanceCommand(UICommand rebalanceCommand) {
        this.rebalanceCommand = rebalanceCommand;
    }

    public UICommand getStartCommand() {
        return startCommand;
    }

    public void setStartCommand(UICommand startCommand) {
        this.startCommand = startCommand;
    }

    public UICommand getStopCommand() {
        return stopCommand;
    }

    public void setStopCommand(UICommand stopCommand) {
        this.stopCommand = stopCommand;
    }

    public UICommand getOptimizeForVirtStoreCommand() {
        return optimizeForVirtStoreCommand;
    }

    public void setOptimizeForVirtStoreCommand(UICommand optimizeForVirtStoreCommand) {
        this.optimizeForVirtStoreCommand = optimizeForVirtStoreCommand;
    }

    public VolumeListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().volumesTitle());

        setDefaultSearchString("Volumes:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.GLUSTER_VOLUME_OBJ_NAME, SearchObjects.GLUSTER_VOLUME_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.GlusterOnly);

        setCreateVolumeCommand(new UICommand("Create Volume", this)); //$NON-NLS-1$
        setRemoveVolumeCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setStartCommand(new UICommand("Start", this)); //$NON-NLS-1$
        setStopCommand(new UICommand("Stop", this)); //$NON-NLS-1$
        setRebalanceCommand(new UICommand("Rebalance", this)); //$NON-NLS-1$
        setOptimizeForVirtStoreCommand(new UICommand("OptimizeForVirtStore", this)); //$NON-NLS-1$
        getRebalanceCommand().setIsAvailable(false);

        getRemoveVolumeCommand().setIsExecutionAllowed(false);
        getStartCommand().setIsExecutionAllowed(false);
        getStopCommand().setIsExecutionAllowed(false);
        getRebalanceCommand().setIsExecutionAllowed(false);

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    @Override
    protected void initDetailModels() {
        super.initDetailModels();
        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(new VolumeGeneralModel());
        list.add(new VolumeParameterListModel());
        list.add(new VolumeBrickListModel());
        list.add(new PermissionListModel());
        list.add(new VolumeEventListModel());
        setDetailModels(list);
    }

    private void createVolume() {
        if (getWindow() != null) {
            return;
        }

        VolumeModel volumeModel = new VolumeModel();
        volumeModel.setTitle(ConstantsManager.getInstance().getConstants().createVolumeTitle());
        setWindow(volumeModel);
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                VolumeListModel volumeListModel = (VolumeListModel) model;
                VolumeModel innerVolumeModel = (VolumeModel) volumeListModel.getWindow();
                ArrayList<storage_pool> dataCenters = (ArrayList<storage_pool>) result;

                if (volumeListModel.getSystemTreeSelectedItem() != null)
                {
                    switch (volumeListModel.getSystemTreeSelectedItem().getType())
                    {
                    case Volumes:
                    case Cluster:
                    case Cluster_Gluster:
                        VDSGroup cluster = (VDSGroup) volumeListModel.getSystemTreeSelectedItem().getEntity();
                        for (storage_pool dc : dataCenters)
                        {
                            if (dc.getId().equals(cluster.getStoragePoolId()))
                            {
                                innerVolumeModel.getDataCenter()
                                        .setItems(new ArrayList<storage_pool>(Arrays.asList(new storage_pool[] {dc})));
                                innerVolumeModel.getDataCenter().setSelectedItem(dc);
                                break;
                            }
                        }
                        innerVolumeModel.getDataCenter().setIsChangable(false);
                        innerVolumeModel.getDataCenter().setInfo(ConstantsManager.getInstance()
                                .getConstants()
                                .cannotChooseVolumesDataCenterinTreeContect());
                        innerVolumeModel.getCluster().setItems(Arrays.asList(cluster));
                        innerVolumeModel.getCluster().setSelectedItem(cluster);
                        innerVolumeModel.getCluster().setIsChangable(false);
                        innerVolumeModel.getCluster().setInfo(ConstantsManager.getInstance()
                                .getConstants()
                                .cannotChooseVolumesClusterinTreeContect());
                        break;
                    case Clusters:
                    case DataCenter:
                        storage_pool selectDataCenter =
                                (storage_pool) volumeListModel.getSystemTreeSelectedItem().getEntity();
                        innerVolumeModel.getDataCenter()
                                .setItems(new ArrayList<storage_pool>(Arrays.asList(new storage_pool[] { selectDataCenter })));
                        innerVolumeModel.getDataCenter().setSelectedItem(selectDataCenter);
                        innerVolumeModel.getDataCenter().setIsChangable(false);
                        innerVolumeModel.getDataCenter().setInfo(ConstantsManager.getInstance()
                                .getConstants()
                                .cannotChooseVolumesDataCenterinTreeContect());
                        break;
                    default:
                        innerVolumeModel.getDataCenter().setItems(dataCenters);
                        innerVolumeModel.getDataCenter().setSelectedItem(Linq.FirstOrDefault(dataCenters));
                        break;
                    }
                }
                else
                {
                    innerVolumeModel.getDataCenter().setItems(dataCenters);
                    innerVolumeModel.getDataCenter().setSelectedItem(Linq.FirstOrDefault(dataCenters));
                }

                UICommand command = new UICommand("onCreateVolume", volumeListModel); //$NON-NLS-1$
                command.setTitle(ConstantsManager.getInstance().getConstants().ok());
                command.setIsDefault(true);
                innerVolumeModel.getCommands().add(command);
                command = new UICommand("Cancel", volumeListModel);  //$NON-NLS-1$
                command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                command.setIsCancel(true);
                innerVolumeModel.getCommands().add(command);
            }
        };
        AsyncDataProvider.GetDataCenterByClusterServiceList(_asyncQuery, false, true);

    }

    private void removeVolume() {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeVolumesTitle());
        model.setHashName("remove_volume"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().removeVolumesMessage());
        model.setNote(ConstantsManager.getInstance().getConstants().removeVolumesWarning());

        if (getSelectedItems() == null) {
            return;
        }

        ArrayList<String> list = new ArrayList<String>();
        for (GlusterVolumeEntity item : Linq.<GlusterVolumeEntity> Cast(getSelectedItems()))
        {
            list.add(item.getName());
        }
        model.setItems(list);

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void onRemoveVolume() {
        if (getWindow() == null)
        {
            return;
        }

        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (getSelectedItems() == null) {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();

        for (Object item : getSelectedItems())
        {
            GlusterVolumeEntity volume = (GlusterVolumeEntity) item;
            list.add(new GlusterVolumeActionParameters(volume.getId(), false));
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.DeleteGlusterVolume, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.StopProgress();
                        cancel();
                    }
                }, model);
    }

    @Override
    protected void syncSearch() {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.GlusterVolume);
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        if (getSelectedItems() == null || getSelectedItems().size() == 0)
        {
            getRemoveVolumeCommand().setIsExecutionAllowed(false);
            getStopCommand().setIsExecutionAllowed(false);
            getStartCommand().setIsExecutionAllowed(false);
            getRebalanceCommand().setIsExecutionAllowed(false);
            getOptimizeForVirtStoreCommand().setIsExecutionAllowed(false);
            return;
        }

        getRemoveVolumeCommand().setIsExecutionAllowed(true);
        getStopCommand().setIsExecutionAllowed(true);
        getStartCommand().setIsExecutionAllowed(true);
        getRebalanceCommand().setIsExecutionAllowed(true);
        getOptimizeForVirtStoreCommand().setIsExecutionAllowed(true);

        for (GlusterVolumeEntity volume : Linq.<GlusterVolumeEntity> Cast(getSelectedItems()))
        {
            if (volume.getStatus() == GlusterStatus.UP)
            {
                getRemoveVolumeCommand().setIsExecutionAllowed(false);
                getStartCommand().setIsExecutionAllowed(false);
            }
            else if (volume.getStatus() == GlusterStatus.DOWN)
            {
                getStopCommand().setIsExecutionAllowed(false);
                getRebalanceCommand().setIsExecutionAllowed(false);
            }
        }
    }

    private void cancel() {
        setWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (command.equals(getCreateVolumeCommand())) {
            createVolume();
        }
        else if (command.equals(getRemoveVolumeCommand())) {
            removeVolume();
        }
        else if (command.getName().equals("Cancel")) { //$NON-NLS-1$
            cancel();
        } else if (command.getName().equals("onCreateVolume")) { //$NON-NLS-1$
            onCreateVolume();
        } else if (command.equals(getStartCommand())) {
            start();
        } else if (command.equals(getStopCommand())) {
            stop();
        } else if (command.equals(getRebalanceCommand())) {
            rebalance();
        } else if (command.equals(getOptimizeForVirtStoreCommand())) {
            optimizeForVirtStore();
        } else if (command.getName().equals("onStop")) {//$NON-NLS-1$
            onStop();
        } else if (command.getName().equals("OnRemove")) { //$NON-NLS-1$
            onRemoveVolume();
        }

    }

    private void rebalance() {
        if (getSelectedItems() == null) {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            GlusterVolumeEntity volume = (GlusterVolumeEntity) item;
            list.add(new GlusterVolumeRebalanceParameters(volume.getId(), false, false));
        }
        Frontend.RunMultipleAction(VdcActionType.StartRebalanceGlusterVolume, list);
    }

    private void optimizeForVirtStore() {
        if (getSelectedItems() == null || getSelectedItems().size() == 0) {
            return;
        }
        ArrayList<Guid> volumeIds = new ArrayList<Guid>();
        for (Object item : getSelectedItems()) {
            volumeIds.add(((GlusterVolumeEntity) item).getId());
        }
        optimizeVolumesForVirtStore(volumeIds);
    }

    private void optimizeVolumesForVirtStore(final List<Guid> volumeList) {
        AsyncQuery aQuery = new AsyncQuery();
        aQuery.setModel(this);
        aQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, final Object result)
            {
                AsyncQuery aQueryInner = new AsyncQuery();
                aQueryInner.setModel(this);
                aQueryInner.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object modelInner, final Object resultInner)
                    {

                        AsyncQuery aQueryInner1 = new AsyncQuery();
                        aQueryInner1.setModel(this);
                        aQueryInner1.asyncCallback = new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object modelInner1, Object resultInner1)
                            {
                                String optionGroupVirt = (String) result;
                                String optionOwnerUserVirt = (String) resultInner;
                                String optionOwnerGroupVirt = (String) resultInner1;

                                ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
                                for (Guid volumeId : volumeList)
                                {
                                    GlusterVolumeOptionEntity optionGroup = new GlusterVolumeOptionEntity();
                                    optionGroup.setVolumeId(volumeId);
                                    optionGroup.setKey("group"); //$NON-NLS-1$
                                    optionGroup.setValue(optionGroupVirt);
                                    list.add(new GlusterVolumeOptionParameters(optionGroup));

                                    GlusterVolumeOptionEntity optionOwnerUser = new GlusterVolumeOptionEntity();
                                    optionOwnerUser.setVolumeId(volumeId);
                                    optionOwnerUser.setKey("storage.owner-uid"); //$NON-NLS-1$
                                    optionOwnerUser.setValue(optionOwnerUserVirt);
                                    list.add(new GlusterVolumeOptionParameters(optionOwnerUser));

                                    GlusterVolumeOptionEntity optionOwnerGroup = new GlusterVolumeOptionEntity();
                                    optionOwnerGroup.setVolumeId(volumeId);
                                    optionOwnerGroup.setKey("storage.owner-gid"); //$NON-NLS-1$
                                    optionOwnerGroup.setValue(optionOwnerGroupVirt);
                                    list.add(new GlusterVolumeOptionParameters(optionOwnerGroup));
                                }
                                Frontend.RunMultipleAction(VdcActionType.SetGlusterVolumeOption, list);
                            }
                        };

                        AsyncDataProvider.GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.GlusterVolumeOptionOwnerGroupVirtValue,
                                AsyncDataProvider.getDefaultConfigurationVersion()),
                                aQueryInner1);
                    }
                };
                AsyncDataProvider.GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.GlusterVolumeOptionOwnerUserVirtValue,
                        AsyncDataProvider.getDefaultConfigurationVersion()),
                        aQueryInner);
            }
        };
        AsyncDataProvider.GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.GlusterVolumeOptionGroupVirtValue,
                AsyncDataProvider.getDefaultConfigurationVersion()),
                aQuery);
    }

    private void stop() {
        if (getWindow() != null)
        {
            return;
        }
        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().confirmStopVolume());
        model.setHashName("volume_stop"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().stopVolumeMessage());
        model.setNote(ConstantsManager.getInstance().getConstants().stopVolumeWarning());

        if (getSelectedItems() == null) {
            return;
        }

        ArrayList<String> list = new ArrayList<String>();
        for (GlusterVolumeEntity item : Linq.<GlusterVolumeEntity> Cast(getSelectedItems()))
        {
            list.add(item.getName());
        }
        model.setItems(list);

        UICommand tempVar = new UICommand("onStop", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void onStop()
    {
        if (getWindow() == null)
        {
            return;
        }
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (getSelectedItems() == null) {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            GlusterVolumeEntity volume = (GlusterVolumeEntity) item;
            list.add(new GlusterVolumeActionParameters(volume.getId(), false));
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.StopGlusterVolume, list,
                new IFrontendMultipleActionAsyncCallback() {

                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {
                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.StopProgress();
                        cancel();
                    }

                }, model);
    }

    private void start() {
        if (getSelectedItems() == null) {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            GlusterVolumeEntity volume = (GlusterVolumeEntity) item;
            list.add(new GlusterVolumeActionParameters(volume.getId(), false));
        }
        Frontend.RunMultipleAction(VdcActionType.StartGlusterVolume, list);
    }


    private void onCreateVolume() {
        VolumeModel volumeModel = (VolumeModel) getWindow();

        if (!volumeModel.validate())
        {
            return;
        }

        Guid clusterId = ((VDSGroup) volumeModel.getCluster().getSelectedItem()).getId();
        GlusterVolumeEntity volume = new GlusterVolumeEntity();
        volume.setClusterId(clusterId);
        volume.setName((String) volumeModel.getName().getEntity());
        GlusterVolumeType type = (GlusterVolumeType) volumeModel.getTypeList().getSelectedItem();

        if (type == GlusterVolumeType.STRIPE || type == GlusterVolumeType.DISTRIBUTED_STRIPE) {
            volume.setStripeCount(volumeModel.getStripeCountValue());
        } else if (type == GlusterVolumeType.REPLICATE || type == GlusterVolumeType.DISTRIBUTED_REPLICATE) {
            volume.setReplicaCount(volumeModel.getReplicaCountValue());
        }
        volume.setVolumeType(type);

        if ((Boolean) volumeModel.getTcpTransportType().getEntity()) {
            volume.getTransportTypes().add(TransportType.TCP);
        }
        if ((Boolean) volumeModel.getRdmaTransportType().getEntity()) {
            volume.getTransportTypes().add(TransportType.RDMA);
        }

        ArrayList<GlusterBrickEntity> brickList = new ArrayList<GlusterBrickEntity>();

        for (Object model : volumeModel.getBricks().getItems())
        {
            brickList.add((GlusterBrickEntity) ((EntityModel) model).getEntity());
        }

        volume.setBricks(brickList);

        if ((Boolean) volumeModel.getNfs_accecssProtocol().getEntity()) {
            volume.enableNFS();
        }
        else {
            volume.disableNFS();
        }

        if ((Boolean) volumeModel.getCifs_accecssProtocol().getEntity()) {
            volume.enableCifs();
        }
        else {
            volume.disableCifs();
        }

        volume.setAccessControlList((String) volumeModel.getAllowAccess().getEntity());

        volumeModel.StartProgress(null);

        CreateGlusterVolumeParameters parameter = new CreateGlusterVolumeParameters(volume);

        Frontend.RunAction(VdcActionType.CreateGlusterVolume, parameter, new IFrontendActionAsyncCallback() {

            @Override
            public void Executed(FrontendActionAsyncResult result) {
                VolumeListModel localModel = (VolumeListModel) result.getState();
                localModel.postOnCreateVolume(result.getReturnValue());
            }
        }, this);
    }

    public void postOnCreateVolume(VdcReturnValueBase returnValue)
    {
        VolumeModel model = (VolumeModel) getWindow();

        model.StopProgress();

        if (returnValue != null && returnValue.getSucceeded())
        {
            cancel();
            Guid volumeId = (Guid) returnValue.getActionReturnValue();
            if ((Boolean) model.getOptimizeForVirtStore().getEntity()) {
                optimizeVolumesForVirtStore(Arrays.asList(volumeId));
            }
        }
    }

    @Override
    protected String getListName() {
        return "VolumeListModel"; //$NON-NLS-1$
    }

    private SystemTreeItemModel systemTreeSelectedItem;

    @Override
    public SystemTreeItemModel getSystemTreeSelectedItem()
    {
        return systemTreeSelectedItem;
    }

    @Override
    public void setSystemTreeSelectedItem(SystemTreeItemModel value)
    {
        if (systemTreeSelectedItem != value)
        {
            systemTreeSelectedItem = value;
            OnSystemTreeSelectedItemChanged();
        }
    }

    private void OnSystemTreeSelectedItemChanged()
    {
        updateActionAvailability();
    }

    @Override
    public boolean IsSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("volume"); //$NON-NLS-1$
    }
}
