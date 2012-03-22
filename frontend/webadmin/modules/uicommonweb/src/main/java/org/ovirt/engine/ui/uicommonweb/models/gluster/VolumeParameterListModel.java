package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public class VolumeParameterListModel extends SearchableListModel {

    private UICommand addParameterCommand;
    private UICommand removeParameterCommand;

    public UICommand getAddParameterCommand() {
        return addParameterCommand;
    }

    public void setAddParameterCommand(UICommand command) {
        this.addParameterCommand = command;
    }

    public void setRemoveParameterCommand(UICommand command) {
        this.removeParameterCommand = command;
    }

    public UICommand getRemoveParameterCommand() {
        return removeParameterCommand;
    }

    @Override
    protected String getListName() {
        // TODO Auto-generated method stub
        return "VolumeParameterListModel";
    }

    public VolumeParameterListModel()
    {
        setAddParameterCommand(new UICommand("Add", this));
        setRemoveParameterCommand(new UICommand("Remove", this));
    }

    private void addParameter() {
        // TODO Auto-generated method stub

    }

    private void removeParameter() {
        // TODO Auto-generated method stub

    }

    @Override
    public void ExecuteCommand(UICommand command) {
        super.ExecuteCommand(command);
        if (command.equals(getAddParameterCommand())) {
            addParameter();
        }
        else if (command.equals(getRemoveParameterCommand())) {
            removeParameter();
        }
    }
}
