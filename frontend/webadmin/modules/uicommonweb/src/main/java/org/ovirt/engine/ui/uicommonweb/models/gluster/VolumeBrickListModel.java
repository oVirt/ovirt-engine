package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
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

        ListModel listModel = new ListModel();
        setWindow(listModel);
        listModel.setTitle(ConstantsManager.getInstance().getConstants().addBricksVolume());
        listModel.setHashName("add_bricks"); //$NON-NLS-1$

        // TODO: fetch the data to display
        listModel.setItems(new ArrayList<EntityModel>());

        UICommand command = new UICommand("Ok", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        listModel.getCommands().add(command);

        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsDefault(true);
        listModel.getCommands().add(command);
    }

    private void onAddBricks() {
        ListModel listModel = (ListModel) getWindow();
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
