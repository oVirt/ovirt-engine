package org.ovirt.engine.ui.uicommonweb.models.volumes;

import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;

public class VolumeListModel extends ListWithDetailsModel {
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

    private void createVolume() {
        // TODO Auto-generated method stub

    }

    private void removeVolume() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void SyncSearch() {
        super.SyncSearch();
    }

    @Override
    protected void OnSelectedItemChanged() {
        super.OnSelectedItemChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        // TODO Auto-generated method stub

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

    }

    @Override
    protected String getListName() {
        // TODO Auto-generated method stub
        return "VolumeListModel";
    }
}
