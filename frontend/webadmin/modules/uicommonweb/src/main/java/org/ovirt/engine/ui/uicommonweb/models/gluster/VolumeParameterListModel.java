package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeOptionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionInfo;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VolumeParameterListModel extends SearchableListModel {

    private UICommand addParameterCommand;
    private UICommand editParameterCommand;
    private UICommand resetAllParameterCommand;

    public UICommand getAddParameterCommand() {
        return addParameterCommand;
    }

    public void setAddParameterCommand(UICommand command) {
        this.addParameterCommand = command;
    }

    public UICommand getEditParameterCommand() {
        return editParameterCommand;
    }

    public void setEditParameterCommand(UICommand command) {
        this.editParameterCommand = command;
    }

    public void setResetAllParameterCommand(UICommand command) {
        this.resetAllParameterCommand = command;
    }

    public UICommand getResetAllParameterCommand() {
        return resetAllParameterCommand;
    }

    @Override
    protected String getListName() {
        // TODO Auto-generated method stub
        return "VolumeParameterListModel"; //$NON-NLS-1$
    }

    public VolumeParameterListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().parameterTitle());
        setHashName("parameters"); //$NON-NLS-1$
        setAddParameterCommand(new UICommand(ConstantsManager.getInstance().getConstants().AddVolume(), this));
        setEditParameterCommand(new UICommand(ConstantsManager.getInstance().getConstants().editVolume(), this));
        setResetAllParameterCommand(new UICommand(ConstantsManager.getInstance().getConstants().resetAllVolume(), this));
    }

    @Override
    protected void OnSelectedItemChanged()
    {
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
        editParameterCommand.setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() == 1);
    }

    private void addParameter() {
        if (getWindow() != null) {
            return;
        }

        VolumeParameterModel volumeParameterModel = new VolumeParameterModel();
        volumeParameterModel.setTitle(ConstantsManager.getInstance().getConstants().addOptionVolume());
        setWindow(volumeParameterModel);

        volumeParameterModel.getKeyList().setItems(new ArrayList<GlusterVolumeOptionInfo>());

        UICommand command = new UICommand("OnAddParameter", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        volumeParameterModel.getCommands().add(command);
        command = new UICommand("OnAddParameterCancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsDefault(true);
        volumeParameterModel.getCommands().add(command);
    }

    private void onAddParameter() {
        if (getEntity() == null) {
            return;
        }

        GlusterVolumeEntity volume = (GlusterVolumeEntity) getEntity();

        VolumeParameterModel model = (VolumeParameterModel) getWindow();

        if (!model.Validate())
        {
            return;
        }

        GlusterVolumeOptionEntity option = new GlusterVolumeOptionEntity();
        option.setVolumeId(volume.getId());
        option.setKey(((GlusterVolumeOptionInfo) model.getKeyList().getSelectedItem()).getKey());
        option.setValue((String) model.getValue().getEntity());

        Frontend.RunAction(VdcActionType.SetGlusterVolumeOption,
                new GlusterVolumeOptionParameters(option),
                new IFrontendActionAsyncCallback() {

                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                    }
                });
        setWindow(null);
    }

    private void onAddParameterCancel() {
        setWindow(null);
    }

    private void editParameter() {
        if (getWindow() != null) {
            return;
        }

        VolumeParameterModel volumeParameterModel = new VolumeParameterModel();
        volumeParameterModel.setTitle(ConstantsManager.getInstance().getConstants().editOptionVolume());
        setWindow(volumeParameterModel);

        ArrayList<GlusterVolumeOptionInfo> optionList = new ArrayList<GlusterVolumeOptionInfo>();
        volumeParameterModel.getKeyList().setItems(optionList);
        volumeParameterModel.getKeyList().setIsChangable(false);

        GlusterVolumeOptionEntity selectedOption = (GlusterVolumeOptionEntity) getSelectedItem();

        for (GlusterVolumeOptionInfo option : optionList) {
            if (option.getKey().equals(selectedOption.getKey()))
            {
                volumeParameterModel.getKeyList().setSelectedItem(option);
                break;
            }
        }

        volumeParameterModel.getValue().setEntity(selectedOption.getValue());

        UICommand command = new UICommand("OnAddParameter", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        volumeParameterModel.getCommands().add(command);
        command = new UICommand("OnAddParameterCancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsDefault(true);
        volumeParameterModel.getCommands().add(command);
    }

    private void resetAllParameter() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void OnEntityChanged() {
        if (getEntity() == null) {
            return;
        }
        super.OnEntityChanged();
        GlusterVolumeEntity glusterVolumeEntity = (GlusterVolumeEntity) getEntity();
        ArrayList<GlusterVolumeOptionEntity> list = new ArrayList<GlusterVolumeOptionEntity>();
        for (GlusterVolumeOptionEntity glusterVolumeOption : glusterVolumeEntity.getOptions()) {
            list.add(glusterVolumeOption);
        }
        setItems(list);
    }

    @Override
    public void ExecuteCommand(UICommand command) {
        super.ExecuteCommand(command);
        if (command.equals(getAddParameterCommand())) {
            addParameter();
        }
        else if (command.getName().equals("OnAddParameter")) { //$NON-NLS-1$
            onAddParameter();
        }
        else if (command.getName().equals("OnAddParameterCancel")) { //$NON-NLS-1$
            onAddParameterCancel();
        }
        else if (command.equals(getEditParameterCommand())) {
            editParameter();
        }
        else if (command.equals(getResetAllParameterCommand())) {
            resetAllParameter();
        }
    }
}
