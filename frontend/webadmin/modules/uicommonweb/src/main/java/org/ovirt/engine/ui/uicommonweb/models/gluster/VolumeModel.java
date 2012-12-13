package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
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
        getAddBricksCommand().setTitle(ConstantsManager.getInstance().getConstants().addBricksVolume());

        setDataCenter(new ListModel());
        getDataCenter().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                dataCenter_SelectedItemChanged();
            }
        });
        getDataCenter().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);

        setCluster(new ListModel());
        getCluster().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                clusterSelectedItemChanged();
            }
        });

        setName(new EntityModel());

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
        getTcpTransportType().setIsChangable(false);

        setRdmaTransportType(new EntityModel());
        getRdmaTransportType().setEntity(false);
        getRdmaTransportType().setIsAvailable(false);

        getTypeList().setSelectedItem(GlusterVolumeType.DISTRIBUTE);
        getReplicaCount().setIsAvailable(false);
        getStripeCount().setIsAvailable(false);

        setBricks(new ListModel());

        getTypeList().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (getTypeList().getSelectedItem() == GlusterVolumeType.REPLICATE
                        || getTypeList().getSelectedItem() == GlusterVolumeType.DISTRIBUTED_REPLICATE) {
                    getReplicaCount().setIsAvailable(true);
                }
                else {
                    getReplicaCount().setIsAvailable(false);
                }

                if (getTypeList().getSelectedItem() == GlusterVolumeType.STRIPE
                        || getTypeList().getSelectedItem() == GlusterVolumeType.DISTRIBUTED_STRIPE) {
                    getStripeCount().setIsAvailable(true);
                }
                else {
                    getStripeCount().setIsAvailable(false);
                }

                if (getBricks().getItems() != null && ((List) getBricks().getItems()).size() > 0
                        && !validateBrickCount()
                        && getAddBricksCommand().getIsExecutionAllowed())
                {
                    getAddBricksCommand().Execute();
                }
            }
        });

        setGluster_accecssProtocol(new EntityModel());
        getGluster_accecssProtocol().setEntity(true);
        getGluster_accecssProtocol().setIsChangable(false);

        setNfs_accecssProtocol(new EntityModel());
        getNfs_accecssProtocol().setEntity(true);

        setCifs_accecssProtocol(new EntityModel());
        getCifs_accecssProtocol().setEntity(true);

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
        if (replicaCount.getEntity() instanceof String)
        {
            return Integer.parseInt((String) replicaCount.getEntity());
        }
        else
        {
            return (Integer) replicaCount.getEntity();
        }
    }

    public void setReplicaCount(EntityModel replicaCount) {
        this.replicaCount = replicaCount;
    }

    public EntityModel getStripeCount() {
        return stripeCount;
    }

    public Integer getStripeCountValue() {
        if (stripeCount.getEntity() instanceof String)
        {
            return Integer.parseInt((String) stripeCount.getEntity());
        }
        else
        {
            return (Integer) stripeCount.getEntity();
        }
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
        OnPropertyChanged(new PropertyChangedEventArgs("Bricks")); //$NON-NLS-1$
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
        if (getWindow() != null || getCluster().getSelectedItem() == null)
        {
            return;
        }

        VolumeBrickModel volumeBrickModel = new VolumeBrickModel();

        volumeBrickModel.getVolumeType().setEntity(getTypeList().getSelectedItem());

        volumeBrickModel.getReplicaCount().setEntity(getReplicaCount().getEntity());
        volumeBrickModel.getReplicaCount().setIsChangable(true);
        volumeBrickModel.getReplicaCount().setIsAvailable(getReplicaCount().getIsAvailable());

        volumeBrickModel.getStripeCount().setEntity(getStripeCount().getEntity());
        volumeBrickModel.getStripeCount().setIsChangable(true);
        volumeBrickModel.getStripeCount().setIsAvailable(getStripeCount().getIsAvailable());

        setWindow(volumeBrickModel);
        volumeBrickModel.setTitle(ConstantsManager.getInstance().getConstants().addBricksVolume());
        volumeBrickModel.setHashName("add_bricks"); //$NON-NLS-1$

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(volumeBrickModel);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                VolumeBrickModel volumeBrickModel = (VolumeBrickModel) model;
                ArrayList<VDS> hostList = (ArrayList<VDS>) result;
                Iterator<VDS> iterator = hostList.iterator();
                while (iterator.hasNext())
                {
                    if (iterator.next().getstatus() != VDSStatus.Up)
                    {
                        iterator.remove();
                    }
                }
                volumeBrickModel.getServers().setItems(hostList);
            }
        };
        AsyncDataProvider.GetHostListByCluster(_asyncQuery, ((VDSGroup) getCluster().getSelectedItem()).getname());

        // TODO: fetch the mount points to display
        if (getBricks().getItems() != null)
            volumeBrickModel.getBricks().setItems(getBricks().getItems());
        else
            volumeBrickModel.getBricks().setItems(new ArrayList<EntityModel>());

        UICommand command = new UICommand("OnAddBricks", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        volumeBrickModel.getCommands().add(command);

        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        volumeBrickModel.getCommands().add(command);

    }

    private void onAddBricks() {
        VolumeBrickModel volumeBrickModel = (VolumeBrickModel) getWindow();

        if (!volumeBrickModel.validate())
        {
            return;
        }

        GlusterVolumeType volumeType = (GlusterVolumeType) getTypeList().getSelectedItem();
        if (!volumeBrickModel.validateBrickCount(volumeType, true))
        {
            String validationMsg =
                    volumeBrickModel.getValidationFailedMsg((GlusterVolumeType) getTypeList().getSelectedItem(), true);
            if (validationMsg != null)
            {
                volumeBrickModel.setMessage(validationMsg);
            }
            return;
        }

        if ((volumeType == GlusterVolumeType.REPLICATE || volumeType == GlusterVolumeType.DISTRIBUTED_REPLICATE)
                && !volumeBrickModel.validateReplicateBricks()) {
            ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle(ConstantsManager.getInstance()
                    .getConstants()
                    .addBricksReplicateConfirmationTitle());
            confirmModel.setHashName("add_bricks_confirmation"); //$NON-NLS-1$
            confirmModel.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .addBricksToReplicateVolumeFromSameServerMsg());

            UICommand okCommand = new UICommand("OnAddBricksInternal", this); //$NON-NLS-1$
            okCommand.setTitle(ConstantsManager.getInstance().getConstants().yes());
            okCommand.setIsDefault(true);
            getConfirmWindow().getCommands().add(okCommand);

            UICommand cancelCommand = new UICommand("CancelConfirmation", this); //$NON-NLS-1$
            cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().no());
            cancelCommand.setIsCancel(true);
            getConfirmWindow().getCommands().add(cancelCommand);
        }
        else {
            onAddBricksInternal();
        }
    }

    private void onAddBricksInternal() {
        VolumeBrickModel volumeBrickModel = (VolumeBrickModel) getWindow();

        cancelConfirmation();

        if (!volumeBrickModel.validate())
        {
            return;
        }

        GlusterVolumeType selectedVolumeType = (GlusterVolumeType) getTypeList().getSelectedItem();
        if (selectedVolumeType == GlusterVolumeType.REPLICATE
                || selectedVolumeType == GlusterVolumeType.DISTRIBUTED_REPLICATE)
        {
            getReplicaCount().setEntity(volumeBrickModel.getReplicaCount().getEntity());
        }
        else if (selectedVolumeType == GlusterVolumeType.STRIPE
                || selectedVolumeType == GlusterVolumeType.DISTRIBUTED_STRIPE)
        {
            getStripeCount().setEntity(volumeBrickModel.getStripeCount().getEntity());
        }

        ArrayList<EntityModel> brickList = new ArrayList<EntityModel>();
        for (Object object : volumeBrickModel.getBricks().getItems()) {
            EntityModel entityModel = (EntityModel) object;
            brickList.add(entityModel);
        }
        volumeBrickModel.getBricks().setItems(null);

        ListModel brickListModel = new ListModel();
        brickListModel.setItems(brickList);
        brickListModel.setSelectedItems(brickList);

        setBricks(brickListModel);
        setWindow(null);
    }

    private void cancelConfirmation() {
        setConfirmWindow(null);
    }

    public boolean validateBrickCount()
    {
        return VolumeBrickModel.validateBrickCount((GlusterVolumeType) getTypeList().getSelectedItem(),
                getBricks(),
                getReplicaCountValue(),
                getStripeCountValue(),
                true);
    }

    public boolean validate() {
        if (!validateBrickCount())
        {
            setMessage(VolumeBrickModel.getValidationFailedMsg((GlusterVolumeType) getTypeList().getSelectedItem(),
                    true));
            return false;
        }

        getName().ValidateEntity(new IValidation[] { new NotEmptyValidation() });

        setMessage(null);
        boolean validTransportTypes = true;
        if (((Boolean) getTcpTransportType().getEntity()) == false
                && ((Boolean) getRdmaTransportType().getEntity()) == false)
        {
            validTransportTypes = false;
            setMessage(ConstantsManager.getInstance().getConstants().volumeTransportTypesValidationMsg());
        }

        return getName().getIsValid() && validTransportTypes;
    }

    private void clusterSelectedItemChanged()
    {
        setBricks(new ListModel());

        if (getCluster().getSelectedItem() != null)
        {
            final VDSGroup cluster = (VDSGroup) getCluster().getSelectedItem();
            AsyncDataProvider.GetHostListByCluster(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object returnValue) {
                    // In case the result of previous call is returned after selecting some other cluster
                    if (!((VDSGroup) getCluster().getSelectedItem()).getId().equals(cluster.getId())) {
                        return;
                    }

                    List<VDS> hostList = (List<VDS>) returnValue;
                    for (VDS host : hostList) {
                        if (host.getstatus() == VDSStatus.Up) {
                            getAddBricksCommand().setIsExecutionAllowed(true);
                            setMessage(null);
                            return;
                        }
                    }
                    getAddBricksCommand().setIsExecutionAllowed(false);
                    setMessage(ConstantsManager.getInstance().getConstants().volumeEmptyClusterValidationMsg());
                }
            }), cluster.getname());
        }
        else
        {
            getAddBricksCommand().setIsExecutionAllowed(false);
            setMessage(null);
        }
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
                    ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) result;
                    VDSGroup oldCluster = (VDSGroup) volumeModel.getCluster().getSelectedItem();
                    storage_pool selectedDataCenter = (storage_pool) getDataCenter().getSelectedItem();

                    Iterator<VDSGroup> iterator = clusters.iterator();
                    while(iterator.hasNext())
                    {
                        if (!iterator.next().supportsGlusterService()) {
                            iterator.remove();
                        }
                    }

                    // Update selected cluster only if the returned cluster list is indeed the selected datacenter's
                    // clusters
                    if (clusters.isEmpty()
                            || clusters.size() > 0
                            && clusters.get(0)
                                    .getStoragePoolId()
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
    public void ExecuteCommand(UICommand command) {
        super.ExecuteCommand(command);

        if (command == getAddBricksCommand())
        {
            addBricks();
        } else if (command.getName().equals("OnAddBricks")) { //$NON-NLS-1$
            onAddBricks();
        } else if (command.getName().equals("Cancel")) { //$NON-NLS-1$
            setWindow(null);
        } else if (command.getName().equals("OnAddBricksInternal")) { //$NON-NLS-1$
            onAddBricksInternal();
        } else if (command.getName().equals("CancelConfirmation")) { //$NON-NLS-1$
            cancelConfirmation();
        }
    }

}
