package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VolumeBrickListModel extends SearchableListModel {

    @Override
    protected String getListName() {
        return "VolumeBrickListModel";  //$NON-NLS-1$
    }

    public VolumeBrickListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().bricksTitle());
        setHashName("bricks");  //$NON-NLS-1$
        setIsTimerDisabled(false);
        setAddBricksCommand(new UICommand("Add Bricks", this)); //$NON-NLS-1$
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
        // TODO: add the code to do the action (which is currently not available)
        setWindow(null);
    }

    @Override
    public void ExecuteCommand(UICommand command) {
        super.ExecuteCommand(command);
        if (command.equals(getAddBricksCommand())) {
            addBricks();
        } else if (command.getName().equals("Ok")) { //$NON-NLS-1$
            onAddBricks();
        } else if (command.getName().equals("Cancel")) { //$NON-NLS-1$
            setWindow(null);
        }
    }

}
