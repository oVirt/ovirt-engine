package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public class VolumeBrickListModel extends SearchableListModel {

    @Override
    protected String getListName() {
        return "VolumeBrickListModel";
    }

    public VolumeBrickListModel() {
        setTitle("Bricks");
        setIsTimerDisabled(false);
        setAddBricksCommand(new UICommand("Add Bricks", this));
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
        listModel.setTitle("Add Bricks");
        listModel.setHashName("add_bricks");

        // TODO: fetch the data to display
        listModel.setItems(new ArrayList<EntityModel>());

        UICommand command = new UICommand("Ok", this);
        command.setTitle("Ok");
        command.setIsDefault(true);
        listModel.getCommands().add(command);

        command = new UICommand("Cancel", this);
        command.setTitle("Cancel");
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
        } else if (command.getName().equals("Ok")) {
            onAddBricks();
        } else if (command.getName().equals("Cancel")) {
            setWindow(null);
        }
    }

}
