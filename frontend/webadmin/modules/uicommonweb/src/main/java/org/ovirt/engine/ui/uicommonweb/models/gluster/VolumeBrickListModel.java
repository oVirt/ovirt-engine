package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Iterator;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeBricksActionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeReplaceBrickActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterTaskOperation;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class VolumeBrickListModel extends SearchableListModel {

    @Override
    protected String getListName() {
        return "VolumeBrickListModel"; //$NON-NLS-1$
    }

    public VolumeBrickListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().bricksTitle());
        setHashName("bricks"); //$NON-NLS-1$
        setIsTimerDisabled(false);
        setAddBricksCommand(new UICommand("Add Bricks", this)); //$NON-NLS-1$
        setRemoveBricksCommand(new UICommand("Remove Bricks", this)); //$NON-NLS-1$
        setReplaceBrickCommand(new UICommand("Replace Brick", this)); //$NON-NLS-1$
        getReplaceBrickCommand().setIsAvailable(false);
    }

    private UICommand addBricksCommand;

    public UICommand getAddBricksCommand()
    {
        return addBricksCommand;
    }

    private void setAddBricksCommand(UICommand value)
    {
        addBricksCommand = value;
    }

    private UICommand removeBricksCommand;

    public UICommand getRemoveBricksCommand()
    {
        return removeBricksCommand;
    }

    private void setRemoveBricksCommand(UICommand value)
    {
        removeBricksCommand = value;
    }

    private UICommand replaceBrickCommand;

    public UICommand getReplaceBrickCommand()
    {
        return replaceBrickCommand;
    }

    private void setReplaceBrickCommand(UICommand value)
    {
        replaceBrickCommand = value;
    }

    @Override
    protected void OnSelectedItemChanged() {
        super.OnSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void SelectedItemsChanged()
    {
        super.SelectedItemsChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability()
    {
        getRemoveBricksCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0);
        getReplaceBrickCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() == 1);
    }

    @Override
    protected void OnEntityChanged() {
        super.OnEntityChanged();
        if (getEntity() == null) {
            return;
        }
        GlusterVolumeEntity glusterVolumeEntity = (GlusterVolumeEntity) getEntity();
        setItems(glusterVolumeEntity.getBricks());
    }

    @Override
    protected void SyncSearch() {
        OnEntityChanged();
    }

    private void addBricks() {

        if (getWindow() != null)
        {
            return;
        }

        GlusterVolumeEntity volumeEntity = (GlusterVolumeEntity) getEntity();

        if (volumeEntity == null)
        {
            return;
        }

        VolumeBrickModel volumeBrickModel = new VolumeBrickModel();

        volumeBrickModel.getReplicaCount().setEntity(volumeEntity.getReplicaCount());
        volumeBrickModel.getReplicaCount().setIsChangable(true);
        volumeBrickModel.getReplicaCount().setIsAvailable(volumeEntity.getVolumeType() == GlusterVolumeType.REPLICATE
                || volumeEntity.getVolumeType() == GlusterVolumeType.DISTRIBUTED_REPLICATE);

        volumeBrickModel.getStripeCount().setEntity(volumeEntity.getStripeCount());
        volumeBrickModel.getStripeCount().setIsChangable(true);
        volumeBrickModel.getStripeCount().setIsAvailable(volumeEntity.getVolumeType() == GlusterVolumeType.STRIPE
                || volumeEntity.getVolumeType() == GlusterVolumeType.DISTRIBUTED_STRIPE);

        setWindow(volumeBrickModel);
        volumeBrickModel.setTitle(ConstantsManager.getInstance().getConstants().addBricksVolume());
        volumeBrickModel.setHashName("add_bricks"); //$NON-NLS-1$
        volumeBrickModel.getVolumeType().setEntity(volumeEntity.getVolumeType());

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(volumeBrickModel);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                VDSGroup cluster = (VDSGroup) result;

                AsyncQuery _asyncQueryInner = new AsyncQuery();
                _asyncQueryInner.setModel(model);
                _asyncQueryInner.asyncCallback = new INewAsyncCallback() {
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
                AsyncDataProvider.GetHostListByCluster(_asyncQueryInner, cluster.getname());
            }
        };
        AsyncDataProvider.GetClusterById(_asyncQuery, volumeEntity.getClusterId());

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
        if (volumeBrickModel == null)
        {
            return;
        }

        if (!volumeBrickModel.validate())
        {
            return;
        }

        GlusterVolumeEntity volumeEntity = (GlusterVolumeEntity) getEntity();
        if (volumeEntity == null)
        {
            return;
        }

        ArrayList<GlusterBrickEntity> brickList = new ArrayList<GlusterBrickEntity>();
        for (Object model : volumeBrickModel.getBricks().getItems())
        {
            GlusterBrickEntity brickEntity = (GlusterBrickEntity) ((EntityModel) model).getEntity();
            brickEntity.setVolumeId(volumeEntity.getId());
            brickList.add(brickEntity);
        }

        volumeBrickModel.setMessage(null);

        if (brickList.size() == 0)
        {
            volumeBrickModel.setMessage(ConstantsManager.getInstance().getConstants().emptyAddBricksMsg());
            return;
        }

        if (!VolumeBrickModel.validateBrickCount(volumeEntity.getVolumeType(), volumeEntity.getBricks().size()
                + brickList.size(),
                volumeBrickModel.getReplicaCountValue(), volumeBrickModel.getStripeCountValue()))
        {
            volumeBrickModel.setMessage(VolumeBrickModel.getValidationFailedMsg(volumeEntity.getVolumeType()));
            return;
        }

        volumeBrickModel.StartProgress(null);

        GlusterVolumeBricksActionParameters parameter = new GlusterVolumeBricksActionParameters(volumeEntity.getId(),
                brickList, volumeBrickModel.getReplicaCountValue(), volumeBrickModel.getStripeCountValue());

        Frontend.RunAction(VdcActionType.AddBricksToGlusterVolume, parameter, new IFrontendActionAsyncCallback() {

            @Override
            public void Executed(FrontendActionAsyncResult result) {
                VolumeBrickModel localModel = (VolumeBrickModel) result.getState();
                localModel.StopProgress();
                setWindow(null);
            }
        }, volumeBrickModel);
    }

    private void removeBricks()
    {
        if (getSelectedItems() == null || getSelectedItems().isEmpty())
        {
            return;
        }

        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeBricksTitle());
        model.setHashName("remove_bricks"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().removeBricksMessage());

        java.util.ArrayList<String> list = new java.util.ArrayList<String>();
        for (GlusterBrickEntity item : Linq.<GlusterBrickEntity> Cast(getSelectedItems()))
        {
            list.add(item.getQualifiedName());
        }
        model.setItems(list);

        UICommand command1 = new UICommand("OnRemove", this); //$NON-NLS-1$
        command1.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command1.setIsDefault(true);
        model.getCommands().add(command1);

        UICommand command2 = new UICommand("Cancel", this); //$NON-NLS-1$
        command2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command2.setIsCancel(true);
        model.getCommands().add(command2);
    }

    private void onRemoveBricks() {
        if (getWindow() == null)
        {
            return;
        }

        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (getSelectedItems() == null || getSelectedItems().isEmpty()) {
            return;
        }

        GlusterVolumeEntity volumeEntity = (GlusterVolumeEntity) getEntity();

        GlusterVolumeRemoveBricksParameters parameter =
                new GlusterVolumeRemoveBricksParameters(volumeEntity.getId(), getSelectedItems());
        model.StartProgress(null);

        Frontend.RunAction(VdcActionType.GlusterVolumeRemoveBricks, parameter, new IFrontendActionAsyncCallback() {

            @Override
            public void Executed(FrontendActionAsyncResult result) {

                ConfirmationModel localModel = (ConfirmationModel) result.getState();
                localModel.StopProgress();
                setWindow(null);
            }
        }, model);
    }

    private void replaceBrick()
    {
        if (getWindow() != null)
        {
            return;
        }

        GlusterVolumeEntity volumeEntity = (GlusterVolumeEntity) getEntity();

        if (volumeEntity == null)
        {
            return;
        }

        ReplaceBrickModel brickModel = new ReplaceBrickModel();

        setWindow(brickModel);
        brickModel.setTitle(ConstantsManager.getInstance().getConstants().replaceBrickTitle());
        brickModel.setHashName("replace_brick"); //$NON-NLS-1$

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(brickModel);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                VDSGroup cluster = (VDSGroup) result;

                AsyncQuery _asyncQueryInner = new AsyncQuery();
                _asyncQueryInner.setModel(model);
                _asyncQueryInner.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model, Object result)
                    {
                        ReplaceBrickModel brickModel = (ReplaceBrickModel) model;
                        ArrayList<VDS> hostList = (ArrayList<VDS>) result;
                        brickModel.getServers().setItems(hostList);
                    }
                };
                AsyncDataProvider.GetHostListByCluster(_asyncQueryInner, cluster.getname());
            }
        };
        AsyncDataProvider.GetClusterById(_asyncQuery, volumeEntity.getClusterId());

        UICommand command = new UICommand("OnReplace", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        brickModel.getCommands().add(command);

        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsDefault(true);
        brickModel.getCommands().add(command);
    }

    private void onReplaceBrick()
    {
        ReplaceBrickModel replaceBrickModel = (ReplaceBrickModel) getWindow();
        if (replaceBrickModel == null)
        {
            return;
        }

        if (!replaceBrickModel.validate())
        {
            return;
        }

        GlusterVolumeEntity volumeEntity = (GlusterVolumeEntity) getEntity();
        if (volumeEntity == null)
        {
            return;
        }

        GlusterBrickEntity existingBrick = (GlusterBrickEntity) getSelectedItem();
        if (existingBrick == null)
        {
            return;
        }

        VDS server = (VDS) replaceBrickModel.getServers().getSelectedItem();

        GlusterBrickEntity newBrick = new GlusterBrickEntity();
        newBrick.setVolumeId(volumeEntity.getId());
        newBrick.setServerId(server.getId());
        newBrick.setServerName(server.gethost_name());
        newBrick.setBrickDirectory((String) replaceBrickModel.getBrickDirectory().getEntity());

        replaceBrickModel.StartProgress(null);

        GlusterVolumeReplaceBrickActionParameters parameter =
                new GlusterVolumeReplaceBrickActionParameters(volumeEntity.getId(),
                        GlusterTaskOperation.START,
                        existingBrick,
                        newBrick,
                        false);

        Frontend.RunAction(VdcActionType.ReplaceGlusterVolumeBrick, parameter, new IFrontendActionAsyncCallback() {

            @Override
            public void Executed(FrontendActionAsyncResult result) {

                ReplaceBrickModel localModel = (ReplaceBrickModel) result.getState();
                localModel.StopProgress();
                setWindow(null);
            }
        }, replaceBrickModel);

    }

    @Override
    public void ExecuteCommand(UICommand command) {
        super.ExecuteCommand(command);
        if (command.equals(getAddBricksCommand())) {
            addBricks();
        } else if (command.getName().equals("Ok")) { //$NON-NLS-1$
            onAddBricks();
        } else if (command.equals(getRemoveBricksCommand())) {
            removeBricks();
        } else if (command.getName().equals("OnRemove")) { //$NON-NLS-1$
            onRemoveBricks();
        } else if (command.equals(getReplaceBrickCommand())) {
            replaceBrick();
        } else if (command.getName().equals("OnReplace")) { //$NON-NLS-1$
            onReplaceBrick();
        }
        else if (command.getName().equals("Cancel")) { //$NON-NLS-1$
            setWindow(null);
        }
    }

}
