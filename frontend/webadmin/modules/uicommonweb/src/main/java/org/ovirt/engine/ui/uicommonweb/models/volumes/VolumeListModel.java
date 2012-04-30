package org.ovirt.engine.ui.uicommonweb.models.volumes;

import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.gluster.CreateGlusterVolumeParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeActionParameters;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Configurator.GlusterModeEnum;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
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
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VolumeListModel extends ListWithDetailsModel implements ISupportSystemTreeContext {
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

    public VolumeListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().volumesTitle());

        setDefaultSearchString("Volumes:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());

        setCreateVolumeCommand(new UICommand("Create Volume", this)); //$NON-NLS-1$
        setRemoveVolumeCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setStartCommand(new UICommand("Start", this)); //$NON-NLS-1$
        setStopCommand(new UICommand("Stop", this)); //$NON-NLS-1$
        setRebalanceCommand(new UICommand("Rebalance", this)); //$NON-NLS-1$

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    @Override
    protected void InitDetailModels() {
        super.InitDetailModels();
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
            public void OnSuccess(Object model, Object result)
            {
                VolumeListModel volumeListModel = (VolumeListModel) model;
                VolumeModel innerVolumeModel = (VolumeModel) volumeListModel.getWindow();
                java.util.ArrayList<storage_pool> dataCenters = (java.util.ArrayList<storage_pool>) result;

                innerVolumeModel.getDataCenter().setItems(dataCenters);
                innerVolumeModel.getDataCenter().setSelectedItem(Linq.FirstOrDefault(dataCenters));

                if (volumeListModel.getSystemTreeSelectedItem() != null)
                {
                    switch (volumeListModel.getSystemTreeSelectedItem().getType())
                    {
                    case Volumes:
                    case Cluster:
                    case Cluster_Gluster:
                        VDSGroup cluster = (VDSGroup) volumeListModel.getSystemTreeSelectedItem().getEntity();
                        for (storage_pool dc : (java.util.ArrayList<storage_pool>) innerVolumeModel.getDataCenter()
                                .getItems())
                        {
                            if (dc.getId().equals(cluster.getstorage_pool_id()))
                            {
                                innerVolumeModel.getDataCenter()
                                        .setItems(new java.util.ArrayList<storage_pool>(java.util.Arrays.asList(new storage_pool[] { dc })));
                                innerVolumeModel.getDataCenter().setSelectedItem(dc);
                                break;
                            }
                        }
                        innerVolumeModel.getDataCenter().setIsChangable(false);
                        innerVolumeModel.getDataCenter().setInfo("Cannot choose Volume's Data Center in tree context"); //$NON-NLS-1$
                        innerVolumeModel.getCluster().setIsChangable(false);
                        innerVolumeModel.getCluster().setInfo("Cannot choose Volume's Cluster in tree context"); //$NON-NLS-1$
                        break;
                    case Clusters:
                    case DataCenter:
                        storage_pool selectDataCenter =
                                (storage_pool) volumeListModel.getSystemTreeSelectedItem().getEntity();
                        innerVolumeModel.getDataCenter()
                                .setItems(new java.util.ArrayList<storage_pool>(java.util.Arrays.asList(new storage_pool[] { selectDataCenter })));
                        innerVolumeModel.getDataCenter().setSelectedItem(selectDataCenter);
                        innerVolumeModel.getDataCenter().setIsChangable(false);
                        innerVolumeModel.getDataCenter().setInfo("Cannot choose Volume's Data Center in tree context"); //$NON-NLS-1$
                        break;
                    default:
                        break;
                    }
                }

                UICommand command = new UICommand("onCreateVolume", volumeListModel); //$NON-NLS-1$
                command.setTitle(ConstantsManager.getInstance().getConstants().ok());
                command.setIsDefault(true);
                innerVolumeModel.getCommands().add(command);
                command = new UICommand("Cancel", volumeListModel);  //$NON-NLS-1$
                command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                command.setIsDefault(true);
                innerVolumeModel.getCommands().add(command);
            }
        };
        AsyncDataProvider.GetDataCenterList(_asyncQuery);

    }

    private void removeVolume() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void SyncSearch() {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.GlusterVolume);
        tempVar.setMaxCount(getSearchPageSize());
        super.SyncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    protected void OnSelectedItemChanged() {
        super.OnSelectedItemChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        GlusterVolumeEntity volume = (GlusterVolumeEntity) getSelectedItem();
        getRemoveVolumeCommand().setIsExecutionAllowed(getSelectedItem() != null);
        getStartCommand().setIsExecutionAllowed(volume != null && volume.getStatus() != GlusterVolumeStatus.UP);
        getStopCommand().setIsExecutionAllowed(getSelectedItem() != null);
        getRebalanceCommand().setIsExecutionAllowed(getSelectedItem() != null);
    }

    private void cancel() {
        setWindow(null);
    }

    @Override
    public void ExecuteCommand(UICommand command) {
        super.ExecuteCommand(command);
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
        }
    }

    private void rebalance() {
        if (getSelectedItem() == null) {
            return;
        }
        GlusterVolumeEntity volume = (GlusterVolumeEntity) getSelectedItem();
        // Frontend.RunAction(VdcActionType.RebalanceGlusterVolumeStart,
        // new GlusterVolumeParameters(clusterId, volume.getName()));

    }

    private void stop() {
        if (getSelectedItem() == null) {
            return;
        }
        GlusterVolumeEntity volume = (GlusterVolumeEntity) getSelectedItem();
        // Frontend.RunAction(VdcActionType.StopGlusterVolume, new GlusterVolumeParameters(clusterId,
        // volume.getName()));

    }

    private void start() {
        if (getSelectedItem() == null) {
            return;
        }
        GlusterVolumeEntity volume = (GlusterVolumeEntity) getSelectedItem();
        Frontend.RunAction(VdcActionType.StartGlusterVolume, new GlusterVolumeActionParameters(volume.getId(),false));
    }

    private void onCreateVolume() {
        VolumeModel model = (VolumeModel) getWindow();
        Guid clusterId = ((VDSGroup) model.getCluster().getSelectedItem()).getId();
        GlusterVolumeEntity volume = new GlusterVolumeEntity();
        volume.setClusterId(clusterId);
        volume.setName((String) model.getName().getEntity());
        GlusterVolumeType type = (GlusterVolumeType) model.getTypeList().getSelectedItem();

        if (type == GlusterVolumeType.STRIPE) {
            volume.setStripeCount(4);
        } else if (type == GlusterVolumeType.REPLICATE) {
            volume.setReplicaCount(2);
        }
        volume.setVolumeType(type);

        if ((Boolean) model.getTcpTransportType().getEntity())
            volume.getTransportTypes().add(TransportType.TCP);
        if ((Boolean) model.getRdmaTransportType().getEntity())
            volume.getTransportTypes().add(TransportType.RDMA);

        volume.setBricks((List<GlusterBrickEntity>) model.getBricks().getItems());

        if ((Boolean) model.getNfs_accecssProtocol().getEntity())
            volume.enableNFS();
        else
            volume.disableNFS();

        if ((Boolean) model.getCifs_accecssProtocol().getEntity())
            volume.enableCifs();
        else
            volume.disableCifs();

        volume.setAccessControlList((String) model.getAllowAccess().getEntity());

        CreateGlusterVolumeParameters parameter = new CreateGlusterVolumeParameters(volume);

        Frontend.RunAction(VdcActionType.CreateGlusterVolume, parameter, new IFrontendActionAsyncCallback() {

            @Override
            public void Executed(FrontendActionAsyncResult result) {

            }
        });
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

    @Override
    public void setIsAvailable(boolean value) {
        super.setIsAvailable(getGlusterModeEnum() != GlusterModeEnum.ONLY_OVIRT && value);
    }
}
