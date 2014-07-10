package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class VolumeModel extends Model {
    EntityModel<String> name;
    ListModel<GlusterVolumeType> typeList;
    EntityModel<Integer> replicaCount;
    EntityModel<Integer> stripeCount;
    EntityModel<Boolean> tcpTransportType;
    EntityModel<Boolean> rdmaTransportType;
    ListModel<StoragePool> dataCenter;
    ListModel<VDSGroup> cluster;
    ListModel<EntityModel<GlusterBrickEntity>> bricks;
    EntityModel<Boolean> gluster_accecssProtocol;
    EntityModel<Boolean> nfs_accecssProtocol;
    EntityModel<Boolean> cifs_accecssProtocol;
    EntityModel<String> allowAccess;
    EntityModel<Boolean> optimizeForVirtStore;

    private boolean forceAddBricks;

    private UICommand addBricksCommand;

    public UICommand getAddBricksCommand()
    {
        return addBricksCommand;
    }

    private void setAddBricksCommand(UICommand value)
    {
        addBricksCommand = value;
    }
    public ListModel<StoragePool> getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(ListModel<StoragePool> dataCenter) {
        this.dataCenter = dataCenter;
    }

    public ListModel<VDSGroup> getCluster() {
        return cluster;
    }

    public void setCluster(ListModel<VDSGroup> cluster) {
        this.cluster = cluster;
    }

    public VolumeModel() {

        setAddBricksCommand(new UICommand("AddBricks", this)); //$NON-NLS-1$
        getAddBricksCommand().setIsExecutionAllowed(false);
        getAddBricksCommand().setTitle(ConstantsManager.getInstance().getConstants().addBricksVolume());

        setDataCenter(new ListModel<StoragePool>());
        getDataCenter().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                dataCenter_SelectedItemChanged();
            }
        });
        getDataCenter().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);

        setCluster(new ListModel<VDSGroup>());
        getCluster().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                clusterSelectedItemChanged();
            }
        });

        setName(new EntityModel<String>());

        setTypeList(new ListModel<GlusterVolumeType>());
        ArrayList<GlusterVolumeType> list = new ArrayList<GlusterVolumeType>(Arrays.asList(GlusterVolumeType.values()));
        getTypeList().setItems(list);

        setReplicaCount(new EntityModel<Integer>());
        getReplicaCount().setEntity(VolumeListModel.REPLICATE_COUNT_DEFAULT);
        getReplicaCount().setIsChangable(false);

        setStripeCount(new EntityModel<Integer>());
        getStripeCount().setEntity(VolumeListModel.STRIPE_COUNT_DEFAULT);
        getStripeCount().setIsChangable(false);

        setTcpTransportType(new EntityModel<Boolean>());
        getTcpTransportType().setEntity(true);
        getTcpTransportType().setIsChangable(false);

        setRdmaTransportType(new EntityModel<Boolean>());
        getRdmaTransportType().setEntity(false);
        getRdmaTransportType().setIsAvailable(false);

        getTypeList().setSelectedItem(GlusterVolumeType.DISTRIBUTE);
        getReplicaCount().setIsAvailable(false);
        getStripeCount().setIsAvailable(false);

        setBricks(new ListModel<EntityModel<GlusterBrickEntity>>());

        getTypeList().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {

                getReplicaCount().setIsAvailable(getTypeList().getSelectedItem().isReplicatedType());
                getStripeCount().setIsAvailable(getTypeList().getSelectedItem().isStripedType());

                if (getBricks().getItems() != null && ((List) getBricks().getItems()).size() > 0
                        && !validateBrickCount()
                        && getAddBricksCommand().getIsExecutionAllowed())
                {
                    getAddBricksCommand().execute();
                }
            }
        });

        setGluster_accecssProtocol(new EntityModel<Boolean>());
        getGluster_accecssProtocol().setEntity(true);
        getGluster_accecssProtocol().setIsChangable(false);

        setNfs_accecssProtocol(new EntityModel<Boolean>());
        getNfs_accecssProtocol().setEntity(true);

        setCifs_accecssProtocol(new EntityModel<Boolean>());
        getCifs_accecssProtocol().setEntity(true);

        setAllowAccess(new EntityModel<String>());
        getAllowAccess().setEntity("*"); //$NON-NLS-1$

        setOptimizeForVirtStore(new EntityModel<Boolean>());
        getOptimizeForVirtStore().setEntity(false);
    }

    public EntityModel<String> getName() {
        return name;
    }

    public void setName(EntityModel<String> name) {
        this.name = name;
    }

    public ListModel<GlusterVolumeType> getTypeList() {
        return typeList;
    }

    public void setTypeList(ListModel<GlusterVolumeType> typeList) {
        this.typeList = typeList;
    }

    public EntityModel<Integer> getReplicaCount() {
        return replicaCount;
    }

    public Integer getReplicaCountValue() {
        return replicaCount.getEntity();
    }

    public void setReplicaCount(EntityModel<Integer> replicaCount) {
        this.replicaCount = replicaCount;
    }

    public EntityModel<Integer> getStripeCount() {
        return stripeCount;
    }

    public Integer getStripeCountValue() {
        return stripeCount.getEntity();
    }

    public void setStripeCount(EntityModel<Integer> stripeCount) {
        this.stripeCount = stripeCount;
    }

    public EntityModel<Boolean> getTcpTransportType() {
        return tcpTransportType;
    }

    public void setTcpTransportType(EntityModel<Boolean> tcpTransportType) {
        this.tcpTransportType = tcpTransportType;
    }

    public EntityModel<Boolean> getRdmaTransportType() {
        return rdmaTransportType;
    }

    public void setRdmaTransportType(EntityModel<Boolean> rdmaTransportType) {
        this.rdmaTransportType = rdmaTransportType;
    }

    public ListModel<EntityModel<GlusterBrickEntity>> getBricks() {
        return bricks;
    }

    public void setBricks(ListModel<EntityModel<GlusterBrickEntity>> bricks) {

        this.bricks = bricks;
        onPropertyChanged(new PropertyChangedEventArgs("Bricks")); //$NON-NLS-1$
    }

    public EntityModel<Boolean> getGluster_accecssProtocol() {
        return gluster_accecssProtocol;
    }

    public void setGluster_accecssProtocol(EntityModel<Boolean> gluster_accecssProtocol) {
        this.gluster_accecssProtocol = gluster_accecssProtocol;
    }

    public EntityModel<Boolean> getNfs_accecssProtocol() {
        return nfs_accecssProtocol;
    }

    public void setNfs_accecssProtocol(EntityModel<Boolean> nfs_accecssProtocol) {
        this.nfs_accecssProtocol = nfs_accecssProtocol;
    }

    public EntityModel<Boolean> getCifs_accecssProtocol() {
        return cifs_accecssProtocol;
    }

    public void setCifs_accecssProtocol(EntityModel<Boolean> cifs_accecssProtocol) {
        this.cifs_accecssProtocol = cifs_accecssProtocol;
    }

    public EntityModel<String> getAllowAccess() {
        return allowAccess;
    }

    public void setAllowAccess(EntityModel<String> allowAccess) {
        this.allowAccess = allowAccess;
    }

    public EntityModel<Boolean> getOptimizeForVirtStore() {
        return optimizeForVirtStore;
    }

    public void setOptimizeForVirtStore(EntityModel<Boolean> optimizeForVirtStore) {
        this.optimizeForVirtStore = optimizeForVirtStore;
    }

    public boolean isForceAddBricks() {
        return this.forceAddBricks;
    }

    public void setForceAddBricks(boolean forceAddBricks) {
        this.forceAddBricks = forceAddBricks;
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

        VDSGroup cluster = getCluster().getSelectedItem();
        if (cluster != null) {
            boolean isForceAddBrickSupported =
                    GlusterFeaturesUtil.isGlusterForceAddBricksSupported(cluster.getcompatibility_version());
            volumeBrickModel.getForce().setIsAvailable(isForceAddBrickSupported);
            volumeBrickModel.getForce().setEntity(isForceAddBricks() && isForceAddBrickSupported);
        }

        setWindow(volumeBrickModel);
        volumeBrickModel.setTitle(ConstantsManager.getInstance().getConstants().addBricksTitle());
        volumeBrickModel.setHelpTag(HelpTag.add_bricks);
        volumeBrickModel.setHashName("add_bricks"); //$NON-NLS-1$

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(volumeBrickModel);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                VolumeBrickModel volumeBrickModel = (VolumeBrickModel) model;
                ArrayList<VDS> hostList = (ArrayList<VDS>) result;
                Iterator<VDS> iterator = hostList.iterator();
                while (iterator.hasNext())
                {
                    if (iterator.next().getStatus() != VDSStatus.Up)
                    {
                        iterator.remove();
                    }
                }
                volumeBrickModel.getServers().setItems(hostList);
            }
        };
        AsyncDataProvider.getInstance().getHostListByCluster(_asyncQuery, getCluster().getSelectedItem().getName());

        // TODO: fetch the mount points to display
        if (getBricks().getItems() != null)
            volumeBrickModel.getBricks().setItems(getBricks().getItems());
        else
            volumeBrickModel.getBricks().setItems(new ArrayList<EntityModel<GlusterBrickEntity>>());

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

        GlusterVolumeType volumeType = getTypeList().getSelectedItem();
        if (!volumeBrickModel.validateBrickCount(volumeType, true))
        {
            String validationMsg =
                    volumeBrickModel.getValidationFailedMsg(getTypeList().getSelectedItem(), true);
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
            confirmModel.setHelpTag(HelpTag.add_bricks_confirmation);
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

        GlusterVolumeType selectedVolumeType = getTypeList().getSelectedItem();
        if (selectedVolumeType.isReplicatedType())
        {
            getReplicaCount().setEntity(volumeBrickModel.getReplicaCount().getEntity());
        }
        if (selectedVolumeType.isStripedType())
        {
            getStripeCount().setEntity(volumeBrickModel.getStripeCount().getEntity());
        }

        ArrayList<EntityModel<GlusterBrickEntity>> brickList = new ArrayList<EntityModel<GlusterBrickEntity>>();
        brickList.addAll(volumeBrickModel.getBricks().getItems());
        volumeBrickModel.getBricks().setItems(null);

        ListModel<EntityModel<GlusterBrickEntity>> brickListModel = new ListModel<EntityModel<GlusterBrickEntity>>();
        brickListModel.setItems(brickList);
        brickListModel.setSelectedItems(brickList);

        setBricks(brickListModel);

        setForceAddBricks(volumeBrickModel.getForce().getEntity());

        setWindow(null);
    }

    private void cancelConfirmation() {
        setConfirmWindow(null);
    }

    public boolean validateBrickCount()
    {
        return VolumeBrickModel.validateBrickCount(getTypeList().getSelectedItem(),
                getBricks(),
                getReplicaCountValue(),
                getStripeCountValue(),
                true);
    }

    public boolean validate() {
        if (!validateBrickCount())
        {
            setMessage(VolumeBrickModel.getValidationFailedMsg(getTypeList().getSelectedItem(),
                    true));
            return false;
        }

        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), new LengthValidation(50),
                new AsciiNameValidation() });

        setMessage(null);
        boolean validTransportTypes = true;
        if (getTcpTransportType().getEntity() == false
                && getRdmaTransportType().getEntity() == false)
        {
            validTransportTypes = false;
            setMessage(ConstantsManager.getInstance().getConstants().volumeTransportTypesValidationMsg());
        }

        return getName().getIsValid() && validTransportTypes;
    }

    private void clusterSelectedItemChanged()
    {
        setBricks(new ListModel<EntityModel<GlusterBrickEntity>>());

        if (getCluster().getSelectedItem() != null)
        {
            final VDSGroup cluster = getCluster().getSelectedItem();

            AsyncDataProvider.getInstance().isAnyHostUpInCluster(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object returnValue) {

                    // In case the result of previous call is returned after selecting some other cluster
                    if (!getCluster().getSelectedItem().getId().equals(cluster.getId())) {
                        return;
                    }

                    if ((Boolean) returnValue) {
                        getAddBricksCommand().setIsExecutionAllowed(true);
                        setMessage(null);
                    }
                    else {
                        getAddBricksCommand().setIsExecutionAllowed(false);
                        setMessage(ConstantsManager.getInstance().getConstants().volumeEmptyClusterValidationMsg());
                    }

                }
            }), cluster.getName());
        }
        else
        {
            getAddBricksCommand().setIsExecutionAllowed(false);
            setMessage(null);
        }
    }

    private void dataCenter_SelectedItemChanged()
    {
        StoragePool dataCenter = getDataCenter().getSelectedItem();
        if (dataCenter != null)
        {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object result)
                {
                    VolumeModel volumeModel = (VolumeModel) model;
                    ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) result;
                    VDSGroup oldCluster = volumeModel.getCluster().getSelectedItem();
                    StoragePool selectedDataCenter = getDataCenter().getSelectedItem();

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
                                    .equals(selectedDataCenter.getId()))
                    {
                        volumeModel.getCluster().setItems(clusters);

                        if (oldCluster != null)
                        {
                            VDSGroup newSelectedItem =
                                    Linq.firstOrDefault(clusters, new Linq.ClusterPredicate(oldCluster.getId()));
                            if (newSelectedItem != null)
                            {
                                volumeModel.getCluster().setSelectedItem(newSelectedItem);
                            }
                        }

                        if (volumeModel.getCluster().getSelectedItem() == null)
                        {
                            volumeModel.getCluster().setSelectedItem(Linq.firstOrDefault(clusters));
                        }
                    }
                }
            };

            // load clusters of Gluster type
            AsyncDataProvider.getInstance().getClusterByServiceList(_asyncQuery, dataCenter.getId(), false, true);
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

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
