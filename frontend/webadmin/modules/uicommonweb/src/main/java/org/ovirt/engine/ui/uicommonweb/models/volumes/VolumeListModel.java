package org.ovirt.engine.ui.uicommonweb.models.volumes;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
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

    public VolumeListModel() {
        setTitle("Volumes");

        setDefaultSearchString("Volumes:");
        setCreateVolumeCommand(new UICommand("Create Volume", this));
        setRemoveVolumeCommand(new UICommand("Remove", this));

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
        volumeModel.setTitle("Create Volume");
        setWindow(volumeModel);
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
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
                    case Hosts:
                    case Cluster:
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
                        innerVolumeModel.getDataCenter().setInfo("Cannot choose Host's Data Center in tree context");
                        innerVolumeModel.getCluster().setIsChangable(false);
                        innerVolumeModel.getCluster().setInfo("Cannot choose Host's Cluster in tree context");
                        break;
                    case DataCenter:
                        storage_pool selectDataCenter =
                                (storage_pool) volumeListModel.getSystemTreeSelectedItem().getEntity();
                        innerVolumeModel.getDataCenter()
                                .setItems(new java.util.ArrayList<storage_pool>(java.util.Arrays.asList(new storage_pool[] { selectDataCenter })));
                        innerVolumeModel.getDataCenter().setSelectedItem(selectDataCenter);
                        innerVolumeModel.getDataCenter().setIsChangable(false);
                        innerVolumeModel.getDataCenter().setInfo("Cannot choose Host's Data Center in tree context");
                        break;
                    default:
                        break;
                    }
                }

                UICommand command = new UICommand("onCreateVolume", volumeListModel);
                command.setTitle("OK");
                command.setIsDefault(true);
                innerVolumeModel.getCommands().add(command);
                command = new UICommand("Cancel", volumeListModel);
                command.setTitle("Cancel");
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
        super.SyncSearch();
        ArrayList<GlusterVolumeEntity> list = new ArrayList<GlusterVolumeEntity>();
        GlusterVolumeEntity glusterVolume = new GlusterVolumeEntity();
        glusterVolume.setName("Vol1");
        glusterVolume.setId(new Guid());
        list.add(glusterVolume);
        setItems(list);
    }

    @Override
    protected void OnSelectedItemChanged() {
        super.OnSelectedItemChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        getRemoveVolumeCommand().setIsExecutionAllowed(getSelectedItem() != null);
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
        else if (command.getName().equals("Cancel")) {
            cancel();
        } else if (command.getName().equals("onCreateVolume")) {
            onCreateVolume();
        }

    }

    private void onCreateVolume() {
        VolumeModel model = (VolumeModel) getWindow();
        Guid clusterId = ((VDSGroup) model.getCluster().getSelectedItem()).getId();
        GlusterVolumeEntity volume = new GlusterVolumeEntity();
        volume.setName((String) model.getName().getEntity());
        volume.setVolumeType((GlusterVolumeType) model.getTypeList().getSelectedItem());
        volume.setBricks((List<GlusterBrickEntity>) model.getBricks().getItems());
        // CreateGlusterVolumeParameters parameter = new CreateGlusterVolumeParameters(clusterId, volume);
        //
        // Frontend.RunAction(VdcActionType.CreateGlusterVolume, parameter, new IFrontendActionAsyncCallback() {
        //
        // @Override
        // public void Executed(FrontendActionAsyncResult result) {
        // int x =10;
        // x;
        //
        // }
        // });
    }

    @Override
    protected String getListName() {
        return "VolumeListModel";
    }

    private SystemTreeItemModel systemTreeSelectedItem;

    public SystemTreeItemModel getSystemTreeSelectedItem()
    {
        return systemTreeSelectedItem;
    }

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

}
