package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VolumeModel extends Model {
    EntityModel name;
    ListModel typeList;
    EntityModel replicaCount;
    EntityModel stripeCount;
    EntityModel tcpTransportType;
    EntityModel rdmaTransportType;
    ListModel dataCenter;
    ListModel cluster;
    ListModel bricks;
    EntityModel gluster_accecssProtocol;
    EntityModel nfs_accecssProtocol;
    EntityModel cifs_accecssProtocol;
    EntityModel allowAccess;

    private UICommand addBricksCommand;

    public UICommand getAddBricksCommand()
    {
        return addBricksCommand;
    }

    private void setAddBricksCommand(UICommand value)
    {
        addBricksCommand = value;
    }
    public ListModel getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(ListModel dataCenter) {
        this.dataCenter = dataCenter;
    }

    public ListModel getCluster() {
        return cluster;
    }

    public void setCluster(ListModel cluster) {
        this.cluster = cluster;
    }

    public VolumeModel() {

        setAddBricksCommand(new UICommand("AddBricks", this)); //$NON-NLS-1$
        getAddBricksCommand().setIsExecutionAllowed(false);

        setDataCenter(new ListModel());
        getDataCenter().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                dataCenter_SelectedItemChanged();
            }
        });
        setCluster(new ListModel());
        setName(new EntityModel());

        getName().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (getName().getEntity() != null && ((String) getName().getEntity()).trim().length() > 0)
                    getAddBricksCommand().setIsExecutionAllowed(true);
                else
                    getAddBricksCommand().setIsExecutionAllowed(false);
            }
        });

        setTypeList(new ListModel());
        ArrayList<GlusterVolumeType> list = new ArrayList<GlusterVolumeType>(Arrays.asList(GlusterVolumeType.values()));
        getTypeList().setItems(list);

        setReplicaCount(new EntityModel());
        getReplicaCount().setEntity(VolumeListModel.REPLICATE_COUNT_DEFAULT);
        getReplicaCount().setIsChangable(false);

        setStripeCount(new EntityModel());
        getStripeCount().setEntity(VolumeListModel.STRIPE_COUNT_DEFAULT);
        getStripeCount().setIsChangable(false);

        setTcpTransportType(new EntityModel());
        getTcpTransportType().setEntity(true);

        setRdmaTransportType(new EntityModel());
        getRdmaTransportType().setEntity(false);

        getTypeList().setSelectedItem(GlusterVolumeType.DISTRIBUTE);
        getReplicaCount().setIsAvailable(false);
        getStripeCount().setIsAvailable(false);

        getTypeList().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (getTypeList().getSelectedItem() == GlusterVolumeType.REPLICATE
                        || getTypeList().getSelectedItem() == GlusterVolumeType.DISTRIBUTED_REPLICATE)
                    getReplicaCount().setIsAvailable(true);
                else
                    getReplicaCount().setIsAvailable(false);

                if (getTypeList().getSelectedItem() == GlusterVolumeType.STRIPE
                        || getTypeList().getSelectedItem() == GlusterVolumeType.DISTRIBUTED_STRIPE)
                    getStripeCount().setIsAvailable(true);
                else
                    getStripeCount().setIsAvailable(false);

                getAddBricksCommand().Execute();
            }
        });

        setBricks(new ListModel());

        setGluster_accecssProtocol(new EntityModel());
        getGluster_accecssProtocol().setEntity(true);
        getGluster_accecssProtocol().setIsChangable(false);

        setNfs_accecssProtocol(new EntityModel());
        getNfs_accecssProtocol().setEntity(true);

        setCifs_accecssProtocol(new EntityModel());
        getCifs_accecssProtocol().setEntity(false);

        setAllowAccess(new EntityModel());
        getAllowAccess().setEntity("*"); //$NON-NLS-1$
    }

    public EntityModel getName() {
        return name;
    }

    public void setName(EntityModel name) {
        this.name = name;
    }

    public ListModel getTypeList() {
        return typeList;
    }

    public void setTypeList(ListModel typeList) {
        this.typeList = typeList;
    }

    public EntityModel getReplicaCount() {
        return replicaCount;
    }

    public Integer getReplicaCountValue() {
        return Integer.parseInt((String) replicaCount.getEntity());
    }

    public void setReplicaCount(EntityModel replicaCount) {
        this.replicaCount = replicaCount;
    }

    public EntityModel getStripeCount() {
        return stripeCount;
    }

    public Integer getStripeCountValue() {
        return Integer.parseInt((String) stripeCount.getEntity());
    }

    public void setStripeCount(EntityModel stripeCount) {
        this.stripeCount = stripeCount;
    }

    public EntityModel getTcpTransportType() {
        return tcpTransportType;
    }

    public void setTcpTransportType(EntityModel tcpTransportType) {
        this.tcpTransportType = tcpTransportType;
    }

    public EntityModel getRdmaTransportType() {
        return rdmaTransportType;
    }

    public void setRdmaTransportType(EntityModel rdmaTransportType) {
        this.rdmaTransportType = rdmaTransportType;
    }

    public ListModel getBricks() {
        return bricks;
    }

    public void setBricks(ListModel bricks) {
        this.bricks = bricks;
    }

    public EntityModel getGluster_accecssProtocol() {
        return gluster_accecssProtocol;
    }

    public void setGluster_accecssProtocol(EntityModel gluster_accecssProtocol) {
        this.gluster_accecssProtocol = gluster_accecssProtocol;
    }

    public EntityModel getNfs_accecssProtocol() {
        return nfs_accecssProtocol;
    }

    public void setNfs_accecssProtocol(EntityModel nfs_accecssProtocol) {
        this.nfs_accecssProtocol = nfs_accecssProtocol;
    }

    public EntityModel getCifs_accecssProtocol() {
        return cifs_accecssProtocol;
    }

    public void setCifs_accecssProtocol(EntityModel cifs_accecssProtocol) {
        this.cifs_accecssProtocol = cifs_accecssProtocol;
    }

    public EntityModel getAllowAccess() {
        return allowAccess;
    }

    public void setAllowAccess(EntityModel allowAccess) {
        this.allowAccess = allowAccess;
    }

    public void addBricks(){
        if (getWindow() != null)
        {
            return;
        }
        VolumeBrickModel volumeBrickModel = new VolumeBrickModel();

        volumeBrickModel.getReplicaCount().setEntity(getReplicaCount().getEntity());
        volumeBrickModel.getReplicaCount().setIsChangable(true);
        volumeBrickModel.getReplicaCount().setIsAvailable(getReplicaCount().getIsAvailable());

        volumeBrickModel.getStripeCount().setEntity(getStripeCount().getEntity());
        volumeBrickModel.getStripeCount().setIsChangable(true);
        volumeBrickModel.getStripeCount().setIsAvailable(getStripeCount().getIsAvailable());

        setWindow(volumeBrickModel);
        volumeBrickModel.setTitle(ConstantsManager.getInstance().getConstants().addBricksVolume());
        volumeBrickModel.setHashName("add_bricks"); //$NON-NLS-1$

        // TODO: fetch the mount points to display
        volumeBrickModel.getBricks().setItems(new ArrayList<EntityModel>());

        UICommand command = new UICommand("Ok", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        volumeBrickModel.getCommands().add(command);

        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsDefault(true);
        volumeBrickModel.getCommands().add(command);

    }

    private void onAddBricks() {
        VolumeBrickModel volumeBrickModel = (VolumeBrickModel) getWindow();
        if (!volumeBrickModel.validateAddBricks((GlusterVolumeType) getTypeList().getSelectedItem()))
        {
            return;
        }

        ArrayList<GlusterBrickEntity> brickList = new ArrayList<GlusterBrickEntity>();
        for (Object object : volumeBrickModel.getBricks().getSelectedItems()) {
            EntityModel entityModel = (EntityModel) object;
            brickList.add((GlusterBrickEntity) entityModel.getEntity());
        }
        ListModel brickListModel = new ListModel();
        brickListModel.setItems(brickList);

        setBricks(brickListModel);
        setWindow(null);
    }

    public boolean Validate() {
        if(getTypeList().getSelectedItem() == GlusterVolumeType.REPLICATE)
        {
            IntegerValidation replicaCountValidation = new IntegerValidation();
            replicaCountValidation.setMinimum(2);
            replicaCountValidation.setMaximum(65535);
            getReplicaCount().ValidateEntity(new IValidation[]{replicaCountValidation});
        }
        return getReplicaCount().getIsValid();
    }

    private void dataCenter_SelectedItemChanged()
    {
        storage_pool dataCenter = (storage_pool) getDataCenter().getSelectedItem();
        if (dataCenter != null)
        {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object result)
                {
                    VolumeModel volumeModel = (VolumeModel) model;
                    java.util.ArrayList<VDSGroup> clusters = (java.util.ArrayList<VDSGroup>) result;
                    VDSGroup oldCluster = (VDSGroup) volumeModel.getCluster().getSelectedItem();
                    storage_pool selectedDataCenter = (storage_pool) getDataCenter().getSelectedItem();

                    // Update selected cluster only if the returned cluster list is indeed the selected datacenter's
                    // clusters
                    if (clusters.isEmpty()
                            || clusters.size() > 0
                            && clusters.get(0)
                                    .getstorage_pool_id()
                                    .getValue()
                                    .equals(selectedDataCenter.getId().getValue()))
                    {
                        volumeModel.getCluster().setItems(clusters);

                        if (oldCluster != null)
                        {
                            VDSGroup newSelectedItem =
                                    Linq.FirstOrDefault(clusters, new Linq.ClusterPredicate(oldCluster.getId()));
                            if (newSelectedItem != null)
                            {
                                volumeModel.getCluster().setSelectedItem(newSelectedItem);
                            }
                        }

                        if (volumeModel.getCluster().getSelectedItem() == null)
                        {
                            volumeModel.getCluster().setSelectedItem(Linq.FirstOrDefault(clusters));
                        }
                    }
                }
            };

            AsyncDataProvider.GetClusterList(_asyncQuery, dataCenter.getId());
        }
    }

    @Override
    public void ExecuteCommand(UICommand command){
        super.ExecuteCommand(command);

        if (command == getAddBricksCommand())
        {
            addBricks();
        } else if (command.getName().equals("Ok")) { //$NON-NLS-1$
            onAddBricks();
        } else if (command.getName().equals("Cancel")) { //$NON-NLS-1$
            setWindow(null);
        }
    }

}
